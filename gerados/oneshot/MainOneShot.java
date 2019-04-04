public class MainOneShot extends Thread{

    OneShot oneShot;

    MainOneShot(){
        oneShot = new OneShot();
        this.start();
    }

    public void run(){
        while(true){
            oneShot.once();
        }
    }

    public static void main(String args[]){
        MainOneShot main = new MainOneShot();
    }

}
