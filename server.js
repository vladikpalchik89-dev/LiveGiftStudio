
const express = require("express");
const http = require("http");
const WebSocket = require("ws");

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: "/events" });

const PORT = process.env.PORT || 3000;
const username = (process.env.TIKTOK_USERNAME || "vinnipyh99").replace(/^@/, "");

app.get("/", (_req, res) => {
  res.json({ app: "LiveGiftStudio", status: "running", tiktok: username });
});

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

function sendEvent(event) {
  const message = JSON.stringify(event);
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) client.send(message);
  });
}

wss.on("connection", (socket) => {
  socket.send(JSON.stringify({ type: "connected" }));
});

server.listen(PORT, "0.0.0.0", () => {
  console.log("LiveGiftStudio server listening on", PORT);
  try {
    const connector = require("tiktok-live-connector");
    const TikTokLiveConnection = connector.TikTokLiveConnection;
    const WebcastEvent = connector.WebcastEvent || {};

    if (!TikTokLiveConnection) {
      throw new Error("TikTokLiveConnection not found");
    }

    const connection = new TikTokLiveConnection(username);

    connection.on(WebcastEvent.GIFT || "gift", (data) => {
      sendEvent({
        type: "gift",
        username: data?.uniqueId || "Зритель",
        giftName: data?.giftDetails?.giftName || "Подарок",
        repeatCount: data?.repeatCount || 1,
        timestamp: Date.now()
      });
    });

    connection.connect()
      .then(() => console.log("TikTok LIVE connected"))
      .catch((error) => console.error("TikTok connection failed:", error.message));
  } catch (error) {
    console.error("TikTok connector error:", error.message);
  }
});
