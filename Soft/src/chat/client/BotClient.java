package chat.client;



import chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client{
    public class BotSocketThread extends SocketThread{
        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if(message!=null && message.contains(":")){
            String userName = message.substring(0, message.indexOf(":"));
            String text = message.substring(message.indexOf(":")+2);
            SimpleDateFormat dateFormat;
            if(text.equals("дата")) dateFormat = new SimpleDateFormat("d.MM.YYYY");
            else if(text.equals("день")) dateFormat = new SimpleDateFormat("d");
            else if(text.equals("месяц")) dateFormat = new SimpleDateFormat("MMMM");
            else if(text.equals("год")) dateFormat = new SimpleDateFormat("YYYY");
            else if(text.equals("время")) dateFormat = new SimpleDateFormat("H:mm:ss");
            else if(text.equals("час")) dateFormat = new SimpleDateFormat("H");
            else if(text.equals("минуты")) dateFormat = new SimpleDateFormat("m");
            else if(text.equals("секунды")) dateFormat = new SimpleDateFormat("s");
            else dateFormat = null;
            if(dateFormat!=null){
                sendTextMessage("Информация для " + userName+ ": " + dateFormat.format(Calendar.getInstance().getTime()));
            }}


        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }

    @Override
    protected String getUserName() {
        return "date_bot_"+ (int) (Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public static void main(String[] args){
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
