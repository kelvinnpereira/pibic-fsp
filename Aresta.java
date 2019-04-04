import java.util.ArrayList;

public class Aresta{

    private ArrayList<Vertice> vertices;
    private int id;
    private String nome;

    Aresta(){
        vertices = new ArrayList<Vertice>();
    }

    Aresta(String nome){
        this.nome = nome;
    }

    public void setId(int i){
        this.id = id;
    }

    public ArrayList<Vertice> getVertices(){
        return this.vertices;
    }

    public boolean insereVertice(Vertice v){
        if(v == null) return false;
        vertices.add(v);
        return true;
    }

    public void remove(Vertice v){
        vertices.remove(v);
    }

    public String toString(){
        String str = "";
        for(int i=0;i<vertices.size();i++){
            str += vertices.get(i).getNome() + ", id: " + vertices.get(i).getId()  +", estado: "+vertices.get(i).getEstado() +", valor_indice: "+vertices.get(i).getValorIndice()+" | ";
        }
        return str;
    }

}