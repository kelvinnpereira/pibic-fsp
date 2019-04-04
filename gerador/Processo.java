import java.util.ArrayList;

public class Processo{

    private String nome;
    private ArrayList<Acao> acoes;
    private int index;
    private Range range;

    Processo(){
        this("", -1, null);
    }

    Processo(String nome){
        this(nome, -1, null);
    }

    Processo(String nome, int index){
        this(nome, index, null);
    }

    Processo(String nome, int index, Range range){
        this.nome = nome;
        this.index = index;
        this.range = range;
        acoes = new ArrayList<Acao>();
    }

    public ArrayList<Acao> getAcoes(){
        return this.acoes;
    }

    public String getNome(){
        return this.nome;
    }

    public void setRange(Range range){
        this.range = range;
    }

    public boolean equals(Object o){
        Processo p = (Processo)o;
        boolean indexBool = (index != -1 && p.index != -1 ) || (index == -1 && p.index == -1);
        return nome.equals(p.nome) && indexBool;
    }

    public String toString(){
        return this.getNome() +"["+ this.index+"]";
    }

    public int getIndex(){
        return this.index;
    }

    public void setIndex(int index) throws Error{
        if(index == -1)
            throw new Error("Indice nao inicializado!!");
        if(range != null && !range.indexValido(index) )
            throw new Error("Acesso de indice fora do range!!");
        this.index = index;
    }

    public void printAcoes(){
        System.out.println("acoes de "+nome);
        for(int i=0;i<acoes.size();i++){
            System.out.println(acoes.get(i));
        }
    }

}