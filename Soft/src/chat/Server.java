package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        for(Map.Entry<String, Connection> map : connectionMap.entrySet()){
            try {
                map.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не было отправлено.");
            }
        }
    }
    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket){
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            String s = null;
            while (true){
            connection.send(new Message(MessageType.NAME_REQUEST));
            Message receive = connection.receive();
            if(receive.getType()!=(MessageType.USER_NAME) || connectionMap.containsKey(receive.getData())) continue;
            else if(receive.getType().equals(MessageType.USER_NAME)&&!receive.getData().equals(null)&&!receive.getData().equals("")){
                if(!connectionMap.containsKey(receive.getData())) connectionMap.put(receive.getData(), connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                ConsoleHelper.writeMessage(receive.getData() + " принято.");
                s = receive.getData();
                break;
            }
            }
            return s;

        }

        private  void notifyUsers(Connection connection, String userName) throws IOException{
            for(Map.Entry<String, Connection> map : connectionMap.entrySet()){
                if(!userName.equals(map.getKey())){
                connection.send(new Message(MessageType.USER_ADDED, map.getKey()));}
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws  IOException, ClassNotFoundException{
            while (true){
            Message receive = connection.receive();
            if(receive.getType()==MessageType.TEXT){
                Message text = new Message(MessageType.TEXT, userName + ": " + receive.getData());
                sendBroadcastMessage(text);
            }
            else ConsoleHelper.writeMessage("Ошибка");}
        }

        public void run(){
            System.out.println("Установлено новое соединение с адресом " + socket.getRemoteSocketAddress() );
            try {
                Connection connection = new Connection(socket);
                String userName = serverHandshake(connection);
                sendBroadcastMessage(new Message (MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                ConsoleHelper.writeMessage("Соединение с удаленным сервером закрыто.");
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом.");
            }

        }
    }
    public static void main(String[] args) throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())){
            ConsoleHelper.writeMessage("Сервер запущен.");
            while (true){
                Handler handler = new Handler(serverSocket.accept());
                handler.start();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
