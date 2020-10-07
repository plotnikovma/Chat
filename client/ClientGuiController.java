public class ClientGuiController extends Client {
    private ClientGuiModel model = new ClientGuiModel();
    private ClientGuiView view = new ClientGuiView(this);

    public static void main(String[] args) {
        ClientGuiController clientGuiController = new ClientGuiController();
        clientGuiController.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    @Override
    public void run() {
        getSocketThread().run();
    }

    @Override
    protected String getServerAddress() {
        return ClientGuiController.this.view.getServerAddress();
    }

    @Override
    protected int getServerPort() {
        return ClientGuiController.this.view.getServerPort();
    }

    @Override
    protected String getUserName() {
        return ClientGuiController.this.view.getUserName();
    }

    public ClientGuiModel getModel() {
        return ClientGuiController.this.model;
    }

    public class GuiSocketThread extends SocketThread {
        @Override
        protected void processIncomingMessage(String message) {
            ClientGuiController.this.model.setNewMessage(message);
            ClientGuiController.this.view.refreshMessages();
        }

        @Override
        protected void informAboutAddingNewUser(String userName) {
            ClientGuiController.this.model.addUser(userName);
            ClientGuiController.this.view.refreshUsers();
        }

        @Override
        protected void informAboutDeletingNewUser(String userName) {
            ClientGuiController.this.model.deleteUser(userName);
            ClientGuiController.this.view.refreshUsers();
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            ClientGuiController.this.view.notifyConnectionStatusChanged(clientConnected);
        }
    }
}
