class Semaforo{
    
    private int val, max;

    Semaforo(int val){
        this.val = val;
        this.max = max;
    }

    public synchronized void dec() throws InterruptedException{
        val--;
        while(val > 0) wait();
        val = max;
        notifyAll();
    }
}