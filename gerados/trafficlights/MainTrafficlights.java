public class MainTrafficlights extends Thread{

    Trafficlights traffic;

    MainTrafficlights(){
        traffic = new Trafficlights();
        this.start();
    }

    public void run(){
        while(true){
            traffic.red();
            traffic.orange();
            traffic.green();
            traffic.orange();
        }
    }

    public static void main(String args[]){
        MainTrafficlights main = new MainTrafficlights();
    }

}
