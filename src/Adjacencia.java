public class Adjacencia{

    private Aresta aresta;
    private boolean dir;

    Adjacencia(Aresta aresta, boolean dir){
        this.aresta = aresta;
        this.dir = dir;
    }

    public Aresta getAresta(){
        return this.aresta;
    }

    public void setAresta(Aresta aresta){
        this.aresta = aresta;
    }

    public boolean getDir(){
        return this.dir;
    }

    public void setDir(boolean dir){
        this.dir = dir;
    }

    public String toString(){
        return aresta.toString();
    }

}