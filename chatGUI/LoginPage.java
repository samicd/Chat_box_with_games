package chatGUI;

import FinalDemoCode.catanGUI.MainWindow;
import Login.DatabaseConnection;
import chatClient.ChatClient;
import chatClient.LoginStatusListener;
import chatClient.MessageListener;
import chatClient.SLListener;
import chatServer.SnakesAndLadders;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.SocketException;
import static javafx.scene.paint.Color.RED;

/**
 * LoginPage class contains the GUI and functionality for
 * the initial login page, create new user and game home page,
 * which includes a chat.
 *
 * add game page to description
 */
public class LoginPage extends Application {

    private Scene sceneRPS, startScene1;
    private JFrame frame = new JFrame("Snakes and Ladders");
    private JLabel messageLabel = new JLabel("...");
    private Square[] board = new Square[101];
    private Square currentSquare;
    private Square oldSquare;
    private String sendingMessageTo = "#CatanChatRoom";
    private TextArea chatBox = new TextArea();
    private ListView<String> chatList = new ListView<String>();
    private Scene sceneLogin, sceneHomePage, sceneNewUser, sceneUserAccount;
    private ChatClient client = new ChatClient("localhost",4444);

    /**
     * The initial stage is set with the login
     * page scene.
     *
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {

        /**Scene set up for the initial login page.
         * The login page contains a username and
         * password textfield and two buttons 'submit'
         * and 'create new user' within a VBox.
         */

        //username textfield
        Text username = new Text("Username");
        username.setFont(Font.font("Helvetica", 20));

        TextField usernameTextField = new TextField();
        usernameTextField.setFont(Font.font("Helvetica", 20));

        //password textfield
        Text password = new Text("Password");
        password.setFont(Font.font("Helvetica", 20));

        PasswordField passwordTextField = new PasswordField();
        passwordTextField.setFont(Font.font("Helvetica", 20));

        //label to display error message in case of incorrect input
        Label loginFailed = new Label("");
        loginFailed.setFont(Font.font("Helvetica", 16));

        //login button
        Button loginButton = new Button("Login");
        loginButton.setFont(Font.font("Helvetica", 20));
        loginButton.setMaxWidth(Double.MAX_VALUE);

        //create new user button
        Button createNewUserButton = new Button("Create New User");
        createNewUserButton.setFont(Font.font("Helvetica", 20));
        createNewUserButton.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(40, 40, 40, 40));

        vbox.getChildren().addAll(username, usernameTextField, password, passwordTextField, loginFailed, loginButton, createNewUserButton);

        sceneLogin = new Scene(vbox, 400, 400);
        stage.setTitle("Login");
        stage.setScene(sceneLogin);
        stage.show();


        /**
         * Eventhandler for the login button on login page screen.
         * When the login button is pressed, the function checks
         * if it is a valid input. The client is then connected to the
         * server and the login details are checked with the database
         * to see if they match. If login details are correct, the
         * user is taken to the game home page screen, otherwise
         * an error message is displayed.
         */
        loginButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

                    try {

                        String usernameInput = usernameTextField.getText();
                        String passwordInput = passwordTextField.getText();

                        //if username or password is empty an error message is displayed
                        if ( usernameInput.isEmpty() || passwordInput.isEmpty() ) {
                            loginFailed.setText("Please enter details");
                        }

                        //if there are any spaces in the username or password input an error message is displayed
                        else if ( usernameInput.contains(" ") || passwordInput.contains(" ") ) {
                            loginFailed.setText("Must not contain spaces");


                        } else if ( client.connect() ) {

                            //sends login details through client-server to be checked in database
                            String reply = client.login(usernameInput, passwordInput);

                            //user logged in if reply from client-server correct, otherwise login denied
                            if (reply.equals("login")) {

                                client.setUsername(usernameInput);
                                client.setPassword(passwordInput);

                                System.out.println("Succesfully connected");


                                //adding a user to the catan group
                                client.joinGroup("#CatanChatRoom");

                                //send a  message to the server to get the groups this client is a member of and set it
                                //below adds the group to the List View

                                Platform.runLater(() -> {
                                   	chatBox.appendText("*sender* indicates a private message. \nClick on someones name to send a private message.\n");
                                    chatList.getItems().clear();
                                    chatList.getItems().add("#CatanChatRoom");
                                });

                                stage.setTitle("Welcome");
                                stage.setScene(sceneHomePage);
                                stage.show();

                                usernameTextField.clear();
                                passwordTextField.clear();
                                loginFailed.setText("");



                            } else if ( reply.equals("alreadyOnline")) {
                                loginFailed.setText("Already online");

                            }else if ( reply.equals("incorrectpassword") ) {
                                loginFailed.setText("Incorrect password");

                            } else if ( reply.equals("incorrectusername") ) {
                                loginFailed.setText("Username does not exist");

                            } else
                                loginFailed.setText("Could not connect");
                            {
                            }
                        } else {
                            loginFailed.setText("Could not connect");
                        }

                    } catch (IOException e1) {
                        loginFailed.setText("Could not connect");
                    }
                }
        );

        /**
         * Eventhandler for Create New User button on login page.
         * User taken to new user page.
         */

        createNewUserButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            usernameTextField.clear();
            passwordTextField.clear();
            loginFailed.setText("");

            stage.setScene(sceneNewUser);
            stage.setTitle("New User");
            stage.show();
        } );

        /**
         * Scene set up for new user page.
         * The scene consists of a new username textfield, new
         * password textfield and two buttons 'submit' to
         * confirm details and 'return' to go back to login
         * page, all contained within a VBox.
         */

        //new username textfield

        Text newUser = new Text("New User");
        newUser.setFont(Font.font("Helvetica", 20));

        TextField newUserTextField = new TextField();
        newUserTextField.setFont(Font.font("Helvetica", 20));

        //new password textfield

        Text newPassword = new Text("New Password");
        newPassword.setFont(Font.font("Helvetica", 20));

        PasswordField newPasswordTextField = new PasswordField();
        newPasswordTextField.setFont(Font.font("Helvetica", 20));

        //label to display error message in case of invalid input

        Label invalidInput = new Label("");
        invalidInput.setFont(Font.font("Helvetica", 16));

        //submit button

        Button submitButton = new Button("Submit");
        submitButton.setFont(Font.font("Helvetica", 20));
        submitButton.setMaxWidth(Double.MAX_VALUE);

        //return button

        Button returnButton = new Button("Return");
        returnButton.setFont(Font.font("Helvetica", 20));
        returnButton.setMaxWidth(Double.MAX_VALUE);

        VBox newUserVbox = new VBox(15);
        newUserVbox.setPadding(new Insets(40, 40, 40, 40));

        newUserVbox.getChildren().addAll(newUser, newUserTextField, newPassword, newPasswordTextField, invalidInput, submitButton, returnButton);

        sceneNewUser = new Scene(newUserVbox, 400, 400);

        /**
         * Eventhandler for submit button on new user scene.
         * The function checks that the input is valid and
         * then passes the inputs to be checked by the database
         * via the client-server. If the input is confirmed,
         * the user is taken to the login page, otherwise an error
         * message is displayed.
         */
        submitButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            try {
                String newUserInput = newUserTextField.getText();
                String newPasswordInput = newPasswordTextField.getText();

                //checks for valid input
                if ( newUserInput.trim().isEmpty() || newPasswordInput.trim().isEmpty() ) {
                    invalidInput.setText("Please enter details");

                } else if ( newUserInput.contains(" ") || newPasswordInput.contains(" ") ) {
                    invalidInput.setText("Must not contain spaces");

                } else if ( newUserInput.length() > 10 || newPasswordInput.length() > 10 ) {
                    invalidInput.setText("Maximum ten characters");


                } else if ( client.connect() ) {

                    //user inputs passed to be checked by database via client-server
                    String reply = client.newUser(newUserInput, newPasswordInput);

                    //if username already exists error message displayed
                    if ( reply.equals("userAlreadyExists") ) {
                        invalidInput.setText("Username already exists");

                        //if input details valid the user is taken to login page
                    } else if ( reply.equals("newUser") ) {

                        stage.setScene(sceneLogin);
                        stage.setTitle("Login");
                        stage.show();

                        newUserTextField.clear();
                        newPasswordTextField.clear();
                        invalidInput.setText("");

                    } else {
                        invalidInput.setText("Could not connect");
                    }
                }

            } catch (IOException e1) {
                invalidInput.setText("Could not connect");
            }
        });

        /**
         * Eventhandler for the return button on the new user scene.
         * The function returns the user to the login scene.
         */
        returnButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            newUserTextField.clear();
            newPasswordTextField.clear();
            invalidInput.setText("");

            stage.setScene(sceneLogin);
            stage.setTitle("Login");
            stage.show();
        });

        /**
         * Set up for the game home page. The page consists of
         * a 'start game' button, 'user account' button' and 'logout'
         * button. Underneath this is a chat messenger consisting of
         * text area, textfield for user input, 'send' button
         * and list of users online.
         */

        //start button

        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("Helvetica", 20));
        startButton.setMaxWidth(Double.MAX_VALUE);

        //user account button

        Button userAccountButton = new Button("User Account");
        userAccountButton.setFont(Font.font("Helvetica", 20));
        userAccountButton.setMaxWidth(Double.MAX_VALUE);

        //logout button

        Button logoutButton = new Button("Logout");
        logoutButton.setFont(Font.font("Helvetica", 20));
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        //chat box text area

        chatBox.setMaxWidth(405);
        chatBox.setMaxHeight(500);
        chatBox.setEditable(false);
        chatBox.appendText("*sender* indicates a private message. \nClick on someones name to send a private message.\n");

        //send button

        TextField chatInput = new TextField();
        Button sendButton = new Button("Send");

        VBox chatVbox = new VBox(15);
        chatVbox.getChildren().addAll(chatBox, chatInput, sendButton);

        HBox chatHbox = new HBox(15);

        chatHbox.setMaxWidth(600);
        chatHbox.setMaxHeight(600);

        //list for people who are online
        chatList.setMaxHeight(222);
        chatList.setMaxWidth(100);

        chatHbox.getChildren().addAll(chatList, chatVbox);

        VBox vboxHome = new VBox(15);
        vboxHome.setPadding(new Insets(40, 40, 40, 40));

        vboxHome.getChildren().addAll(startButton, userAccountButton, logoutButton, chatHbox);

        sceneHomePage = new Scene(vboxHome, 600, 600);


        /**
         * When a user is selected from the list, it updates the 'sendingMessageTo' field.
         */
        chatList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                // change the label text value to the newly selected item.
                sendingMessageTo = newValue;
            }
        });

        /**
         * When the sendButton is pressed the text from the chat input is parsed and concatenated with the appropriate
         * 'sendingMessageTo' label and sent to the client side using the 'msg' method.
         */
        sendButton.addEventFilter(ActionEvent.ANY, e -> {

            String chatInputText = chatInput.getText();

            if(client.isConnected()) {
                chatBox.appendText("Server disconnected\n");
            } else{

                //checks if the box is empty
                if (!chatInputText.isEmpty()) {

                    if (sendingMessageTo.equalsIgnoreCase("#CatanChatRoom")) {

                        Platform.runLater(() -> chatBox.appendText("You to Group : " + chatInputText + "\n"));
                        chatInput.clear();

                    }
                    else{

                        Platform.runLater(() -> chatBox.appendText("*You* to " + sendingMessageTo + " : " + chatInputText + "\n"));
                        chatInput.clear();
                    }
                    try {
                        client.msg(sendingMessageTo, chatInputText);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        /**
         * Eventhandler for the logout button, calls 'logout' method from client
         */

        logoutButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            // The code below didn't produce the required results with respect receiving the correct message from the server
    			            //            try {
    			            //                client.saveMessage(client.getUsername(), chatBox.getText());
    			            //            } catch (IOException e1) {
    			            //                e1.printStackTrace();
    			            //            }
    			            //                    if (reply.equals("saved") || reply.equals("failedDatabaseConnection")) {
    			            //                        System.out.println("yes");
    			            //                        chatBox.clear();
    			            //                    }else if(reply.equalsIgnoreCase("didnt")){
    			            //                        System.out.println("didn't interupt thread");
    			            //                    }
    			            //            } catch (IOException e1) {
    			            //                e1.printStackTrace();
    			            //            }
            try {
                client.logoff();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            stage.setTitle("Login");
            stage.setScene(sceneLogin);
            stage.show();
        });

        /**
         * Account page, this consists of username and wins and losses
         */

        // Username
        Text usernameText = new Text("Username");
        usernameText.setFont(Font.font("Helvetica", 20));

        // Username place holder
        Text usernameDetailsText = new Text("");
        usernameDetailsText.setFont(Font.font("Helvetica", 20));

        // Wins
        Text winText = new Text("Session Wins");
        winText.setFont(Font.font("Helvetica", 20));

        // Win place holder text
        Text winDetailsText = new Text("");
        winDetailsText.setFont(Font.font("Helvetica", 20));

        // Losses
        Text lossText = new Text("Session Losses");
        lossText.setFont(Font.font("Helvetica", 20));

        // Loss place holder text
        Text lossDetailsText = new Text("");
        lossDetailsText.setFont(Font.font("Helvetica", 20));

        Text overallWins = new Text("Total Wins");
        overallWins.setFont(Font.font("Helvetica", 20));

        // Overall wins
        Text overallWinsText = new Text("");
        overallWinsText.setFont(Font.font("Helvetica", 20));

        Text overallLosses = new Text("Total Losses");
        overallLosses.setFont(Font.font("Helvetica", 20));

        // Overall losses
        Text overallLossesText = new Text("");
        overallLossesText.setFont(Font.font("Helvetica", 20));

        GridPane gridPaneAccount = new GridPane();
        gridPaneAccount.setPadding(new Insets(40, 40, 40, 40));
        gridPaneAccount.setVgap(5);
        gridPaneAccount.setHgap(5);

        gridPaneAccount.add(usernameText, 0, 0);
        gridPaneAccount.add(usernameDetailsText, 10, 0);
        gridPaneAccount.add(winText, 0, 5);
        gridPaneAccount.add(winDetailsText, 10, 5);
        gridPaneAccount.add(lossText, 0, 10);
        gridPaneAccount.add(lossDetailsText, 10, 10);
        gridPaneAccount.add(overallWins, 0, 15);
        gridPaneAccount.add(overallWinsText, 10, 15);
        gridPaneAccount.add(overallLosses, 0, 20);
        gridPaneAccount.add(overallLossesText, 10, 20);

        sceneUserAccount = new Scene(gridPaneAccount, 350, 300);

        /**
         *  User account button for game home page
         */
        userAccountButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        	if(client.isConnected()) {
        			        		chatBox.appendText("Server disconnected\n");
        			        	}else {
            usernameDetailsText.setText(client.username);

            Stage userAccountStage = new Stage();
            userAccountStage.setTitle("User Account");
            userAccountStage.setScene(sceneUserAccount);
            userAccountStage.show();
        		}
        });




        //start game scene

        //Play rock, paper, scissors button
        Button startGameButton = new Button("Play rock, paper, scissors");
        startGameButton.setFont(Font.font("Helvetica", 20));

        //Play Snakes and Ladders button
        Button startSLButton = new Button("Play Snakes and Ladders");
        startSLButton.setFont(Font.font("Helvetica", 20));
        
        //Play Catan button
        Button startGame2Button = new Button("Play Catan");
        startGame2Button.setFont(Font.font("Helvetica", 20));
        startGame2Button.setMaxWidth(Double.MAX_VALUE);

        Button replayRPS = new Button("Replay");
        replayRPS.setVisible(false);

        VBox startVBox = new VBox(15);
        startVBox.setPadding(new Insets(40, 40, 40, 40));
        startVBox.setSpacing(15);

        startVBox.getChildren().addAll(startGameButton, startSLButton, startGame2Button);
        startVBox.setAlignment(Pos.CENTER);

        startScene1 = new Scene(startVBox, 400, 150);

        Stage stage1 = new Stage();
        stage1.setTitle("Rock Paper Scissors!!!");

        //Opens window that contains game options
        startButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        	if(client.isConnected()) {
        			        		chatBox.appendText("Server disconnected\n");
        			        	}
            stage1.setScene(startScene1);
            stage1.show();
        
        });

        /**
         * Event handler opens new window for the rock, paper, scissors game and adds user to images group
         */
        startGameButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        	
        	
        	try {
				client.joinGroup("#rockPaperScissor");

            }catch (SocketException e3) {
            	chatBox.appendText("disconnected");
            } catch (IOException e1) {
				e1.printStackTrace();
            }
			Platform.runLater(() -> stage1.setScene(sceneRPS));
			stage1.show();

			// Debug statement
			System.out.println("images window opened");
            
        });

        /**
         * Event handler opens new window for the Snakes and Ladder game and adds user to SL group
         */

        startSLButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            try {
                client.joinGroup("#SnakesAndLadders");
                //client.SLPlayer(client.getUsername(), " ",  0);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            stage1.hide();
            Platform.runLater(() -> frame.getContentPane());
            frame.setSize(640, 640);
            frame.setVisible(true);
            frame.setResizable(false);
        });
        
        startGame2Button.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        	if(client.isConnected()) {
        		chatBox.appendText("Server disconnected\n");
        	}
        	
            stage1.setScene(MainWindow.catanBoard());
            stage1.setTitle("Catan");
            stage1.show();
        });

        //Rock, Paper, Scissors game page

        //rock image
        FileInputStream inputStream1 = new FileInputStream("src/images/rock.png");
        Image rockImage = new Image(inputStream1);
        ImageView rockView = new ImageView(rockImage);
        rockView.setFitHeight(200);
        rockView.setFitWidth(200);

        //paper image
        FileInputStream inputStream2 = new FileInputStream("src/images/paper.jpg");
        Image paperImage = new Image(inputStream2);
        ImageView paperView = new ImageView(paperImage);
        paperView.setFitHeight(200);
        paperView.setFitWidth(200);

        //scissor image
        FileInputStream inputStream3 = new FileInputStream("src/images/scissors.png");
        Image scissorImage = new Image(inputStream3);
        ImageView scissorView = new ImageView(scissorImage);
        scissorView.setFitHeight(200);
        scissorView.setFitWidth(200);

        //hbox contains the three images
        HBox hboxRPS = new HBox(15);
        hboxRPS.setPadding(new Insets(40, 40, 40, 40));
        hboxRPS.getChildren().addAll(rockView, paperView, scissorView);

        //empty text to be set on click
        Text choice = new Text("");
        choice.setFont(Font.font("Helvetica", 40));

        //empty score to be set on click
        Text score = new Text("");
        choice.setFont(Font.font("Helvetica", 40));

        //vbox contains choice and score text
        VBox vboxRPS = new VBox(15);
        vboxRPS.setPadding(new Insets(40, 40, 40, 40));
        vboxRPS.getChildren().addAll(hboxRPS, choice,replayRPS, score);
        vboxRPS.setAlignment(Pos.CENTER);

        sceneRPS = new Scene(vboxRPS, 800, 500);

        /**
         * Rock click event filter. This calls the 'choiceMadeInGUI' method.
         * Also disables other choice buttons.
         */
        rockView.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            String rock = "rock";
            choice.setText(rock);

            try {
                client.choiceMadeInGUI(client.getUsername(),rock);
            }catch (SocketException e3) {
        			            	choice.setText("disconnected");
        			            	chatBox.appendText("disconnected from server\n");
            }catch (IOException e1) {
                e1.printStackTrace();
            }

            paperView.setDisable(true);
            scissorView.setDisable(true);
            rockView.setDisable(true);
            
        });

        /**
         * Paper click event filter. This calls the 'choiceMadeInGUI' method.
         * Also disables other choice buttons.
         */
        paperView.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            String paper = "paper";
            choice.setText(paper);

            try {
                client.choiceMadeInGUI(client.getUsername(),paper);
            } catch (SocketException e3) {
            	choice.setText("disconnected");
            	chatBox.appendText("disconnected from server\n");
            }catch (IOException e1) {
                e1.printStackTrace();
            }

            paperView.setDisable(true);
            scissorView.setDisable(true);
            rockView.setDisable(true);
        });


        /**
         * Scissors click event filter. This calls the 'choiceMadeInGUI' method.
         * Also disables other choice buttons.
         */
        scissorView.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {

            String scissor = "scissor";
            choice.setText(scissor);

            try {
                client.choiceMadeInGUI(client.getUsername(),scissor);
            } catch (SocketException e3) {
            	choice.setText("disconnected");
            	chatBox.appendText("disconnected from server\n");
            }catch (IOException e1) {
                e1.printStackTrace();
            }

            paperView.setDisable(true);
            scissorView.setDisable(true);
            rockView.setDisable(true);
        });

        replayRPS.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            stage1.setScene(sceneRPS);
            stage1.show();
        });

        /**
         * Snakes and Ladders JFrame set up
         */

        messageLabel.setBackground(java.awt.Color.lightGray);
        frame.getContentPane().add(messageLabel, BorderLayout.SOUTH);

        var boardPanel = new JLabel();
        ImageIcon icon = new ImageIcon("src/images/Board2.png");
        boardPanel.setIcon(icon);
        boardPanel.setLayout(new GridLayout(10, 10, 2, 2));


        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 10; i++) {

                if (j % 2 == 1){
                    board[(10 * (9-j)) + i + 1] = new Square(new int[]{i, j});
                    board[(10 * (9-j)) + i + 1].position = new int[]{i,j};
                    boardPanel.add(board[(10 * (9-j)) + i + 1]);
                }
                else {
                    board[(10 * (9-j) + 10 - i)] = new Square(new int[]{i, j});
                    board[(10 * (9-j) + 10 - i)].position = new int[]{i,j};
                    boardPanel.add(board[(10 * (9-j) + 10 - i)]);
                }
            }
        }

        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);

        currentSquare = board[1];
        currentSquare.setText('B');
        JButton roll = new JButton("Roll");
        frame.getContentPane().add(roll, BorderLayout.EAST);

        roll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {

                int steps = SnakesAndLadders.roll();

                if (SnakesAndLadders.yourTurn) {

                    SnakesAndLadders.yourTurn = false;

                    if (10 - currentSquare.position[0] + 10 * (9 - currentSquare.position[1]) + steps >= 100) {
                        oldSquare = currentSquare;
                        currentSquare = board[100];
                        messageLabel.setText("You rolled " + steps + ", please wait");
                        System.out.println("roll was " + steps);
                    } else if (currentSquare.position[1] % 2 == 1) {
                        int value = currentSquare.position[0] + 1 + 10 * (9 - currentSquare.position[1]) + steps;
                        oldSquare = currentSquare;
                        if (value == 28) {
                            currentSquare = board[13];
                            messageLabel.setText("You rolled " + steps + ", oh no you've slid down the snake, please wait");
                        } else if (value == 50) {
                            currentSquare = board[51];
                            messageLabel.setText("You rolled " + steps + ", you landed on a ladder up you go, please wait");
                        } else if (value == 21) {
                            currentSquare = board[41];
                            messageLabel.setText("You rolled " + steps + ", you landed on a ladder up you go, please wait");
                        } else if (value == 98) {
                            currentSquare = board[63];
                            messageLabel.setText("You rolled " + steps + ", oh no you've slid down the snake, please wait");
                        } else if (value == 75) {
                            currentSquare = board[56];
                            messageLabel.setText("You rolled " + steps + ", oh no you've slid down the snake, please wait");
                        } else {
                            currentSquare = board[value];
                            messageLabel.setText("You rolled " + steps + ", please wait");
                            System.out.println("roll was " + steps);
                        }
                    } else {
                        int value = 10 - currentSquare.position[0] + 10 * (9 - currentSquare.position[1]) + steps;
                        oldSquare = currentSquare;
                        if (value == 28) {
                            currentSquare = board[13];
                            messageLabel.setText("You rolled " + steps + ", oh no you've slid down the snake, please wait");
                        } else if (value == 50) {
                            currentSquare = board[51];
                            messageLabel.setText("You rolled " + steps + ", you landed on a ladder up you go, please wait");
                        } else if (value == 21) {
                            currentSquare = board[41];
                            messageLabel.setText("You rolled " + steps + ", you landed on a ladder up you go, please wait");
                        } else if (value == 98) {
                            currentSquare = board[63];
                            messageLabel.setText("You rolled " + steps + ", oh no you've slid down the snake, please wait");
                        } else if (value == 75) {
                            currentSquare = board[56];
                            messageLabel.setText("You rolled " + steps + ", oh no you've slid down the snake, please wait");
                        } else {
                            currentSquare = board[value];
                            messageLabel.setText("You rolled " + steps + ", please wait");
                            System.out.println("roll was " + steps);
                        }
                    }
                    try {

                        client.SLPlayer(client.getUsername(), "MOVE ", steps);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        /**
         * Adds the client to a message listener that will parse particular messages from the client.
         * This handles private, group and server messages.
         */
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String sender, String msgBody) {

                String[] tokens = StringUtils.split(msgBody,null,2);

                if(!sender.equalsIgnoreCase(client.getUsername())) {
                    if(tokens[0].equalsIgnoreCase("private")){

                        chatBox.appendText( "*" + sender + "* : " + tokens[1] + "\n");

                    }else if(tokens[0].equalsIgnoreCase("group")){
                        chatBox.appendText("Server : " + sender + " " + tokens[1] + "\n");

                    }else if(tokens[0].equalsIgnoreCase("result")){

                        chatBox.appendText("Server : " + tokens[1] + "\n");
                        choice.setText(tokens[1] + "!!!");
                        rockView.setDisable(false);
                        paperView.setDisable(false);
                        scissorView.setDisable(false);

                    }
                    else {

                        chatBox.appendText(sender + " : " + msgBody + "\n");
                        System.out.println("You got a message from " + sender + " : " + msgBody);
                    }
                }else if(sender.equalsIgnoreCase(client.getUsername())){
                    if(tokens[0].equalsIgnoreCase("group")){
                        chatBox.appendText("Server : " + sender + " " + tokens[1] + "\n");
                    }
                }
            }
        });

        client.addSLListener(new SLListener() {
            int opponentSquare = 1;
            @Override
            public void onTurn(String sender, String msgBody, int steps) {

                var mark = 'X';
                var opponentMark = 'O';
                frame.setTitle("Snakes and Ladders: Player " + sender);
                //String[] tokens = StringUtils.split(msgBody, null, 2);

                if (msgBody.equalsIgnoreCase("VALID_MOVE")) {

                    oldSquare.setText(' ');
                    oldSquare.repaint();
                    if (board[opponentSquare] == oldSquare) {
                        oldSquare.setText(opponentMark);
                        oldSquare.repaint();
                    }
                    if (currentSquare == board[opponentSquare]) {
                        currentSquare.setText('B');
                        currentSquare.repaint();
                    } else {
                        currentSquare.setText(mark);
                        currentSquare.repaint();
                    }
                    messageLabel.setText("Wait");
                    if (currentSquare == board[100]) {
                        JOptionPane.showMessageDialog(frame, "Winner Winner, Snake Dinner?");
                        frame.dispose();
                    }
                } else if (msgBody.equalsIgnoreCase("OPPONENT_MOVED")) {

                    //steps is not one of the tokens
                    //String steps = tokens[1];
                    //int roll = steps.charAt(0);
                    if (opponentSquare != 1) {
                        oldSquare.setText(' ');
                        oldSquare.repaint();
                    }
                    board[opponentSquare].setText(' ');
                    board[opponentSquare].repaint();

                    if (currentSquare == board[opponentSquare] || currentSquare == board[1]) {
                        currentSquare.setText(mark);
                        currentSquare.repaint();
                    }
                    opponentSquare += (steps - 48);
                    if (opponentSquare > 100) {
                        opponentSquare = 100;
                    } else if (opponentSquare == 28) {
                        opponentSquare = 13;
                    } else if (opponentSquare == 50) {
                        opponentSquare = 52;
                    } else if (opponentSquare == 21) {
                        opponentSquare = 41;
                    } else if (opponentSquare == 98) {
                        opponentSquare = 63;
                    } else if (opponentSquare == 75) {
                        opponentSquare = 56;
                    }
                    if (currentSquare == board[opponentSquare]) {
                        board[opponentSquare].setText('B');
                        board[opponentSquare].repaint();
                    } else {
                        board[opponentSquare].setText(opponentMark);
                        board[opponentSquare].repaint();
                    }
                    SnakesAndLadders.yourTurn = true;
                    if (opponentSquare == 100) {
                        JOptionPane.showMessageDialog(frame, "You lost, sorry");
                        frame.dispose();
                    }
                    messageLabel.setText("Opponent moved, your turn");
                } else if (msgBody.equalsIgnoreCase("MESSAGE")) {
                    //if (tokens[1].equalsIgnoreCase("waiting")) {
                        SnakesAndLadders.yourTurn = true;
                        messageLabel.setText("Wait");
                    //}
                } else if (msgBody.equalsIgnoreCase("OTHER_PLAYER_LEFT")) {
                    JOptionPane.showMessageDialog(frame, "Other player left");
                }
            }

        });

        client.addLoginStatusListener(new LoginStatusListener() {
            @Override
            public void online(String login) {

                chatBox.appendText(login + " is Online \n");
                Platform.runLater(() -> chatList.getItems().add(login));
            }
            @Override
            public void offline(String login) {
                ObservableList<String> listOfItems = chatList.getItems();
                for(String user : listOfItems ){
                    if(user.equalsIgnoreCase(login)){

                        chatBox.appendText(login + " is offline" + "\n");

                        Platform.runLater(() ->
                                listOfItems.remove(user));

                        Platform.runLater(() ->
                                chatList.setItems(listOfItems)
                        );
                        break;
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}