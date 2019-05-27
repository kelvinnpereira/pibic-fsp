import java.util.ArrayList;

public class HiperGrafo{

    private ArrayList<Vertice> vertices;
    private ArrayList<Aresta> arestas;

    public HiperGrafo() {
        this.vertices = new ArrayList<Vertice>();
        this.arestas = new ArrayList<Aresta>();
    }

    public ArrayList<Vertice> getVertices(){
        return this.vertices;
    }

    public ArrayList<Aresta> getAresta(){
        return this.arestas;
    }

    public Vertice insereVertice(String nome, int id, int estado, int valor_indice, boolean compartilhada){
        if(nome == null) return null;
        Vertice existe = busca(nome, id, estado, valor_indice);
        if(existe != null) return existe;
        Vertice v = new Vertice(nome, id, estado, valor_indice, compartilhada);
        vertices.add(v);
        return v;
    }

    public Vertice busca(String nome){
        for(int i=0;i<vertices.size();i++){
            if( nome.equals(vertices.get(i).getNome()) ){
                return vertices.get(i);
            }
        }
        return null;
    }

    public Vertice busca(String nome, int valor_indice){
        for(int i=0;i<vertices.size();i++){
            if( nome.equals(vertices.get(i).getNome()) && valor_indice == vertices.get(i).getValorIndice() ){
                return vertices.get(i);
            }
        }
        return null;
    }

    public Vertice busca(String nome, int id, int estado, int valor_indice){
        Vertice v = new Vertice(nome, id, estado, valor_indice, false);
        for(int i=0;i<vertices.size();i++){
            if( v.equals(vertices.get(i)) ){
                return vertices.get(i);
            }
        }
        return null;
    }

    public Vertice buscaUltimo(String nome, int id, int estado, int valor_indice){
        Vertice v = new Vertice(nome, id, estado, valor_indice, false), ret = null;
        for(int i=0;i<vertices.size();i++){
            if( v.equals(vertices.get(i)) )
                ret = vertices.get(i);
        }
        return ret;
    }

    public Aresta insereAresta(){
        Aresta a = new Aresta();
        arestas.add(a);
        a.setId(arestas.size()-1);
        return a;
    }

    public void insereAdj(Vertice v1, Vertice v2, Aresta a) {
        if(v1 == null || v2 == null || existeAresta(v1, v2) != null) return;
        v1.insereAresta(a);
        a.insereVertice(v2);
    }

    public void insereAdjSimetrica(Vertice v1, Vertice v2) {
        insereAdj(v1, v2, new Aresta());
        insereAdj(v2, v1, new Aresta());
    }

    public String existeAresta(Vertice v1, Vertice v2) {
        if(v1 == null || v2 == null) return null;
        Aresta a = v1.buscaVizinho(v2);
        return a != null ? v2.getNome() : null;
    }

    public String toString(){
        String str = "";
        for(int i=0;i<vertices.size();i++)
            str += vertices.get(i).toString() + "\n\n\n";
        return str;
    }

}