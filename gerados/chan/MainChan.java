public class MainChan extends Thread{

    Chan chan;

    MainChan(){
        chan = new Chan();
        this.start();
    }

    public void run(){
        while(true){
            chan.in();
            chan.out();
            chan.in();
            chan.in();
        }
    }

    public static void main(String args[]){
        MainChan main = new MainChan();
    }

}
