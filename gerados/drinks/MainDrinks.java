public class MainDrinks extends Thread{

    Drinks drinks;

    MainDrinks(){
        drinks = new Drinks();
        this.start();
    }

    public void run(){
        while(true){
            /*TRACE*/
            drinks.red();
            drinks.cofee();
            drinks.blue();
            drinks.tea();
        }
    }

    public static void main(String args[]){
        MainDrinks main = new MainDrinks();
    }

} 
