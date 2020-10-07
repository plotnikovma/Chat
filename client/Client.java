import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.clientConnected) {
            System.out.println("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            System.out.println("Произошла ошибка во время работы клиента.");
        }
        while (this.clientConnected) {
            String messageText = ConsoleHelper.readString();
            if (messageText.equalsIgnoreCase("exit") || this.clientConnected == false) break;
            if (shouldSendTextFromConsole()) sendTextMessage(messageText);
        }



    }

    public class SocketThread extends Thread {

        @Override
        public void run() {
            try {
                Socket clientSocket = new Socket(getServerAddress(),getServerPort());
                Client.this.connection = new Connection(clientSocket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " is connected");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " is disconnected");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        //step1: рукопожатие
        protected void clientHandshake() throws IOException,ClassNotFoundException {
            while (!Client.this.clientConnected) {
                Message message = Client.this.connection.receive();
                if (message != null) {
                    MessageType type = message.getType();
                    if (type == MessageType.NAME_REQUEST) {
                        String userName = getUserName();
                        Client.this.connection.send(new Message(MessageType.USER_NAME, userName));
                    } else if (type == MessageType.NAME_ACCEPTED) {
                        notifyConnectionStatusChanged(true);
                        return;
                    } else {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }

        //step2: обработка сообщений сервером
        protected void clientMainLoop() throws IOException,ClassNotFoundException {
            while (true) {
                Message message = Client.this.connection.receive();
                if (message != null) {
                    MessageType type = message.getType();
                    if (type == MessageType.TEXT) {
                        processIncomingMessage(message.getData());
                    } else if (type == MessageType.USER_ADDED) {
                        informAboutAddingNewUser(message.getData());
                    } else if (type == MessageType.USER_REMOVED) {
                        informAboutDeletingNewUser(message.getData());
                    } else {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }
    }

    protected String getServerAddress() {
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            this.connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            this.clientConnected = false;
            e.printStackTrace();
        }
    }
}
