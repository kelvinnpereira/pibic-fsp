import java.util.ArrayList;

public class Vertice{

    private ArrayList<Aresta> arestas;
    private String nome;
    private int id, estado, valor_indice;
    private boolean compartilhada, trava;

    Vertice(){
    }

    Vertice(String nome, int id, int estado, int valor_indice){
        this.nome = nome;
        this.id = id;
        this.estado = estado;
        this.valor_indice = valor_indice;
        this.arestas = new ArrayList<Aresta>();
    }

    public ArrayList<Aresta> getArestas(){
        return this.arestas;
    }

    public int getId(){
        return this.id;
    }

    public String getNome(){
        return this.nome;
    }

    public String getBoxName(){
        return this.nome+(valor_indice == -1 ? "" : "["+valor_indice+"]");
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public boolean getCompartilhada(){
        return this.compartilhada;
    }

    public void setCompartilhada(boolean compartilhada){
        this.compartilhada = compartilhada;
    }

    public int getEstado(){
        return this.estado;
    }

    public int getValorIndice(){
        return this.valor_indice;
    }

    public void insereAresta(Aresta a){
        if(a == null) return;
        arestas.add(a);
    }

    public boolean getTrava(){
        return this.trava;
    }

    public void setTrava(boolean val){
        this.trava = val;
    }

    public String toString(){
        String str = nome+", id: "+id +", e: "+estado+", vi: "+valor_indice+", s:"+compartilhada+ ", t: "+trava+" --> ";
        for(int i=0;i<arestas.size();i++){
            str += arestas.get(i).toString();
        }
        return str;
    }

    public Vertice buscaNome(String nome){
        for(int i=0;i<arestas.size();i++){
            ArrayList<Vertice> v = arestas.get(i).getVertices();
            for(int j=0;j<v.size();j++){
                if(v.get(j).getNome().equals(nome) ) return v.get(j);
            }
        }
        return null;
    }

    public Vertice buscaVizinho(String nome, int valor_indice, int id){
        for(int i=0;i<arestas.size();i++){
            ArrayList<Vertice> v = arestas.get(i).getVertices();
            for(int j=0;j<v.size();j++){
                if(v.get(j).getNome().equals(nome) && v.get(j).getValorIndice() == valor_indice && v.get(j).getId() == id) return v.get(j);
            }
        }
        return null;
    }

    public Aresta buscaVizinho(Vertice vertice) {
        for(int i=0;i<arestas.size();i++){
            ArrayList<Vertice> v = arestas.get(i).getVertices();
            for(int j=0;j<v.size();j++){
                if(v.get(j) == vertice) return arestas.get(i);
            }
        }
        return null;
    }

    public void remove(Vertice v){
        Aresta a = buscaVizinho(v);
        if(a == null) return;
        arestas.remove(a);
        a.remove(v);
    }

    public boolean equals(Object o){
        Vertice v = (Vertice)o;
        return id == v.id && estado == v.estado && valor_indice == v.valor_indice && nome.equals(v.nome);
    }

    public Vertice only(){
        return  arestas.size() == 1 && 
                arestas.get(0).getVertices().size() == 1 && 
                (   arestas.get(0).getVertices().get(0).getNome().equals("STOP") || 
                    arestas.get(0).getVertices().get(0).getNome().equals("ERROR")
                ) 
                ? arestas.get(0).getVertices().get(0) : null;
    }

}