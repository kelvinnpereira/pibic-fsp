public class VerticeAdj{

    private Vertice vertice;
    private VerticeAdj prox;

    VerticeAdj(){
    }

    VerticeAdj(Vertice vertice){
        this.vertice = vertice;
    }

    public String toString() {
        return vertice.getNome() + " " + vertice.getId() +" | ";
    }
    
    public Vertice getVertice() {
        return vertice;
    }
    
    public void setVertice(Vertice vertice) {
        this.vertice = vertice;
    }
    
    public VerticeAdj getProx() {
        return prox;
    }

    public void setProx(VerticeAdj prox) {
        this.prox = prox;
    }

}