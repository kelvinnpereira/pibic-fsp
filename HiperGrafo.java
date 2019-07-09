import java.util.ArrayList;

public class HiperGrafo{

    private ArrayList<Vertice> vertices;
    private ArrayList<Aresta> arestas;
    private ArrayList<Vertice> atual;

    public HiperGrafo() {
        this.vertices = new ArrayList<Vertice>();
        this.arestas = new ArrayList<Aresta>();
        this.atual = new ArrayList<Vertice>();
    }

    public ArrayList<Vertice> getVertices(){
        return this.vertices;
    }

    public ArrayList<Vertice> getAtual(){
        return this.atual;
    }

    public void setAtual(ArrayList<Vertice> atual){
        this.atual = atual;
    }

    public ArrayList<Aresta> getAresta(){
        return this.arestas;
    }

    public Vertice insereVertice(String nome, int id, int estado, int valor_indice, boolean inicio){
        if(nome == null) return null;
        Vertice existe = busca(nome, id, estado, valor_indice);
        if(existe != null) return existe;
        Vertice v = new Vertice(nome, id, estado, valor_indice);
        vertices.add(v);
        if(inicio) atual.add(v);
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
        Vertice v = new Vertice(nome, id, estado, valor_indice);
        for(int i=0;i<vertices.size();i++){
            if( v.equals(vertices.get(i)) ){
                return vertices.get(i);
            }
        }
        return null;
    }

    public Vertice buscaUltimo(String nome, int id, int estado, int valor_indice){
        Vertice v = new Vertice(nome, id, estado, valor_indice), ret = null;
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
        str += "\n--------------atual--------------------\n";
        for(int i=0;i<atual.size();i++)
            str += atual.get(i).toString() + "\n\n\n";
        return str;
    }

    public Object clone(){
        HiperGrafo hg = new HiperGrafo();
        for(int i=0;i<this.vertices.size();i++){
            Vertice v = this.vertices.get(i);
            hg.insereVertice(v.getNome(), v.getId(), v.getEstado(), v.getValorIndice(), false);
        }
        for(int i=0;i<this.atual.size();i++){
            Vertice v = this.atual.get(i);
            hg.atual.add(hg.busca(v.getNome(), v.getId(), v.getEstado(), v.getValorIndice()));
        }
        for(int i=0;i<this.vertices.size();i++){
            ArrayList<Aresta> a = this.vertices.get(i).getArestas();
            for(int j=0;j<a.size();j++){
                ArrayList<Vertice> ve = a.get(j).getVertices();
                for(int k=0;k<ve.size();k++){
                    Vertice v1 = this.vertices.get(i), v2 = ve.get(k);
                    hg.insereAdj(hg.busca(v1.getNome(), v1.getId(), v1.getEstado(), v1.getValorIndice()), hg.busca(v2.getNome(), v2.getId(), v2.getEstado(), v2.getValorIndice()), new Aresta());
                }
            }
        }
        return hg;
    }

    public void renameAll(String prefix, boolean trava){
        ArrayList<Vertice> v = vertices;
        for(int i=0;i<v.size();i++){
            v.get(i).setNome(prefix+v.get(i).getNome());
            v.get(i).setTrava(trava);
        }
    }

    public Vertice inAtual(String nome){
        ArrayList<Integer> int_list = new ArrayList<Integer>();
        for(int i=0;i<atual.size();i++){
            if(atual.get(i).getBoxName().equals(nome)) int_list.add(i);
        }
        int max = int_list.size()-1, min = 0;
        int i = (int)(Math.random() * ((max - min) + 1)) + min;
        return int_list.size() == 0 ? null : atual.get(int_list.get(i));
    }

}