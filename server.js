
const express = require("express");
const http = require("http");
const WebSocket = require("ws");

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: "/events" });

const PORT = process.env.PORT || 3000;
const username = (process.env.TIKTOK_USERNAME || "vinnipyh99")
  .replace(/^@/, "")
  .trim();
const testKey = process.env.TEST_EVENT_KEY || "";

let currentConnection = null;
let reconnectTimer = null;
let connecting = false;
let stopped = false;

app.get("/", (_req, res) => {
  res.json({
    app: "LiveGiftStudio",
    status: "running",
    tiktok: username,
    tiktokEnabled: process.env.TIKTOK_ENABLED === "true",
    websocket: "/events"
  });
});

app.get("/health", (_req, res) => {
  res.json({ status: "ok", time: new Date().toISOString() });
});

app.get("/test-gift", (req, res) => {
  if (!testKey) {
    return res.status(503).json({
      status: "error",
      message: "TEST_EVENT_KEY is not configured"
    });
  }

  if (req.query.key !== testKey) {
    return res.status(403).json({
      status: "error",
      message: "Invalid test key"
    });
  }

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

function sendEvent(event) {
  const message = JSON.stringify(event);
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

  console.log("TikTok event:", message, "| WebSocket clients:", delivered);
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

function getGiftName(data) {
  return data?.giftDetails?.giftName ||
    data?.extendedGiftInfo?.name ||
    data?.giftName ||
    data?.gift?.name ||
    (data?.giftId ? "Подарок " + data.giftId : "Подарок");
}

wss.on("connection", (socket) => {
  console.log("WebSocket client connected");

  socket.send(JSON.stringify({
    type: "connected",
    message: "LiveGiftStudio connected"
  }));

  socket.on("close", () => console.log("WebSocket client disconnected"));
  socket.on("error", (error) => console.error("WebSocket error:", error.message));
});

server.listen(PORT, "0.0.0.0", () => {
  console.log("LiveGiftStudio server listening on", PORT);
  startTikTok();
});

function scheduleReconnect(reason) {
  if (stopped || process.env.TIKTOK_ENABLED !== "true") return;
  if (reconnectTimer) return;

  console.log("TikTok reconnect scheduled:", reason);

  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    startTikTok();
  }, 25000);
}

function startTikTok() {
  if (stopped || process.env.TIKTOK_ENABLED !== "true") {
    console.log("TikTok connection paused. Set TIKTOK_ENABLED=true to enable.");
    return;
  }

  if (connecting) return;
  connecting = true;

  try {
    const connector = require("tiktok-live-connector");
    const TikTokLiveConnection = connector.TikTokLiveConnection;
    const WebcastEvent = connector.WebcastEvent || {};
    const ControlEvent = connector.ControlEvent || {};

    if (typeof TikTokLiveConnection !== "function") {
      throw new Error("TikTokLiveConnection not found in connector");
    }

    const connection = new TikTokLiveConnection(username, {
      processInitialData: false
    });

    currentConnection = connection;

    connection.on(WebcastEvent.GIFT || "gift", (data) => {
      if (connection !== currentConnection) return;

      sendEvent({
        type: "gift",
        username: getUserName(data),
        giftName: getGiftName(data),
        repeatCount: data?.repeatCount || 1,
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
        fields: Object.keys(data || {})
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
      scheduleReconnect(reason || "disconnected");
    };

    connection.on(
      ControlEvent.DISCONNECTED || "disconnected",
      () => handleDisconnect("disconnected")
    );

    connection.on(
      WebcastEvent.STREAM_END || "streamEnd",
      () => handleDisconnect("stream ended")
    );

    connection.connect()
      .then(() => {
        if (connection !== currentConnection) return;
        connecting = false;
        console.log("TikTok LIVE connected:", username);
      })
      .catch((error) => {
        if (connection !== currentConnection) return;
        connecting = false;
        console.error(
          "TikTok connection failed:",
          error.stack || error.message
        );
        scheduleReconnect("connection failed");
      });
  } catch (error) {
    connecting = false;
    console.error("TikTok connector error:", error.stack || error.message);
    scheduleReconnect("connector error");
  }
}
