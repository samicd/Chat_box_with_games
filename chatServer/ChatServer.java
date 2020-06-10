package chatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 *  This class extends Thread allowing a chatServer to be run. Such that when anyone connects to
 *  the server a chat server worker is instantiated and added the Chat Server List (CSWorkerList).
 *  Once this is complete the ChatServerWorker is started. Allowing it to start reading messages.
 */
public class ChatServer extends Thread{

    //Server Port
    private final int chatServerPort;

    //ConcurrentLinkedQueue of chatServerWorkers, this is concurrent to attempt to prevent synchronisation issues.
    private ConcurrentLinkedQueue<chatServerWorker> CSWorkerList = new ConcurrentLinkedQueue<>();


    /**
     * ChatServerPort constructor, only takes one argument. The port at which to create a socket.
     * The chatServerPort is of type int.
     * @param chatServerPort
     */
    public ChatServer(int chatServerPort) {
        this.chatServerPort = chatServerPort;
    }

    @Override
    /*
      This run method assigns the serverSocket as the ServerPort.
      As well as when accepting a new connection creating a new ChatServerWorker.
     */
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(chatServerPort);

            while(true) {

                // Debug Statement: Lets us know socket assigned correctly and ready for client connections to the
                // server.
                System.out.println("About to accept new client connections!");

                Socket clientSocket = serverSocket.accept();

                // This prints out the clientSocket when a connection is made.
                System.out.println("Accepting client connections from" + clientSocket);

                // Creates a new thread, that will contain the running of the method once a client is connected!
                // This allows for multiple clients by parsing the instance of the server through, this will allow the
                // threads to communicate
                chatServerWorker workingWorker = new chatServerWorker(this, clientSocket);

                // Add a new worker to the worker array list
                CSWorkerList.add(workingWorker);

                //Starts ServerWorker
                workingWorker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a chatServerWorker from the list of workers.
     * @param chatServerWorker
     */
    public void removeChatWorker(chatServerWorker chatServerWorker) {
        getCSWorkerList().remove(chatServerWorker);
    }


    /**
     * Getter for the linkedQueue of Chat Server Workers
     * @return
     */
    public ConcurrentLinkedQueue getCSWorkerList(){
        return CSWorkerList;
    }
}
