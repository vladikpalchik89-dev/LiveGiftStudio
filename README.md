# LiveGiftStudio

Приложение для стримеров: показывает оверлей с подарками, донатами и подписками
из TikTok LIVE и DonationAlerts прямо поверх других приложений на Android.

Состоит из двух частей:

| Часть | Где | Что делает |
|---|---|---|
| Android-приложение | `app/` | Оверлей, звуки, анимации, настройки. Java, без XML-лейаутов |
| Сервер | `server.js` | Подключается к TikTok LIVE и DonationAlerts, раздаёт события по WebSocket |

## Как это работает

```
TikTok LIVE ──┐
              ├─► server.js (Render) ──WebSocket──► OverlayService (Android) ──► оверлей + звук
DonationAlerts┘
```

Приложение подключается к `wss://<сервер>/events` и получает JSON-события:
`gift`, `donation`, `follow`, `chat`, `stream_end`, `connected`.

## Сборка APK

Сборка идёт только через GitHub Actions — локально Android SDK не нужен.

1. Запушь изменения в `main` (или запусти workflow вручную: **Actions → Build APK → Run workflow**).
2. Дождись зелёной галочки.
3. Скачай APK внизу страницы запуска: **Artifacts → LiveGiftStudio-debug-N**.

### Секреты сборки

Ключи в репозитории не хранятся — они передаются в сборку переменными окружения:

| Переменная | Для чего |
|---|---|
| `LGS_SWITCH_KEY` | ключ для `POST /switch-account` |
| `LGS_ORDERS_KEY` | ключ для Google Apps Script, куда уходят заказы |

В GitHub: **Settings → Secrets and variables → Actions → New repository secret**.
Локально: `LGS_SWITCH_KEY=... LGS_ORDERS_KEY=... gradle assembleDebug`.

Если ключи не заданы, APK всё равно соберётся, но переключение аккаунта и отправка
заказов на сервер сообщат «не настроено в этой сборке».

## Сервер

```bash
npm install
npm start
```

Переменные окружения:

| Переменная | Обязательна | Описание |
|---|---|---|
| `PORT` | нет | Порт HTTP/WebSocket (по умолчанию `3000`) |
| `TIKTOK_ENABLED` | нет | `true`, чтобы включить подключение к TikTok LIVE |
| `TIKTOK_USERNAME` | нет | Аккаунт по умолчанию, без `@` |
| `ACCOUNT_SWITCH_KEY` | нет | Ключ для `POST /switch-account` |
| `TEST_EVENT_KEY` | нет | Ключ для `/test-gift` и `/test-donation` |
| `DONATIONALERTS_TOKEN` | нет | Токен DonationAlerts |

Эндпоинты: `GET /`, `GET /health`, `POST /switch-account`,
`GET /test-gift?key=...`, `GET /test-donation?key=...`, `WS /events`.

## Структура

```
app/src/main/java/com/livegiftstudio/
  MainActivity.java     — весь UI: меню, каталог подарков, звуки, настройки
  OverlayService.java   — foreground-сервис: WebSocket, оверлей, звуки, TTS
app/src/main/res/drawable — картинки подарков (webp)
app/src/main/res/raw      — звуки подарков (wav)
server.js                 — сервер событий
```
