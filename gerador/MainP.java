public class MainP implements Runnable{

    Thread threadP;

    P obj_p;

    P obj_p;

    MainP(){
        obj_p = new P();
        obj_p = new P();
        threadP = new Thread(this);
        threadP.start();
    }

    public void run(){
        try{
            while(true){
                Thread.sleep(1000);
                obj_p.a();
                Thread.sleep(1000);
            }
        }catch(InterruptedException e){}
    }

    public static void main(String args[]){
        MainP main = new MainP();
    }

}