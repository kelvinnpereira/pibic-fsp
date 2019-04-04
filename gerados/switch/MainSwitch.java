public class MainSwitch extends Thread{

    On on;
    Off off;

    MainSwitch(){
        on = new On();
        off = new Off();
        this.start();
    }

    public void run(){
        while(true){
            off.on();
            on.off();
            off.on();
            on.off();
            off.on();
        }
    }

    public static void main(String args[]){
        MainSwitch main = new MainSwitch();
    }

}
