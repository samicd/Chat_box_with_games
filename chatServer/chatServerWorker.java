package chatServer;

import org.apache.commons.lang3.StringUtils;
import Login.DatabaseConnection;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * chatServerWorker(CSW) extends Thread such that a CSW can be run. This server worker will contain the methods that will
 * read in messages coming from the client and respond with suitable messages. This class contains methods:
 *          - public chatServerWorker(chatServer.ChatServer serverName, Socket clientSocket) that is a constructor.
 *          - public void run()
 *          - public void chatHandleClientSocket() this starts a loop 'listening' for messages sent to the server.
 *          - private void handleLeave(String[] tokens) this handles a client leaving a group.
 *          - public boolean isMemberOfGroup(String group) simple boolean checking membership.
 *          - private void handleJoin(String[] tokens) handles a client joining a group.
 *          - private void handleMessage(String[] tokens) handles sending messages.
 *          - private void handleNewUser(OutputStream outputStreamOfClient, String[] tokens) handles a new user making an account.
 *          - public String getLogin() gets login
 *          - private void handleLogin(OutputStream outputStreamOfClient, String[] tokens)
 *          - public HashSet<String> getGroupSet() {
 *          - private void send(String message) throws IOException
 */

public class chatServerWorker extends Thread {

    private int score;
    private String choiceFromRPS;
    private chatServerWorker currentPlayer;
    public chatServerWorker opponent;
    private final Socket clientSocket;
    private final chatServer.ChatServer chatServer;
    private String login = null;
    private OutputStream outputStreamOfClient;
    private InputStream inputStreamOfClient;
    private HashSet<String> groupSet = new HashSet<>();
    static boolean yourTurn = false;
    private Scanner input;
    private PrintWriter output;
    int[] position = {0, 9};
    private char mark;

    /**
     * The constructor for a chatServerWorker
     *
     * @param serverName
     * @param clientSocket
     */
    public chatServerWorker(chatServer.ChatServer serverName, Socket clientSocket) {
        this.chatServer = serverName;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            chatHandleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method currently starts the listening process server side. Messages starting with particular keywords or
     * commands will cause different actions to take place mostly through a set of handlers.
     *
     * @throws IOException
     */
    public void chatHandleClientSocket() throws IOException {

        // Gets the input stream of the client
        inputStreamOfClient = clientSocket.getInputStream();

        // Gets the output stream of the client
        outputStreamOfClient = clientSocket.getOutputStream();

        // Starts a buffered reader to allow for effective reading of inputStreamOfClient.
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamOfClient));
        //PrintWriter out = new PrintWriter(outputStreamOfClient, true);

        String line;

        // This reads commands from the client.
        while ((line = reader.readLine()) != null) {

            // This will split the input of the user into something more workable.
            String[] tokens = StringUtils.split(line);

            // The first token is a command, this tells the server what action to take.
            if (tokens != null && tokens.length > 0) {

                String command = tokens[0];

                // If the command is "logoff" the handleClientLogoff handler is called.
                if ("logoff".equalsIgnoreCase(command)) {

                    handleClientLogoff();
                    break;
                }

                // If the command is "login" the handleLogin handler is called.
                else if ("login".equalsIgnoreCase(command)) {

                    handleLogin(outputStreamOfClient, tokens);
                }
                else if("saveMessage".equalsIgnoreCase(command)) {

                    String[] saveMessageTokens = StringUtils.split(line, null, 3);
            			
                }
                // If the command is "user" the handleNewUser handler is called.
                else if ("user".equalsIgnoreCase(command)) {

                    handleNewUser(outputStreamOfClient, tokens);
                }

                // If the command is "msg" the handleMessage handler is called.
                else if ("msg".equalsIgnoreCase(command)) {

                    //This splits the incoming String into 3 parts, making it readable by handleMessage()
                    String[] messageTokens = StringUtils.split(line, null, 3);

                    handleMessage(messageTokens);
                }

                // If the command is "join" the handleJoin handler is called.
                else if ("join".equalsIgnoreCase(command)) {

                    handleJoin(tokens);
                }

                // If the command is "leave" the handleLeave handler is called.
                else if ("leave".equalsIgnoreCase(command)) {

                    handleLeave(tokens);

                    // choice of player being sent from the gui
                } else if ("rpsChoiceFromGui".equalsIgnoreCase(command)) {

                    handleUserChoice(tokens);
                }
                else if ("SLPlayer".equalsIgnoreCase(command)){

                    handleSLTurn(tokens);
                }

                // If the command is unknown the server will send a message to the client saying this command is unknown.
                else {

                    // Message to the client
                    String messageToTheClient = "unknown " + command + "\n";

                    outputStreamOfClient.write(messageToTheClient.getBytes());
                }
            }
        }

        inputStreamOfClient.close();
        outputStreamOfClient.close();
        clientSocket.close();
        System.out.println("closing");
    }

    /**
     * This is for handling and images choice
     * <p>
     * set the choice associated with this chatserverWorker
     * <p>
     * check if the opponents.getChoice is not null
     * <p>
     * if this is the case
     * <p>
     * calculate result
     * <p>
     * send result
     * <p>
     * otherwise
     * <p>
     * send wait
     *
     * @param tokens
     */
    private void handleUserChoice(String[] tokens) throws IOException {
        //TO BE COMPLETE
        String username = tokens[1];
        String choiceOfTheUser = tokens[2];

        String group = "#rockPaperScissor";

        currentPlayer = this;

        // Getting and initialising a the list of workers that represents the list of users online.
        ConcurrentLinkedQueue<chatServerWorker> csWorkerList = chatServer.getCSWorkerList();

        // This loops through the list of ChatServerWorkers for which there is one for every client.
        for (chatServerWorker chatWorker : csWorkerList) {

            // If the chatWorker is a member of the group .
            if (chatWorker.isMemberOfGroup(group)) {

                //error may be caused here the messages arent being read by the readMessageLoop()
                if (!username.equals(chatWorker.getLogin())) {

                    //settting the opponent
                    opponent = chatWorker;

                    //setting the choice in the chatworker current player
                    choiceFromRPS = choiceOfTheUser;

                    //erorr may be caused here
                    if (opponent.choiceFromRPS == null) {
                        //problem with sending


                        //send is executing but the message is not getting to the read meassage loop
                        this.send("msg Server wait for opponent choice\n");
                    } else {

                        if (currentPlayer.choiceFromRPS.startsWith("p") && opponent.choiceFromRPS.startsWith("r") ||
                                currentPlayer.choiceFromRPS.startsWith("r") && opponent.choiceFromRPS.startsWith("s") ||
                                currentPlayer.choiceFromRPS.startsWith("s") && opponent.choiceFromRPS.startsWith("p")) {

                            //increment score of winner

                            this.score += 1;

                            System.out.println(this.score + " " + this.getLogin());

                            if (this.hasWinner()) {
                                //play again
                                System.out.println(currentPlayer.getLogin() + " there is a winner");
                            }

                            currentPlayer.choiceFromRPS = null;
                            opponent.choiceFromRPS = null;

                            //this.setScore(score += 1);
                            String winRound = "msg server result " + currentPlayer.getLogin() + " wins the round\n";

                            String loseRound = "msg server result you lose. " + currentPlayer.getLogin() + " wins the round\n";

                            //problem with sending
                            this.send(winRound);
                            opponent.send(loseRound);

                            // if opponent wins
                        } else if (currentPlayer.choiceFromRPS.startsWith("r") && opponent.choiceFromRPS.startsWith("p") ||
                                currentPlayer.choiceFromRPS.startsWith("s") && opponent.choiceFromRPS.startsWith("r") ||
                                currentPlayer.choiceFromRPS.startsWith("p") && opponent.choiceFromRPS.startsWith("s")) {

                            opponent.score += 1;

                            System.out.println(opponent.score + " " + opponent.getLogin());


                            if (opponent.hasWinner()) {
                                //play again
                                System.out.println(opponent.getLogin() + " there is a winner");
                            }

                            currentPlayer.choiceFromRPS = null;
                            opponent.choiceFromRPS = null;

                            //this.setScore(score += 1);
                            String winRound = "msg server result " + opponent.getLogin() + " wins the round\n";

                            String loseRound = "msg server result you lose. " + opponent.getLogin() + " wins the round\n";

                            //problem with sending

                            this.send(loseRound);
                            opponent.send(winRound);

                            //	if there is a tie.
                        } else if (currentPlayer.choiceFromRPS.startsWith("r") && opponent.choiceFromRPS.startsWith("r") ||
                                currentPlayer.choiceFromRPS.startsWith("p") && opponent.choiceFromRPS.startsWith("p") ||
                                currentPlayer.choiceFromRPS.startsWith("s") && opponent.choiceFromRPS.startsWith("s")) {

                            System.out.println(opponent.score += 1);


                            currentPlayer.choiceFromRPS = null;
                            opponent.choiceFromRPS = null;

                            //play again message

                            String drawRound = "msg server result it was a draw\n";

                            this.send(drawRound);
                            opponent.send(drawRound);
                        }
                    }
                }
            }
        }

        //consider future case in which there is no opoonent
    }

    private void handleSLTurn(String[] tokens) throws IOException {

        String username = tokens[1];
        String group = "#SnakesAndLadders";
        String[] msg = tokens;

        currentPlayer = this;
        //SnakesAndLadders.yourTurn = true;
        this.send("SLPlayer " + this.getLogin() + " MESSAGE " + " Waiting\n");


        // Getting and initialising a the list of workers that represents the list of users online.
        ConcurrentLinkedQueue<chatServerWorker> csWorkerList = chatServer.getCSWorkerList();

        // This loops through the list of ChatServerWorkers for which there is one for every client.
        for (chatServerWorker chatWorker : csWorkerList) {

            // If the chatWorker is a member of the group they will receive a message that the client left the group.
            if (chatWorker.isMemberOfGroup(group)) {

                //error may be caused here the messages aren't being read by the readMessageLoop()
                if (!username.equals(chatWorker.getLogin())) {

                    //setting the opponent
                    opponent = chatWorker;
                    currentPlayer.mark = 'X';
                    opponent.mark = 'O';

                        String move = tokens[2];

                        if (move.equals("QUIT")) {
                            return;

                        } else if (move.equals("MOVE")) {
                            try {
                                String roll = tokens[3];
                                int steps = roll.charAt(0) - 48;
                                SnakesAndLadders.turn(steps,this);
                                this.send("SLPlayer " + this.getLogin() + " VALID_MOVE " + steps + "\n");
                                opponent.send("SLPlayer " + opponent.getLogin() + " OPPONENT_MOVED " + steps + "\n");
                                currentPlayer = currentPlayer.opponent;
                                SnakesAndLadders.yourTurn = false;

                                if (SnakesAndLadders.isWinner(this)) {
                                    this.send("SLPlayer " + this.getLogin() + " VICTORY\n");
                                    opponent.send("SLPlayer " + opponent.getLogin() + " DEFEAT\n");
                                }
                            } catch (IllegalStateException e) {
                                outputStreamOfClient.write(("MESSAGE " + e.getMessage() + "\n").getBytes());
                            }
                        }
                    //}
                }
            }
        }
    }

    /**
     * This will handle a client leaving a group.
     * @param tokens
     * @throws IOException
     */
    private void handleLeave(String[] tokens) throws IOException {

        // Checks that there are enough tokens in the message to read the group the client wants to leave.
        if(tokens.length > 1){

            // The group the client should leave from
            String group = tokens[1];

            // Member of the group will be removed from the set
            groupSet.remove(group);

            // Message to be sent to the client.
            String confirmationMessage = login + " successfully left the " + group + "\n";

            outputStreamOfClient.write(confirmationMessage.getBytes());

            // Getting and initialising a the list of workers that represents the list of users online.
            ConcurrentLinkedQueue<chatServerWorker> csWorkerList = chatServer.getCSWorkerList();

            // This loops through the list of ChatServerWorkers for which there is one for every client.
            for (chatServerWorker chatWorker : csWorkerList) {

                // If the chatWorker is a member of the group they will receive a message that the client left the group.
                if (chatWorker.isMemberOfGroup(group)) {

                    // The message to send out the chatWorker
                    String recievedMessage = login + " left the " + group + "\n";

                    chatWorker.send(recievedMessage);
                }
            }
        }

    }
    /**
		    //PUTTING MESSAGE IN DATABASE
			
		    private void handleSaveMessage(OutputStream outputStream1, String[] tokens) throws IOException {
		
		        String username = tokens[1];
		        String message = tokens[2];
		
		
		
		        try {
		            Connection connection = DatabaseConnection.getConnection();
		
		            if ( connection == null ) {
		                String failedDatabaseConnection = "failedDatabaseConnection\n";
		                outputStream1.write(failedDatabaseConnection.getBytes());
		
		            }
		
		            PreparedStatement userExists = connection.prepareStatement("SELECT username FROM chat_history WHERE username = ?");
		
		            userExists.setString(1, username);
		
		            ResultSet result = userExists.executeQuery();
		
		            if (result.next()) {
		
		                PreparedStatement updateHistory = connection
		                        .prepareStatement("UPDATE chat_history SET messages = ? WHERE username = ?");
		
		                updateHistory.setString(1, message);
		                updateHistory.setString(2, username);
		
		                updateHistory.executeUpdate();
		
		                String saved = "saved " + username + "saved \n";
		                outputStream1.write(saved.getBytes());
		
		                connection.close();
		
		
		
		            } else {
		                PreparedStatement newEntry = connection
		                        .prepareStatement("INSERT INTO chat_history (username, messages) VALUES (?,?)");
		
		                newEntry.setString(1, username);
		                newEntry.setString(2, message);
		
		                newEntry.executeUpdate();
		
		                String saved = "saved " + username + '\n';
		                outputStream1.write(saved.getBytes());
		                connection.close();
		            }
		
		
		
		        } catch (SQLException e1) {
		            String failedDatabaseConnection = "failedDatabaseConnection\n";
		            outputStream1.write(failedDatabaseConnection.getBytes());
		        }
		    }
			*/

    /**
     * Returns a simple boolean if a chatWorker is a member of the group
     * @param group
     * @return boolean
     */
    private boolean isMemberOfGroup(String group){
        return groupSet.contains(group);
    }


    /**
     * This client will be added to a group.
     * @param tokens
     */
    private void handleJoin(String[] tokens) throws IOException {

        // Checks that there are enough tokens in the message to read the group the client wants to leave.
        if (tokens.length > 1) {

            // The group to join
            String group = tokens[1];

            // Membership of the group will be stored in the chatServer.chatServerWorker Instance
            groupSet.add(group);

            // Confirmation Message
            String confirmationMessage = "msg " + login + " group succesfully added to " + group + "\n";

            // Debug Statement
            System.out.println(confirmationMessage);

            outputStreamOfClient.write(confirmationMessage.getBytes());

            // Getting and initialising a the list of workers that represents the list of users online.
            ConcurrentLinkedQueue<chatServerWorker> csWorkerList = chatServer.getCSWorkerList();

            // This loops through the list of ChatServerWorkers for which there is one for every client.
            for (chatServerWorker chatWorker : csWorkerList) {

                // If the chatWorker is a member of the group they will receive a message that the client joined.
                if (chatWorker.isMemberOfGroup(group)) {

                    // Joining message to all members
                    String recievedMessage = "msg " + login + " group joined the group " + group + "\n";
                    chatWorker.send(recievedMessage);
                }
            }
        }
    }

    /**
     * Input WILL follow the format of command + receiver  + "messageBody". Where the command is msg, to indicate message.
     * Receiver is the user or group the message is being sent to, which is followed by the message body.
     *
     * @param tokens
     */
    private void handleMessage(String[] tokens) throws IOException {

        // Receiver string either group or login.
        String reciever = tokens[1];

        // The message body ro be sent out.
        String messageContent = tokens[2];

        // Checks if the first character is #
        boolean isGroup = reciever.charAt(0) == '#';

        // Getting and initialising a the list of workers that represents the list of users online.
        ConcurrentLinkedQueue<chatServerWorker>  csWorkerList = chatServer.getCSWorkerList();

        // This loops through the list of ChatServerWorkers for which there is one for every client.
        for(chatServerWorker chatWorker : csWorkerList) {

            // If the message is for a group
            if (isGroup) {

                // If the chatWorker is a member of the group they will receive a message that the client joined.
                if (chatWorker.isMemberOfGroup(reciever)){

                    //debugging statement
                    System.out.println("message sent to group");

                    // The message to send out the chatWorker
                    String recievedMessage = "msg " + login + " " + messageContent + "\n";
                    chatWorker.send(recievedMessage);
                }

                // Else the message is for an individual.
            } else {

                // Will only get sent to the receiver!
                if (reciever.equalsIgnoreCase(chatWorker.getLogin())) {

                    // Debugging statement
                    System.out.println("message sent to individual");

                    // The message to send out the chatWorker
                    String recievedMessage = "msg " + login + " private " + messageContent + "\n";
                    chatWorker.send(recievedMessage);
                }
            }
        }

    }

    /**
     * This method logs a client off.
     * @throws IOException
     */
    private void handleClientLogoff() throws IOException {

        // This removes chatWorker from the list so when we loop through only users that are online receive a message.
        chatServer.removeChatWorker(this);

        // Getting and initialising a the list of workers that represents the list of users online.
        ConcurrentLinkedQueue<chatServerWorker>  csWorkerList = chatServer.getCSWorkerList();

        // This loops through the list of ChatServerWorkers for which there is one for every client.
        for(chatServerWorker chatWorker : csWorkerList) {

            // Offline message
            String newUserOffline = "offline " + login + " is now offline\n";

            // This sends the message
            chatWorker.send(newUserOffline);
        }

        // Debugging statement
        System.out.println("User " + login + " logged off!");

        // Closes client socket, completing logout process
        clientSocket.close();
    }


    /**
     * This handles the creation of a new sign up, adding them to the database, and catching the possible errors.
     * @param outputStreamOfClient
     * @param tokens
     * @throws IOException
     */
    private void handleNewUser(OutputStream outputStreamOfClient, String[] tokens) throws IOException {

        // username input
        String username = tokens[1];

        // password input
        String password = tokens[2];

        try {

            Connection connection = DatabaseConnection.getConnection();

            // If there is no connection.
            if ( connection == null ) {

                // Error message
                String failedDatabaseConnection = "failedDatabaseConnection\n";
                outputStreamOfClient.write(failedDatabaseConnection.getBytes());

                // Debug Statement
                System.out.println("could not connect to database");
            }

            // SQL statement that checks if there is a pre-existing username with the same credentials.
            PreparedStatement newUsernameStatement = connection.prepareStatement("SELECT username FROM players WHERE username =?");

            newUsernameStatement.setString(1, username);

            // Collecting the result of the query
            ResultSet result = newUsernameStatement.executeQuery();

            // If result has any content then we know the username already exists in the database.
            if ( result.next() ) {

                // Error message
                String userAlreadyExists = "userAlreadyExists\n";
                outputStreamOfClient.write(userAlreadyExists.getBytes());

                // Closes the database connection
                connection.close();

                // Otherwise the username is valid.
            } else {

                // This query is to insert a new entry into the database.
                PreparedStatement newEntry = connection
                        .prepareStatement("INSERT INTO players (username, password) VALUES (?,?)");

                newEntry.setString(1, username);
                newEntry.setString(2, password);

                // Execute the query
                newEntry.executeUpdate();


                String newUser = "newUser\n";
                outputStreamOfClient.write(newUser.getBytes());

                // Closes the database connection
                connection.close();
            }

        } catch (SQLException sqlException) {

            String failedDatabaseConnection = "failedDatabaseConnection\n";
            outputStreamOfClient.write(failedDatabaseConnection.getBytes());

            // Debug Statement
            System.out.println("could not connect to database");
        }
    }

    /**
     * This will be used to communicate across threads when someone new logs in
     * @return
     */
    public String getLogin(){
        return login;
    }

    /**
     * Handles a user logging into the chat.
     * tokens will have the structure of:
     * command : login
     * username : username
     * password : password
     * with 3 tokens in that order.
     *
     *
     * @param outputStreamOfClient
     * @param tokens
     * @throws IOException
     */
    private void handleLogin(OutputStream outputStreamOfClient, String[] tokens) throws IOException {


        String username = tokens[1];
        String password = tokens[2];

        ConcurrentLinkedQueue<chatServerWorker>  csWorkerList = chatServer.getCSWorkerList();


        try {

            if (!isOnline(username)) {
                Connection connection = DatabaseConnection.getConnection();

                if (connection == null) {
                    String failedDatabaseConnection = "failedDatabaseConnection\n";
                    outputStreamOfClient.write(failedDatabaseConnection.getBytes());
                    System.out.println("could not connect to database");

                }

                PreparedStatement statement = connection.prepareStatement("SELECT password FROM players WHERE username = ?");

                statement.setString(1, username);

                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    if (result.getString("password").equals(password)) {


                        String successfulLoginStatement = "login\n";
                        outputStreamOfClient.write(successfulLoginStatement.getBytes());

                        //display login state
                        this.login = username;

                        System.out.println("User " + username + " logged in!");


                        //This tells new users who is online when they log in
                        for (chatServerWorker chatWorker : csWorkerList) {

                            //This prevents users that are online recieving a message saying
                            //they are online
                            if (!username.equals(chatWorker.getLogin())) {

                                //this is to guard against users that are connected but haven't logged in
                                if (chatWorker.getLogin() != null) {
                                    String usersOnline = "online " + chatWorker.getLogin() + " is online \n";
                                    send(usersOnline);
                                }
                            }
                        }
                        //This sends a message to all connections when a new user logs in

                        for (chatServerWorker chatWorker : csWorkerList) {

                            String newUserOnline = "online " + username + " is online\n";
                            //This prevents users that are online recieving a message saying
                            //they are online

                            if (!username.equals(chatWorker.getLogin())) {
                                chatWorker.send(newUserOnline);
                            }
                        }
                        connection.close();

                    }
                    else{

                        String incorrectPassword = "incorrectpassword\n";
                        outputStreamOfClient.write(incorrectPassword.getBytes());
                        System.out.println("incorrect password");
                        connection.close();
                    }

                }
                else{

                    String incorrectUsername = "incorrectusername\n";
                    outputStreamOfClient.write(incorrectUsername.getBytes());
                    System.out.println("incorrect username");
                    connection.close();
                }
            }
        }catch (SQLException sqlException) {

            String failedDatabaseConnection = "failedDatabaseConnection\n";
            outputStreamOfClient.write(failedDatabaseConnection.getBytes());
            System.out.println("could not connect to database");
        }
    }

    private boolean isOnline(String username) throws IOException {
        ConcurrentLinkedQueue<chatServerWorker>  csWorkerList = chatServer.getCSWorkerList();

        if(!csWorkerList.isEmpty()) {

            for (chatServerWorker chatWorker : csWorkerList) {
                if(chatWorker.getLogin() != null) {

                    if ( username.contentEquals(chatWorker.getLogin())) {
                        String alreadyLoggedIn = "alreadyOnline\n";
                        outputStreamOfClient.write(alreadyLoggedIn.getBytes());
                        System.out.println("already logged in");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * gets the groups that this chatServerWorker is in
     * @return groupSet
     */
    public HashSet<String> getGroupSet() {
        return groupSet;
    }

    /**
     * checks if a game has a winner. Will not accept ties, aways have to be a winner
     * best two out of three or until tie broken. Returns true if all conditions met
     *
     * @return boolean
     */
    public boolean hasWinner() {
        if ( this.score >= 2 && this.score != opponent.score ) {

            try {
                Connection connection = DatabaseConnection.getConnection();

                if (connection == null) {
                    return true;
                }

                PreparedStatement scoreUpdateWins = connection.prepareStatement("UPDATE scores SET wins = (wins + 1) WHERE username = ?");

                scoreUpdateWins.setString(1, this.getLogin());

                scoreUpdateWins.executeUpdate();

                PreparedStatement scoreUpdateLosses = connection.prepareStatement("UPDATE scores SET losses = (losses + 1) WHERE username = ?");

                scoreUpdateLosses.setString(1, opponent.getLogin());

                scoreUpdateLosses.executeUpdate();

                connection.close();

            } catch (SQLException e) {
                return true;
            }
            return true;
        }
        return false;    }

    /**
     * This method will access the outputStream of the client socket
     * and send a message to other users.
     * @param message
     */
    private void send(String message) throws IOException {

        //This prevents unnecessary online messages
        if (login != null ) {

            outputStreamOfClient.write(message.getBytes());
            outputStreamOfClient.flush();
        }
    }

    public int[] getPos() {
        return position;
    }

    public void setPos(int[] position){ this.position = position;}
}
