
package chatClient;

import chatServer.SnakesAndLadders;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import java.net.SocketException;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatClient {

    private final String chatServerName;
    private final int chatServerPort;
    public String username;
    private String password;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    private Socket socket;
    private OutputStream serverOutPut;
    private InputStream serverInPut;
    private BufferedReader bufferedIn;
    // Allows for multiple listeners at once
    private ArrayList<chatClient.LoginStatusListener> loginStatusListeners = new ArrayList<>();
    // Message listener
    private ArrayList<chatClient.MessageListener> messageListeners = new ArrayList<>();
    private ArrayList<chatClient.SLListener> slListeners = new ArrayList<>();

    /**
     * Instantiates a new Chat client.
     *
     * @param chatServerName
     * @param chatServerPort
     */
    public ChatClient(String chatServerName, int chatServerPort) {
        this.chatServerName = chatServerName;
        this.chatServerPort = chatServerPort;
    }

    public boolean isConnected() {
        return (this.socket.isClosed());
    }

    /**
     * Sends a message to the server to Join this client to a group.
     *
     * @param group the group
     * @throws IOException the io exception
     */
    public void joinGroup(String group) throws IOException {

        String command = "join " + group + "\n";
        serverOutPut.write(command.getBytes());
    }

    public void leaveGroup(String recipient) throws IOException {

        String command = "leave " + recipient + "\n";
        //sending the message to the server
        serverOutPut.write(command.getBytes());
    }

    public void msg(String recipient, String messageBody) throws IOException {

        String command = "msg " + recipient + " " + messageBody + "\n";
        //sending the message to the server
        serverOutPut.write(command.getBytes());
    }

    public void choiceMadeInGUI(String username, String choiceOfUser) throws IOException {

        String command = "rpsChoiceFromGui " + username + " " + choiceOfUser + "\n";
        //sending the message to the server
        serverOutPut.write(command.getBytes());

    }

    public void SLPlayer(String username, String move, int steps) throws IOException {

        String command = "SLPlayer " + username + " " + move + " " + steps + "\n";
        serverOutPut.write(command.getBytes());
    }

    public String newUser(String username, String password) throws IOException {

        String command = "user " + username + " " + password + "\n";

        //sendiong the login command to the server
        serverOutPut.write(command.getBytes());

        //We want to be able to read in what the server sends back to us,
        //Buffered reader is used for this
        String reply = bufferedIn.readLine();

        //reply is the message reply from the server
        System.out.println("Server reply :" + reply);

        if (reply.equalsIgnoreCase("userAlreadyExists")) {
            return "userAlreadyExists";
        }

        else if (reply.equalsIgnoreCase("newUser")) {
            return "newUser";
        }

        else{
            return "failedDatabaseConnection";
        }
    }

    public void saveMessage(String userName, String message) throws IOException {


        String command = "saveMessage " + userName + " " + message + "\n";

        //Sending the login command to the server
        serverOutPut.write(command.getBytes());

        //            //We want to be able to read in what the server sends back to us,
//            //Buffered reader is used for this
//
//            String reply;
//
//            reply = bufferedIn.readLine();
//            //reply is the message reply from the server
//            System.out.println("Server reply :" + reply);
//
//
//            if ("saved".equalsIgnoreCase(reply)) {
//                return "saved";
//            }
//
//            else{
//                return "failedDatabaseConnection";
//            }
//        return "didnt";
    }


    public String login(String userName, String password) throws IOException {

        String command = "login " + userName + " " + password + "\n";

        //sending the login command to the server
        serverOutPut.write(command.getBytes());

        //We want to be able to read in what the server sends back to us,
        //Buffered reader is used for this
        String reply = bufferedIn.readLine();

        //reply is the message reply from the server
        System.out.println("Server reply :" + reply);

        //if the reply is login, the user has successfully logged in.
        //this is message is sent from the chatServerWorker handelLogin() method
        //we must always return a message otherwise the GUI will freeze/hang
        if ("login".equalsIgnoreCase(reply)) {

            //now that the user is logged in we want to read messages from the server
            startMessageReader();

            return "login";

        }
        else if ("alreadyOnline".equalsIgnoreCase(reply)) {
            return "alreadyOnline";

        }
        else if ("incorrectpassword".equalsIgnoreCase(reply)) {
            return "incorrectpassword";

        }
        else if ("incorrectusername".equalsIgnoreCase(reply)) {
            return "incorrectusername";

        }
        else{
            return "failedDatabaseConnection";
        }
    }

    public void logoff() throws IOException {

        String command = "logoff \n";

        //sending the login command to the server
        serverOutPut.write(command.getBytes());


    }

    public void startMessageReader() {
        Thread threaaaad = new Thread() {
            @Override
            public void run() {
                readingMessageLoop();
            }
        };
        threaaaad.start();
    }

    /**
     * reading message loops, multiple readers causing conflict
     * Melissa logout database logic (reading in the message in the wrong place)
     */
    public void readingMessageLoop() {

        try {
            String line;

            while ((line = bufferedIn.readLine()) != null) {

                //This will split the input of the user into something more workable
                String[] tokens = StringUtils.split(line);

                //the first token is a command
                if (tokens != null && tokens.length > 0) {

                    String command = tokens[0];

                    //must handle online, and other tokens

                    if ("online".equalsIgnoreCase(command)) {
                        handleOnline(tokens);
                    }
                    else if ("offline".equalsIgnoreCase(command)) {

                        handleOffline(tokens);

                        //When the client receives this we want to send a special message
                    }
                    else if ("SLPlayer".equalsIgnoreCase(command)) {
                        handleSL(tokens);
                    }
                    else{

                        String[] messageTokens = StringUtils.split(line, null, 3);

                        //the 3 parts of this will be msg sender group messageContent

                        handleMessage(messageTokens);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void handleMessage(String[] tokens) {

        String login = tokens[1]; //server
        String messageBody = tokens[2]; //win lose message

        for (MessageListener listener : messageListeners) {
            listener.onMessage(login, messageBody);
        }
    }

    public void handleOffline(String[] tokens) {
        String login = tokens[1];

        for (LoginStatusListener listener : loginStatusListeners) {
            listener.offline(login);
        }
    }

    public void handleOnline(String[] tokens) {
        String login = tokens[1];

        for (LoginStatusListener listener : loginStatusListeners) {
            listener.online(login);
        }
    }

    public void handleSL(String[] tokens) {

        String login = tokens[1];
        String body = tokens[2];
        String roll = tokens[3];
        int steps = roll.charAt(0);


        for (SLListener listener : slListeners) {
            listener.onTurn(login, body, steps);
        }
    }
    public Socket getSocket() {
        return socket;
    }

    /**
     * Boolean allows us to be able understand if there is or isnt a
     * connection better.
     *
     * @return
     */
    public boolean connect() {

        try {
            setSocket(new Socket(chatServerName, chatServerPort));

            //This is the chat clients port
            System.out.println("Chat Clients port is " + getSocket().getLocalPort());

            //access to chat server outputstream
            this.serverOutPut = socket.getOutputStream();

            //access to chat server input
            this.serverInPut = socket.getInputStream();

            //creating buffered reader
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverInPut));

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Add listeners
     *
     * @param listener
     */
    public void addSLListener(SLListener listener) {

        slListeners.add(listener);
    }

    /**
     * Add listeners
     *
     * @param listener
     */
    public void addLoginStatusListener(LoginStatusListener listener) {

        loginStatusListeners.add(listener);
    }

    /**
     * Add listeners
     *
     * @param listener
     */
    public void addMessageListener(MessageListener listener) {

        messageListeners.add(listener);
    }
}