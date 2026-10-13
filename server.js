
const express = require("express");
const http = require("http");
const WebSocket = require("ws");

const app = express();
const server = http.createServer(app);

const wss = new WebSocket.Server({
  server,
  path: "/events"
});

const PORT = process.env.PORT || 3000;

const username = (
  process.env.TIKTOK_USERNAME || "vinnipyh99"
).replace(/^@/, "").trim();

const testKey = process.env.TEST_EVENT_KEY || "";

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
  res.json({
    status: "ok",
    time: new Date().toISOString()
  });
});

// Тестовый подарок.
// Секрет TEST_EVENT_KEY хранится только в Render Environment.
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

  res.json({
    status: "ok",
    message: "Test gift sent",
    event
  });
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
        console.error(
          "WebSocket send failed:",
          error.message
        );
      }
    }
  });

  console.log(
    "TikTok event:",
    message,
    "| WebSocket clients:",
    delivered
  );
}

wss.on("connection", (socket) => {
  console.log("WebSocket client connected");

  socket.send(JSON.stringify({
    type: "connected",
    message: "LiveGiftStudio connected"
  }));

  socket.on("close", () => {
    console.log("WebSocket client disconnected");
  });

  socket.on("error", (error) => {
    console.error("WebSocket error:", error.message);
  });
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(
    "LiveGiftStudio server listening on",
    PORT
  );

  startTikTok();
});

function startTikTok() {
  // Подключение выключено по умолчанию.
  // Включим вручную после восстановления дневного лимита.
  if (process.env.TIKTOK_ENABLED !== "true") {
    console.log(
      "TikTok connection paused. " +
      "Set TIKTOK_ENABLED=true to enable manually."
    );
    return;
  }

  try {
    const connector = require("tiktok-live-connector");
    const TikTokLiveConnection =
      connector.TikTokLiveConnection;
    const WebcastEvent = connector.WebcastEvent || {};

    if (typeof TikTokLiveConnection !== "function") {
      throw new Error(
        "TikTokLiveConnection not found in connector"
      );
    }

    const connection = new TikTokLiveConnection(
      username,
      { processInitialData: false }
    );

    connection.on(
      WebcastEvent.GIFT || "gift",
      (data) => {
        sendEvent({
          type: "gift",
          username:
            data?.uniqueId ||
            data?.nickname ||
            "Зритель",
          giftName:
            data?.giftDetails?.giftName ||
            data?.giftName ||
            "Подарок",
          repeatCount: data?.repeatCount || 1,
          timestamp: Date.now()
        });
      }
    );

    connection.on(
      WebcastEvent.CHAT || "chat",
      (data) => {
        sendEvent({
          type: "chat",
          username: data?.uniqueId || "Зритель",
          comment: data?.comment || "",
          timestamp: Date.now()
        });
      }
    );

    connection.connect()
      .then(() => {
        console.log(
          "TikTok LIVE connected:",
          username
        );
      })
      .catch((error) => {
        console.error(
          "TikTok connection failed:",
          error.stack || error.message
        );
      });
  } catch (error) {
    console.error(
      "TikTok connector error:",
      error.stack || error.message
    );
  }
}
