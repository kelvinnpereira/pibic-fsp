public class MainThread extends Thread{

    Created created;
    Running running;
    Runnable runnable;
    NonRunnable nonRunnable;

    MainThread(){
        created = new Created();
        running = new Running();
        runnable = new Runnable();
        nonRunnable = new NonRunnable();
        this.start();
    }

    public void run(){
        while(true){
            created.start();
            running.yield();
            runnable.suspend();
            nonRunnable.stop();
        }
    }

    public static void main(String args[]){
        MainThread main = new MainThread();
    }

}
