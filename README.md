# Similar Images Bot

Бот запускается командой "docker compose up"

Перед запуском отредактировать compose.yaml:
      BOT_TOKEN -- токен бота, полученный от BotFather
      ADMIN_ID -- Telegram ID админа
      AUTO_REMOVE -- Если true, то автоматически удалять одинаковые картинки из канала
      MODE -- Если 'once', то только обрабатывать текущие сообщения в канале и выходить; иначе запускаться и ждать новых сообщений

В файле BotHandler.java в строке 152 можно поменять константу, определяющую порог срабатывания алгоритма поиска похожих картинок. Сейчас установлен 5.0

