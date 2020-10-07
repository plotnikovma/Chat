public class Message implements Serializable {
    //Данные будут обрабатываться на разных JVM, для того чтобы передать объект из одной JVM  в другую его надо сериализовать.
    //for message
    private final MessageType type;
    private final String data;

    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
