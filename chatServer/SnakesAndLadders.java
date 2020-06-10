package chatServer;

public class SnakesAndLadders {

    public static chatServerWorker currentPlayer; //Player
    public static boolean yourTurn=true;


    public static boolean isWinner(chatServerWorker player){

        if (player.getPos()[0] == 0 && player.getPos()[1] == 0){
            return true;
        }
        return false;
    }

    public static int roll(){
        return (int)(Math.random()*6)+1;
    }

    public static void move(int roll, chatServerWorker player){

        for (int i = 0; roll > 0; roll--) {
            if(player.getPos()[1] % 2 == 0) {
                if (player.getPos()[0] == 0 && player.getPos()[1] == 0){
                    roll = 0;
                    isWinner(player);
                }
                else if (player.getPos()[0] == 0) {
                    player.setPos(new int[]{player.getPos()[0], player.getPos()[1] - 1});
                } else {
                    player.setPos(new int[]{player.getPos()[0] - 1, player.getPos()[1]});
                }
            }
            else {
                if (player.getPos()[0] == 9) {
                    player.setPos(new int[]{player.getPos()[0], player.getPos()[1] - 1});
                } else {
                    player.setPos(new int[]{player.getPos()[0] + 1, player.getPos()[1]});
                }
            }
        }
    }

    public static synchronized void turn(int roll, chatServerWorker player) {
        if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        }//else if (player != currentPlayer) {
            //SnakesAndLadders.yourTurn = false;
            //throw new IllegalStateException("Not your turn");
        //} 
    else {
            move(roll, player);
            //currentPlayer = currentPlayer.opponent;
        }
    }
}
