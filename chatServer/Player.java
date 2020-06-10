package chatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player extends Thread{
	
	public Socket clientSocket;
	public String playerName;	//	this can be changed to actual Player object
	public ChatServer server;
	private PrintWriter outputStream1;
    private BufferedReader input;
    private Player currentPlayer;
    private Player opponent1;
    private Player opponent2;
    
	public Player(Socket clientSocket, String playerName, ChatServer server) {
		this.clientSocket = clientSocket;
		this.playerName = playerName;
		this.server = server;
	}
	
	public void connect() throws IOException {
		outputStream1 = new PrintWriter(clientSocket.getOutputStream(), true);
		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		outputStream1.println("Add player name: ");
		while(input.readLine() != null) {
			if(playerName.equals("player1")) {
				currentPlayer = this;
				outputStream1.println("Waiting for other players to connect");
			}else if(playerName.equals("player2")) {
				currentPlayer = this;
				opponent1 = currentPlayer;
				outputStream1.println("Waiting for other players to connect");
			}else if(playerName.contentEquals("player3")){
				currentPlayer = this;
				opponent2 = currentPlayer;
				outputStream1.println("All players connected...");
			}
		}
	}
	
	public void buy() {
		// call process buy method
	}
	
	public void trade() {
		// call process trade method
	}
	
	public void endGame() {
		
	}
	
	public void processBuy() {
		
	}
	
	public void ProcessTrade() {
		
	}
	@Override
	public void run() {
		try {
			//login from chatServerWorker is true then connect to game. we need to add tru bloolean from that class
			connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

