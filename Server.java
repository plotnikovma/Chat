import javax.swing.plaf.multi.MultiSeparatorUI;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;

public class Server {
    private static Map<String,Connection> connectionMap = new ConcurrentHashMap<String,Connection>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Server start");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Handler(clientSocket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void sendBroadcastMessage(Message message) {
        Server.connectionMap.forEach((name,connection) -> {
            try {
                connection.send(message);
            } catch (IOException ex) {
                try {
                    connection.send(new Message(MessageType.TEXT,"Failed to send Message: " + ex.getMessage()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static class Handler extends Thread { //реализует протокол общения
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String remoteUserName = null;
            ConsoleHelper.writeMessage("Connection established with " + socket.getRemoteSocketAddress());

            try (Connection connection = new Connection(this.socket)) {
                //notifications for all users about new remoteUser (step1)
                remoteUserName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,remoteUserName));
                //notifications new remoteUser about all users (step2)
                notifyUsers(connection,remoteUserName);
                //(step3)
                serverMainLoop(connection,remoteUserName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("ERROR was detected while communicating with the server");
            } finally {
                if (remoteUserName != null) {
                    connectionMap.remove(remoteUserName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, remoteUserName));
                    ConsoleHelper.writeMessage("Connection close with: " + remoteUserName);
                }
            }

        }

        //step1: рукопажатие
        private String serverHandshake(Connection connection) throws IOException,ClassNotFoundException { //step1: рукопажатие
            String userName = null;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if (message.getType().equals(MessageType.USER_NAME)) {
                    userName = message.getData();
                    if (!userName.isEmpty() && !connectionMap.containsKey(userName)) {
                        connectionMap.put(userName, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        break;
                    }
                }
            }
            return userName;
        }

        //step2: информирование клиента одругих участниках
        private void notifyUsers(Connection connection, String userName) {
            Server.connectionMap.forEach((name,nameConnection) ->{
                if (!name.equalsIgnoreCase(userName)){
                    try {
                        connection.send(new Message(MessageType.USER_ADDED,name));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        //step2: обработка сообщений сервером
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while(true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    Server.sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("ERROR: message type is not \"TEXT\"");
                }
            }
        }
    }
}
