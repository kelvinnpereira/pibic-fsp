public class MainTHREAD implements Runnable{

    Thread threadTHREAD;

    THREAD obj_thread;

    CREATED obj_created;

    RUNNING obj_running;

    RUNNABLE obj_runnable;

    NON_RUNNABLE obj_non_runnable;

    TERMINATED obj_terminated;

    MainTHREAD(){
        obj_thread = new THREAD();
        obj_created = new CREATED();
        obj_running = new RUNNING();
        obj_runnable = new RUNNABLE();
        obj_non_runnable = new NON_RUNNABLE();
        obj_terminated = new TERMINATED();
        threadTHREAD = new Thread(this);
        threadTHREAD.start();
    }

    public void run(){
        try{
            while(true){
                Thread.sleep(500);
                obj_created.start();
                Thread.sleep(1000);
                obj_running.suspend();
                Thread.sleep(1000);
                obj_non_runnable.resume();
                Thread.sleep(1000);
                obj_runnable.dispatch();
                Thread.sleep(1000);
                obj_running.yield();
                Thread.sleep(1000);
                obj_created.stop();
                Thread.sleep(1000);
                System.out.println("STOP");
                System.exit(1);
            }
        }catch(Exception e){}
    }

    public static void main(String args[]){
        MainTHREAD main = new MainTHREAD();
    }

}