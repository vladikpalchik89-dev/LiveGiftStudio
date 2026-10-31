
const express = require("express");
const http = require("http");
const WebSocket = require("ws");

const app = express();
app.use(express.json());
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: "/events" });

const PORT = process.env.PORT || 3000;
let username = (process.env.TIKTOK_USERNAME || "vinnipyh99")
  .replace(/^@/, "")
  .trim();

const switchKey = process.env.ACCOUNT_SWITCH_KEY || "";
const testKey = process.env.TEST_EVENT_KEY || "";

// Бесплатный ключ EulerStream поднимает лимит подписей
// с нескольких подключений в день до 2500 запросов.
const signApiKey =
  process.env.SIGN_API_KEY ||
  process.env.EULER_API_KEY ||
  "";

let currentConnection = null;
let tiktokConnected = false;
let tiktokError = "";
let connectStartedAt = 0;
let rateLimitedUntil = 0;
let lastSwitch = null;
let consecutiveFailures = 0;
let reconnectTimer = null;
let connecting = false;
let stopped = false;

let donationAlertsSocket = null;
let donationAlertsTimer = null;

const https = require("https");

let seenDonationIds = new Set();
let donationPollTimer = null;
let donationPollStarted = false;
let donationPollError = null;
let lastDonationId = null;
let lastDonationAt = null;
let donationsForwarded = 0;
const recentEvents = [];

const startedAt = Date.now();

app.get("/", (_req, res) => {
  res.json({
    app: "LiveGiftStudio",
    status: "running",
    tiktok: username,
    tiktokEnabled: process.env.TIKTOK_ENABLED === "true",
    tiktokConnected: tiktokConnected,
    websocket: "/events"
  });
});

app.get("/health", (_req, res) => {
  res.json({
    status: "ok",
    time: new Date().toISOString(),
    uptimeSeconds: Math.round((Date.now() - startedAt) / 1000),
    tiktok: {
      enabled: process.env.TIKTOK_ENABLED === "true",
      username,
      connected: tiktokConnected,
      connecting,
      lastError: tiktokError,
      apiKey: Boolean(signApiKey),
      lastSwitch,
      rateLimitedUntil: rateLimitedUntil
        ? new Date(rateLimitedUntil).toISOString()
        : null,
      connectedFor: tiktokConnected && connectStartedAt
        ? Math.round((Date.now() - connectStartedAt) / 1000)
        : 0
    },
    donationAlerts: {
      socket: Boolean(donationAlertsSocket),
      polling: Boolean(donationPollTimer),
      forwarded: donationsForwarded,
      lastDonationId,
      lastDonationAt,
      lastPollError: donationPollError
    },
    websocketClients: wss.clients.size,
    events: {
      forwarded: eventCounter,
      lastType: lastEventType,
      lastAt: lastEventAt
    }
  });
});

// Последние события: видно, дошёл ли донат до сервера.
app.get("/debug/last-events", requireTestKey, (_req, res) => {
  res.json({
    status: "ok",
    donationAlerts: {
      socket: Boolean(donationAlertsSocket),
      polling: Boolean(donationPollTimer),
      lastPollError: donationPollError
    },
    events: recentEvents.slice(-10)
  });
});

// Общая проверка ключа для тестовых эндпоинтов.
function requireTestKey(req, res, next) {
  // Ключ тестов и ключ переключения аккаунта равнозначны:
  // приложение знает только второй.
  const accepted = [testKey, switchKey].filter(Boolean);

  if (accepted.length === 0) {
    return res.status(503).json({
      status: "error",
      message: "TEST_EVENT_KEY is not configured"
    });
  }

  const provided = req.query.key || req.get("x-switch-key") || "";

  if (!accepted.includes(provided)) {
    return res.status(403).json({
      status: "error",
      message: "Invalid test key"
    });
  }

  next();
}

// Переключение аккаунта умеет приходить двумя путями: обычным
// HTTP-запросом и сообщением по уже открытому WebSocket.
function applyAccountSwitch(rawUsername, via) {
  const newUsername = String(rawUsername || "")
    .trim()
    .replace(/^@/, "");

  if (!/^[a-zA-Z0-9._]{2,24}$/.test(newUsername)) {
    return {
      status: 400,
      body: { status: "error", message: "Invalid TikTok username" }
    };
  }

  if (newUsername.toLowerCase() === username.toLowerCase()) {
    return {
      status: 200,
      body: {
        status: "ok",
        username,
        message: "This account is already selected"
      }
    };
  }

  const oldConnection = currentConnection;

  // Сначала отключаем старый эфир.
  currentConnection = null;
  connecting = false;
  tiktokConnected = false;
  tiktokError = "switching account";

  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }

  if (oldConnection) {
    try {
      oldConnection.disconnect();
    } catch (error) {
      console.error("Disconnect error:", error.message);
    }
  }

  const previousUsername = username;
  username = newUsername;

  lastSwitch = {
    at: new Date().toISOString(),
    from: previousUsername,
    to: username,
    via: via || "http"
  };

  console.log(
    "Active TikTok account changed to:",
    username,
    "(was:",
    previousUsername + ", via " + lastSwitch.via + ")"
  );

  if (process.env.TIKTOK_ENABLED === "true") {
    console.log("Starting TikTok connection for:", username);
    startTikTok();
  } else {
    console.log(
      "TikTok integration is disabled (TIKTOK_ENABLED is not true)"
    );
  }

  return {
    status: 200,
    body: {
      status: "ok",
      username,
      tiktokEnabled: process.env.TIKTOK_ENABLED === "true",
      message: "Account selection updated"
    }
  };
}

app.post("/switch-account", (req, res) => {
  if (!switchKey) {
    return res.status(503).json({
      status: "error",
      message: "ACCOUNT_SWITCH_KEY is not configured"
    });
  }

  if (req.get("x-switch-key") !== switchKey) {
    return res.status(403).json({
      status: "error",
      message: "Invalid switch key"
    });
  }

  const result = applyAccountSwitch(req.body?.username, "http");

  res.status(result.status).json(result.body);
});


app.get("/test-like", requireTestKey, (req, res) => {
  const event = {
    type: "like",
    username: String(req.query.username || "TestViewer"),
    likeCount: Number(req.query.count || 10) || 10,
    totalLikeCount: 0,
    timestamp: Date.now(),
    test: true
  };

  sendEvent(event);

  res.json({ status: "ok", message: "Test like sent", event });
});

app.get("/test-gift", requireTestKey, (req, res) => {
  const event = {
    type: "gift",
    username: "TestViewer",
    giftName: "Rose",
    repeatCount: 1,
    timestamp: Date.now(),
    test: true
  };

  sendEvent(event);
  res.json({ status: "ok", message: "Test gift sent", event });
});

app.get("/test-donation", requireTestKey, (req, res) => {
  const event = {
    type: "donation",
    username: "Тестовый зритель",
    amount: 50,
    currency: "RUB",
    message: "Проверка доната",
    timestamp: Date.now(),
    test: true
  };

  sendEvent(event);
  res.json({ status: "ok", message: "Test donation sent", event });
});

app.get("/test-follow", requireTestKey, (req, res) => {
  const event = {
    type: "follow",
    username: "TestFollower",
    timestamp: Date.now(),
    test: true
  };

  sendEvent(event);
  res.json({ status: "ok", message: "Test follow sent", event });
});

app.get("/test-chat", requireTestKey, (req, res) => {
  const event = {
    type: "chat",
    username: "TestViewer",
    comment: String(req.query.text || "Тестовое сообщение"),
    timestamp: Date.now(),
    test: true
  };

  sendEvent(event);
  res.json({ status: "ok", message: "Test chat sent", event });
});


let eventCounter = 0;
let lastEventAt = null;
let lastEventType = null;

function sendEvent(event) {
  const payload = { id: ++eventCounter, ...event };
  lastEventAt = new Date().toISOString();
  lastEventType = payload.type || null;
  const message = JSON.stringify(payload);
  let delivered = 0;

  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      try {
        client.send(message);
        delivered++;
      } catch (error) {
        console.error("WebSocket send failed:", error.message);
      }
    }
  });

  recentEvents.push(payload);

  if (recentEvents.length > 20) {
    recentEvents.shift();
  }

  console.log("Event:", message, "| WebSocket clients:", delivered);
}

function getUserName(data) {
  return data?.user?.uniqueId ||
    data?.user?.nickname ||
    data?.uniqueId ||
    data?.nickname ||
    data?.displayName ||
    data?.userName ||
    "Зритель";
}


// Картинка профиля: коннектор отдаёт её в разных полях
// в зависимости от версии.
function getUserAvatar(data) {
  const user = data?.user || data || {};
  const raw =
    user.profilePictureUrl ||
    user.avatarLarger ||
    user.avatarMedium ||
    user.avatarThumb ||
    user.avatar ||
    "";

  let url = typeof raw === "string" ? raw.replace(/&amp;/g, "&") : "";

  if (url.startsWith("//")) url = "https:" + url;
  if (url.startsWith("http://")) url = "https://" + url.slice(7);

  return url;
}

function getGiftName(data) {
  return data?.giftDetails?.giftName ||
    data?.extendedGiftInfo?.name ||
    data?.giftName ||
    data?.gift?.name ||
    (data?.giftId ? "Подарок " + data.giftId : "Подарок");
}

function getGiftInfo(data) {
  const giftDetails = data?.giftDetails || {};
  const extended = data?.extendedGiftInfo || {};
  const gift = data?.gift || {};

  const rawGiftId =
    data?.giftId ??
    giftDetails?.giftId ??
    extended?.giftId ??
    gift?.id ??
    null;

  const rawDiamondCount =
    data?.diamondCount ??
    giftDetails?.diamondCount ??
    extended?.diamondCount ??
    gift?.diamondCount ??
    null;

  const rawRepeatCount = Number(data?.repeatCount || 1);

  return {
    giftName: getGiftName(data),
    giftId: rawGiftId == null ? null : String(rawGiftId),
    giftDiamondCount:
      rawDiamondCount != null &&
      Number.isFinite(Number(rawDiamondCount))
        ? Number(rawDiamondCount)
        : null,
    repeatCount: Number.isFinite(rawRepeatCount)
      ? Math.max(1, rawRepeatCount)
      : 1,
    repeatEnd: data?.repeatEnd ?? data?.repeat_end ?? null
  };
}



wss.on("connection", (socket, req) => {
  console.log(
    "WebSocket client connected:",
    req.socket.remoteAddress,
    "| total clients:",
    wss.clients.size
  );

  socket.isAlive = true;

  socket.send(JSON.stringify({
    type: "connected",
    message: "LiveGiftStudio connected"
  }));

  socket.on("pong", () => {
    socket.isAlive = true;
  });

  // Переключение аккаунта по сокету: приложение уже подключено,
  // и это надёжнее, чем отдельный HTTP-запрос.
  socket.on("message", (raw) => {
    try {
      const message = JSON.parse(raw.toString());

      if (message.type === "reconnect") {
        if (!switchKey || message.key !== switchKey) {
          socket.send(JSON.stringify({
            type: "switch_result",
            ok: false,
            message: "Invalid switch key"
          }));
          return;
        }

        const result = forceReconnect();

        socket.send(JSON.stringify({
          type: "switch_result",
          ok: result.ok,
          username: result.username,
          message: result.message
        }));

        return;
      }

      if (message.type !== "switch_account") return;

      if (!switchKey || message.key !== switchKey) {
        socket.send(JSON.stringify({
          type: "switch_result",
          ok: false,
          message: "Invalid switch key"
        }));
        return;
      }

      const result = applyAccountSwitch(message.username, "socket");

      socket.send(JSON.stringify({
        type: "switch_result",
        ok: result.status === 200,
        username: result.body?.username || "",
        message: result.body?.message || ""
      }));

      console.log(
        "Account switch via WebSocket:",
        result.status,
        result.body?.username
      );
    } catch (error) {
      console.error("WebSocket message error:", error.message);
    }
  });

  socket.on("close", (code, reason) => {
    console.log(
      "WebSocket client disconnected:",
      "code =", code,
      "reason =", reason.toString()
    );
  });

  socket.on("error", (error) => {
    console.error("WebSocket error:", error.message);
  });
});

// Раз в 30 секунд проверяем, что клиенты живы: мёртвые сокеты
// от мобильной сети иначе висят в wss.clients до бесконечности.
const heartbeat = setInterval(() => {
  wss.clients.forEach((socket) => {
    if (socket.isAlive === false) {
      socket.terminate();
      return;
    }

    socket.isAlive = false;
    socket.ping();
  });
}, 30 * 1000);

heartbeat.unref();

wss.on("close", () => {
  clearInterval(heartbeat);
});


server.listen(PORT, "0.0.0.0", () => {
  console.log("LiveGiftStudio server listening on", PORT);

  console.log(
    "EulerStream API key:",
    signApiKey ? "configured" : "NOT configured (low daily limit)"
  );
  startTikTok();
  startDonationAlerts();
  startDonationPolling();
});

app.use((_req, res) => {
  res.status(404).json({
    status: "error",
    message: "Not found"
  });
});

// Сюда попадают ошибки express, например битый JSON в теле запроса.
app.use((error, _req, res, _next) => {
  console.error("HTTP error:", error.message);
  res.status(error.status || 500).json({
    status: "error",
    message: error.message || "Internal server error"
  });
});

function shutdown(signal) {
  if (stopped) return;
  stopped = true;

  console.log(signal, "received, shutting down");

  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }

  if (donationPollTimer) {
    clearInterval(donationPollTimer);
    donationPollTimer = null;
  }

  if (donationAlertsTimer) {
    clearTimeout(donationAlertsTimer);
    donationAlertsTimer = null;
  }

  if (currentConnection) {
    try {
      currentConnection.disconnect();
    } catch (error) {
      console.error("TikTok disconnect error:", error.message);
    }
    currentConnection = null;
  }

  if (donationAlertsSocket) {
    try {
      donationAlertsSocket.disconnect();
    } catch (error) {
      console.error("DonationAlerts disconnect error:", error.message);
    }
    donationAlertsSocket = null;
  }

  wss.clients.forEach((client) => {
    try {
      client.close(1001, "Server shutting down");
    } catch (error) {
      console.error("WebSocket close error:", error.message);
    }
  });

  server.close(() => {
    console.log("HTTP server closed");
    process.exit(0);
  });

  // Не даём процессу зависнуть, если сокеты не закрылись.
  setTimeout(() => process.exit(0), 5000).unref();
}

process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));



async function startDonationAlerts() {
  const token = process.env.DONATIONALERTS_TOKEN;

  if (!token) {
    console.log("DonationAlerts paused: token is missing");
    return;
  }

  if (stopped) return;

  try {
    const { DASocket, EventName } =
      require("donationalerts-api");

    const socket = new DASocket(token, {
      socketUrl: "wss://socket7.donationalerts.ru",
      autoConnect: false
    });

    socket.on(EventName.Donation, (donation) => {
      forwardDonation(donation);
    });

    socket.on("close", () => {
      handleDonationAlertsDisconnect("closed");
    });

    socket.on("error", (error) => {
      console.error(
        "DonationAlerts socket error:",
        error?.message || error
      );
      handleDonationAlertsDisconnect("error");
    });

    await socket.connect();
    console.log("DonationAlerts socket connected");

    donationAlertsSocket = socket;
  } catch (error) {
    console.error(
      "DonationAlerts connection failed:",
      error.stack || error.message
    );
    handleDonationAlertsDisconnect("connect failed");
  }
}

function daGet(path) {
  return new Promise((resolve, reject) => {
    const token = process.env.DONATIONALERTS_TOKEN;

    if (!token) {
      return reject(new Error("DONATIONALERTS_TOKEN is missing"));
    }

    const request = https.get(
      `https://www.donationalerts.com${path}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "application/json"
        }
      },
      (response) => {
        let body = "";

        response.on("data", (chunk) => {
          body += chunk;
        });

        response.on("end", () => {
          if (response.statusCode < 200 || response.statusCode >= 300) {
            return reject(new Error(
              `HTTP ${response.statusCode}: ${body.slice(0, 160)}`
            ));
          }

          try {
            resolve(JSON.parse(body));
          } catch (error) {
            reject(error);
          }
        });
      }
    );

    request.on("error", reject);
    request.setTimeout(15000, () => {
      request.destroy(new Error("DonationAlerts request timeout"));
    });
  });
}

function forwardDonation(donation) {
  const id = String(donation.id ?? "");

  if (id) {
    if (seenDonationIds.has(id)) return;

    seenDonationIds.add(id);

    if (seenDonationIds.size > 500) {
      seenDonationIds = new Set(
        Array.from(seenDonationIds).slice(-250)
      );
    }
  }

  lastDonationId = id || null;
  lastDonationAt = new Date().toISOString();
  donationsForwarded++;

  sendEvent({
    type: "donation",
    username: String(donation.username || "Зритель"),
    amount: donation.amountMain ?? donation.amount ?? 0,
    currency: String(donation.currency || "RUB"),
    message: String(donation.message || ""),
    timestamp: Date.now()
  });
}

// Резервный канал: раз в 15 секунд спрашиваем у DonationAlerts
// последние донаты. Если сокет молчит, донат всё равно дойдёт.
async function pollDonations() {
  if (stopped) return;

  try {
    const body = await daGet("/api/v1/alerts/donations?page=1");
    const list = Array.isArray(body)
      ? body
      : (Array.isArray(body?.data) ? body.data : []);

    const fresh = [];

    for (const donation of list) {
      const id = String(donation.id ?? "");

      if (!id || seenDonationIds.has(id)) continue;

      fresh.push(donation);
    }

    // Первый опрос только запоминает старые донаты,
    // чтобы не проигрывать их заново после перезапуска.
    if (!donationPollStarted) {
      donationPollStarted = true;

      for (const donation of fresh) {
        seenDonationIds.add(String(donation.id));
      }

      return;
    }

    fresh.sort((left, right) => Number(left.id) - Number(right.id));

    for (const donation of fresh) {
      forwardDonation(donation);
    }

    donationPollError = null;
  } catch (error) {
    donationPollError = error.message;
    console.error("DonationAlerts poll failed:", error.message);
  }
}

function startDonationPolling() {
  // Донаты теперь опрашивает само приложение по своему токену.
  // Серверный опрос с просроченным токеном только засорял логи
  // ошибками 401 каждые 15 секунд.
  console.log(
    "DonationAlerts polling disabled: the Android app polls directly"
  );
}

function handleDonationAlertsDisconnect(reason) {
  if (stopped) return;
  if (donationAlertsTimer) return;

  donationAlertsSocket = null;

  const delay = 60 * 1000;

  console.log(
    "DonationAlerts reconnect scheduled:",
    reason,
    "in",
    delay / 1000,
    "seconds"
  );

  donationAlertsTimer = setTimeout(() => {
    donationAlertsTimer = null;
    startDonationAlerts();
  }, delay);
}



// Сторож: раз в минуту проверяет, что связь действительно есть.
// Раньше флаг «подключено» врал, потому что выставлялся до connect().
setInterval(() => {
  if (stopped || process.env.TIKTOK_ENABLED !== "true") return;
  if (tiktokConnected) return;
  if (reconnectTimer) return;

  if (rateLimitedUntil && Date.now() < rateLimitedUntil) return;

  // Даём текущей попытке 90 секунд.
  if (connecting && Date.now() - connectStartedAt < 90000) return;

  console.log(
    "TikTok watchdog: no connection, restarting (last error:",
    tiktokError || "none",
    ")"
  );

  connecting = false;
  startTikTok();
}, 60 * 1000);

// Принудительное переподключение: сбрасывает паузы и пробует сразу.
function forceReconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }

  consecutiveFailures = 0;
  connecting = false;
  tiktokConnected = false;
  tiktokError = "manual reconnect";

  if (currentConnection) {
    try {
      currentConnection.disconnect();
    } catch (error) {
      console.error("Reconnect disconnect error:", error.message);
    }

    currentConnection = null;
  }

  console.log("Manual reconnect requested for:", username);

  startTikTok();

  return {
    ok: true,
    username,
    message: "Reconnect started"
  };
}

app.post("/reconnect", (req, res) => {
  if (!switchKey) {
    return res.status(503).json({
      status: "error",
      message: "ACCOUNT_SWITCH_KEY is not configured"
    });
  }

  if (req.get("x-switch-key") !== switchKey) {
    return res.status(403).json({
      status: "error",
      message: "Invalid switch key"
    });
  }

  const result = forceReconnect();

  res.json({
    status: result.ok ? "ok" : "error",
    username: result.username,
    message: result.message
  });
});

function scheduleReconnect(reason) {
  if (stopped || process.env.TIKTOK_ENABLED !== "true") return;
  if (reconnectTimer) return;

  const isRateLimit =
    String(reason || "").toLowerCase().includes("rate limit") ||
    String(reason || "").toLowerCase().includes("connection failed");

  const delay = isRateLimit ? 5 * 60 * 1000 : 60 * 1000;

  console.log(
    "TikTok reconnect scheduled:",
    reason,
    "in",
    Math.round(delay / 1000),
    "seconds"
  );

  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    startTikTok();
  }, delay);
}


function startTikTok() {
  if (stopped || process.env.TIKTOK_ENABLED !== "true") {
    console.log("TikTok connection paused. Set TIKTOK_ENABLED=true to enable.");
    return;
  }

  if (connecting) return;
  connecting = true;
  connectStartedAt = Date.now();
  console.log("TikTok connect attempt:", username);

  try {
    const connector = require("tiktok-live-connector");
    const TikTokLiveConnection = connector.TikTokLiveConnection;
    const WebcastEvent = connector.WebcastEvent || {};
    const ControlEvent = connector.ControlEvent || {};

    if (typeof TikTokLiveConnection !== "function") {
      throw new Error("TikTokLiveConnection not found in connector");
    }

    const connection = new TikTokLiveConnection(username, {
      processInitialData: false,
      ...(signApiKey ? { signApiKey } : {})
    });

    currentConnection = connection;

    
connection.on(WebcastEvent.GIFT || "gift", (data) => {
  if (connection !== currentConnection) return;

  const info = getGiftInfo(data);

  sendEvent({
    type: "gift",
    username: getUserName(data),
    avatar: getUserAvatar(data),
    giftName: info.giftName,
    giftId: info.giftId,
    giftDiamondCount: info.giftDiamondCount,
    repeatCount: info.repeatCount,
    repeatEnd: info.repeatEnd,
    timestamp: Date.now()
  });
});
    
    connection.on(WebcastEvent.LIKE || "like", (data) => {
      if (connection !== currentConnection) return;

      const likeCount = Number(
        data?.likeCount ?? data?.count ?? 1
      );

      sendEvent({
        type: "like",
        username: getUserName(data),
        avatar: getUserAvatar(data),
        likeCount:
          Number.isFinite(likeCount) && likeCount > 0
            ? likeCount
            : 1,
        totalLikeCount: Number(data?.totalLikeCount || 0) || 0,
        timestamp: Date.now()
      });
    });

    connection.on(WebcastEvent.CHAT || "chat", (data) => {
      if (connection !== currentConnection) return;

      const comment = String(
        data?.comment ??
        data?.text ??
        data?.message ??
        data?.content ??
        data?.textMessage?.text ??
        ""
      ).trim();

      console.log("TikTok CHAT received:", {
  hasComment: Boolean(comment),
  fields: Object.keys(data || {}),
  userFields: Object.keys(data?.user || {}),
  userDetails: data?.user
});

      if (!comment) return;

      sendEvent({
        type: "chat",
        username: getUserName(data),
        comment,
        timestamp: Date.now()
      });
    });


    const handleFollow = (data) => {
      if (connection !== currentConnection) return;

      sendEvent({
        type: "follow",
        username: getUserName(data),
        timestamp: Date.now()
      });
    };

    connection.on(WebcastEvent.FOLLOW || "follow", handleFollow);

    // Некоторые версии коннектора передают подписки через SOCIAL.
    if (WebcastEvent.SOCIAL) {
      connection.on(WebcastEvent.SOCIAL, (data) => {
        if (connection !== currentConnection) return;

        const description = [
          data?.displayType,
          data?.label,
          data?.action,
          data?.eventType
        ].filter(Boolean).join(" ");

        if (/follow|followed|подпис|关注/i.test(description)) {
          handleFollow(data);
        }
      });
    }

    const handleDisconnect = (reason) => {
      if (connection !== currentConnection) return;

      connecting = false;
      tiktokConnected = false;
      tiktokError = String(reason || "disconnected");
      currentConnection = null;

      console.log("TikTok disconnected:", tiktokError);

      scheduleReconnect(reason || "disconnected");
    };

    connection.on(
      ControlEvent.DISCONNECTED || "disconnected",
      () => handleDisconnect("disconnected")
    );

    connection.on(
  WebcastEvent.STREAM_END || "streamEnd",
  () => {
    if (connection !== currentConnection) return;

    console.log("TikTok LIVE ended. Stopping chat events.");

    // Останавливаем обработку чата старого эфира.
    currentConnection = null;
    connecting = false;
    tiktokConnected = false;
    tiktokError = "stream ended";

    // Сообщаем Android, что эфир завершился.
    sendEvent({
      type: "stream_end",
      timestamp: Date.now()
    });

    try {
      connection.disconnect();
    } catch (error) {
      console.error("Stream end disconnect error:", error.message);
    }

    scheduleReconnect("stream ended");
  }
);

    connection.connect()
      .then((state) => {
        if (connection !== currentConnection) return;

        connecting = false;
        tiktokConnected = true;
        tiktokError = "";
        connectStartedAt = Date.now();
        consecutiveFailures = 0;

        console.log(
          "TikTok LIVE connected:",
          username,
          "| room:",
          state?.roomId || "?"
        );
      })
      
.catch((error) => {
  if (connection !== currentConnection) return;

  connecting = false;
  tiktokConnected = false;
  tiktokError = String(error.message || error);

  console.error(
    "TikTok connection failed:",
    error.stack || error.message
  );

  const errorText = [
    error?.name,
    error?.message,
    error?.cause?.message,
    error?.stack
  ].filter(Boolean).join(" ");

  
if (
  /SignatureRateLimitError|rate_limit_account_day|rate limit/i.test(errorText)
) {
  rateLimitedUntil = Date.now() + 30 * 60 * 1000;

  console.error(
    "EulerStream daily limit reached.",
    signApiKey
      ? "Next attempt in 30 minutes."
      : "Add a free SIGN_API_KEY from eulerstream.com — next attempt in 30 minutes."
  );

  if (!reconnectTimer) {
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null;
      startTikTok();
    }, 30 * 60 * 1000);
  }

  return;
}


if (/requested user isn't online|user is not online/i.test(errorText)) {
  // Каждая попытка тратит запрос к EulerStream, поэтому при
  // выключенном эфире пауза растёт: 30с, 1м, 2м, 4м, 8м, 10м.
  consecutiveFailures = Math.min(consecutiveFailures + 1, 10);

  const offlineDelay = Math.min(
    30 * 1000 * Math.pow(2, consecutiveFailures - 1),
    2 * 60 * 1000
  );

  console.log(
    "TikTok user is offline. Next attempt in",
    Math.round(offlineDelay / 1000),
    "seconds."
  );

  if (!reconnectTimer) {
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null;
      startTikTok();
    }, offlineDelay);
  }

  return;
}


scheduleReconnect("connection failed");

});

  } catch (error) {
    connecting = false;
    console.error("TikTok connector error:", error.stack || error.message);
    scheduleReconnect("connector error");
  }
}
