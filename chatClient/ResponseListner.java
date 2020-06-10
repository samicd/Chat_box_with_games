package chatClient;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ResponseListner {

    public static class ResponseListener implements Runnable {
        private Socket socket;

        public ResponseListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Scanner sc;
            try {
                sc = new Scanner(socket.getInputStream());
                while (!socket.isClosed() && sc.hasNextLine()) {
                    System.out.println(sc.nextLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
