import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message == null) return;
            SimpleDateFormat dateFormat;
            if (message.split(": ").length != 2) return;
            String user = message.split(": ")[0];
            String text = message.split(": ")[1].trim();
            switch (text) {
                case "дата":
                    dateFormat = new SimpleDateFormat("d.MM.YYYY",Locale.ENGLISH);
                    break;
                case "день":
                    dateFormat = new SimpleDateFormat("d",Locale.ENGLISH);
                    break;
                case "месяц":
                    dateFormat = new SimpleDateFormat("MMMM",Locale.ENGLISH);
                    break;
                case "год":
                    dateFormat = new SimpleDateFormat("YYYY",Locale.ENGLISH);
                    break;
                case "время":
                    dateFormat = new SimpleDateFormat("H:mm:ss",Locale.ENGLISH);
                    break;
                case "час":
                    dateFormat = new SimpleDateFormat("H",Locale.ENGLISH);
                    break;
                case "минуты":
                    dateFormat = new SimpleDateFormat("m",Locale.ENGLISH);
                    break;
                case "секунды":
                    dateFormat = new SimpleDateFormat("s",Locale.ENGLISH);
                    break;
                default:
                    return;
            }
            Date date = Calendar.getInstance().getTime();
            sendTextMessage(String.format("Информация для %s: %s", user,dateFormat.format(date)));
        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }
}
