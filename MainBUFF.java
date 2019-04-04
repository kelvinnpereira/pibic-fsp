public class MainBUFF implements Runnable{

    Thread threadBUFF;

    BUFF obj_buff;

    MainBUFF(){
        obj_buff = new BUFF();
        threadBUFF = new Thread(this);
        threadBUFF.start();
    }

    public void run(){
        try{
            while(true){
                Thread.sleep(500);
                obj_buff.a_1();
                Thread.sleep(1000);
                System.exit(1);
            }
        }catch(Exception e){}
    }

    public static void main(String args[]){
        MainBUFF main = new MainBUFF();
    }

}