public class Range{

    private String nome;
    private int inf, sup;

    Range(String nome, int inf, int sup){
        this.inf = inf;
        this.sup = sup;
        this.nome = nome;
    }

    Range(int inf, int sup){
        this("", inf, sup);
    }

    Range(String nome){
        this(nome, -1, -1);
    }

    public int getSup(){
        return this.sup;
    }

    public int getInf(){
        return this.inf;
    }

    public String getNome(){
        return this.nome;
    }

    public boolean equals(Object o){
        Range r = (Range)o;
        return nome.equals(r.nome);
    }

}