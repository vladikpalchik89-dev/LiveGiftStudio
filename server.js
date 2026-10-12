
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
).replace(/^@/, "");

app.get("/", (_req, res) => {
  res.json({
    app: "LiveGiftStudio",
    status: "running",
    tiktok: username
  });
});

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

function sendEvent(event) {
  const message = JSON.stringify(event);

  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });

  console.log("TikTok event:", message);
}

wss.on("connection", (socket) => {
  socket.send(JSON.stringify({
    type: "connected",
    message: "LiveGiftStudio connected"
  }));
});

server.listen(PORT, "0.0.0.0", () => {
  console.log("LiveGiftStudio server listening on", PORT);
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

    const connection = new TikTokLiveConnection(username, {
      processInitialData: false
    });

    connection.on(
      WebcastEvent.GIFT || "gift",
      (data) => {
        sendEvent({
          type: "gift",
          username: data?.uniqueId ||
            data?.nickname || "Зритель",
          giftName: data?.giftDetails?.giftName ||
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
          "TikTok LIVE connected:", username
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
