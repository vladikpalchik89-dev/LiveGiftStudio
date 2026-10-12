
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
// В Render нужно задать секретную переменную TEST_EVENT_KEY.
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
      client.send(message);
      delivered++;
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
