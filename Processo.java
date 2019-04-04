import java.util.ArrayList;

public class Processo{

    private String nome, indice;
    private ArrayList<Acao> acoes;
    private int estado;
    private Range range;

    Processo(String nome){
        this(nome, "", -1, null);
    }

    Processo(String nome, int estado){
        this(nome, "", estado, null);
    }

    Processo(String nome, String indice, int estado){
        this(nome, indice, estado, null);
    }

    Processo(String nome, String indice, int estado, Range range){
        this.nome = nome;
        this.indice = indice;
        this.estado = estado;
        this.range = range;
        acoes = new ArrayList<Acao>();
    }

    public ArrayList<Acao> getAcoes(){
        return this.acoes;
    }

    public String getNome(){
        return this.nome;
    }

    public Range getRange(){
        return this.range;
    }

    public void setRange(Range range){
        this.range = range;
    }

    public boolean equals(Object o){
        Processo p = (Processo)o;
        boolean indexBool = (estado == -1 && estado == p.estado) || ( estado != -1 && estado == p.estado);
        return nome.equals(p.nome) && indexBool;
    }

    public String toString(){
        String str = this.getNome();
        if(!indice.equals(""))
            str += "["+indice+"="+estado+"]";
        if(range != null){
            str += ", range["+range.getInf()+".."+range.getSup()+"]";
        }
        return str;
    }

    public String getIndice(){
        return this.indice;
    }

    public int getEstado(){
        return this.estado;
    }

    public void printAcoes(){
        System.out.println("acoes de "+nome);
        for(int i=0;i<acoes.size();i++){
            System.out.println(acoes.get(i));
        }
    }

}