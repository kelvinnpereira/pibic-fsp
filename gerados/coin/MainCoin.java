public class MainCoin extends Thread{

    Coin coin;

    MainCoin(){
        coin = new Coin();
        this.start();
    }

    public void run(){
        while(true){
            coin.toss();
            coin.heads();
            coin.toss();
            coin.tails();
        }
    }

    public static void main(String args[]){
        MainCoin main = new MainCoin();
    }

}
