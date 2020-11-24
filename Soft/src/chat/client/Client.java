package chat.client;


import chat.Connection;
import chat.ConsoleHelper;
import chat.Message;
import chat.MessageType;

import java.io.IOException;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;
    public class SocketThread extends Thread{

        protected void  processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType()== MessageType.NAME_REQUEST)
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                else if (message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;}
                else
                    throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true){
                Message message = connection.receive();
                if(message.getType()==MessageType.TEXT) processIncomingMessage(message.getData());
                else if (message.getType()==MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
                else if (message.getType()==MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
                else throw new IOException("Unexpected MessageType");
            }
        }

        public void run(){
            try {
                java.net.Socket socket = new java.net.Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }
        }
    }

    protected String getServerAddress() throws IOException {
        return ConsoleHelper.readString();
    }

    protected int getServerPort() throws IOException {

        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT ,text));
        } catch (IOException e) {
            e.printStackTrace();
            clientConnected = false;
        }
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
            try {
                synchronized (this){
                wait();}
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;}
                if(clientConnected){
                    System.out.println("Соединение установлено.");
                    System.out.println("Для выхода наберите команду 'exit'.");

                }
                else{
                    System.out.println("Произошла ошибка во время работы клиента.");
                }
                while (clientConnected){
                    String console = ConsoleHelper.readString();
                    if(console.equals("exit")) break;
                    if(shouldSendTextFromConsole()) sendTextMessage(console);
                }



    }

    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }
}
