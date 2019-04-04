public class Acao{

    private String nome;
    private Processo processo;
    private int index;

    Acao(){
        this("", null, -1);
    }

    Acao(String nome){
        this(nome, null, -1);
    }

    Acao(String nome, Processo processo){
        this(nome, processo, -1);
    }

    Acao(String nome, Processo processo, int index){
        this.nome = nome;
        this.processo = processo;
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }

    public String getNome(){
        return this.nome;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public Processo getProcesso(){
        return this.processo;
    }

    public boolean equals(Object o){
        Acao a = (Acao)o;
        return nome.equals(a.nome) && processo.equals(a.processo);
    }

    public String toString(){
        return new String(nome+"["+index+"]");
    }
}