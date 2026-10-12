
const express = require("express");
const http = require("http");
const WebSocket = require("ws");
const {
  TikTokLiveConnection,
  WebcastEvent
} = require("tiktok-live-connector");

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({
  server,
  path: "/events"
});

const PORT = process.env.PORT || 3000;
const username = (process.env.TIKTOK_USERNAME || "vinnipyh99")
  .replace(/^@/, "");

app.get("/", (req, res) => {
  res.json({
    app: "LiveGiftStudio",
    status: "running",
    tiktok: username
  });
});

app.get("/health", (req, res) => {
  res.json({ status: "ok" });
});

function sendEvent(event) {
  const message = JSON.stringify(event);

  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });

  console.log("Event:", message);
}

wss.on("connection", (socket) => {
  socket.send(JSON.stringify({
    type: "connected",
    message: "LiveGiftStudio connected"
  }));
});

const connection = new TikTokLiveConnection(username);

connection.on(WebcastEvent.GIFT, (data) => {
  sendEvent({
    type: "gift",
    username: data?.uniqueId || data?.nickname || "Зритель",
    giftName: data?.giftDetails?.giftName || "Подарок",
    repeatCount: data?.repeatCount || 1,
    timestamp: Date.now()
  });
});

connection.on(WebcastEvent.CHAT, (data) => {
  sendEvent({
    type: "chat",
    username: data?.uniqueId || "Зритель",
    comment: data?.comment || "",
    timestamp: Date.now()
  });
});

connection.connect()
  .then(() => {
    console.log("Connected to TikTok LIVE:", username);
  })
  .catch((error) => {
    console.error("TikTok connection failed:", error.message);
  });

server.listen(PORT, "0.0.0.0", () => {
  console.log("LiveGiftStudio server listening on", PORT);
});
