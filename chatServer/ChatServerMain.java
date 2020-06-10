package chatServer;


/**
 * This starts the server with a port 4444
 * @author Sami Cass Darweish
 */
public class ChatServerMain {

    /**
     * This is a main method that runs the server
     * @param args
     */
    public static void main(String[] args){

        int port = 4444;

        ChatServer server = new ChatServer(port);

        //This starts our server which is effectively a
        //collection of chatServerWorkers threads
        server.start();
    }
}
