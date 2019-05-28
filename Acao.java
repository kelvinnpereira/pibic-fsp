import java.util.ArrayList;

public class Acao{

    private String nome, expressao, indice;
    private ArrayList<Acao> acoes_atuais;
    private Processo processo;
    private ProcessoLocal pl;
    private boolean inicio, compartilhada;
    private int id, estado, valor_indice;

    Acao(String nome, Processo processo){
        this.nome = nome;
        this.processo = processo;
        this.estado = -1;
    }

    Acao(String nome, Processo processo, String indice, int valor_indice, int estado){
        this.nome = nome;
        this.processo = processo;
        this.indice = indice;
        this.valor_indice = valor_indice;
        this.estado = estado;
        acoes_atuais = new ArrayList<Acao>();
    }

    public ArrayList<Acao> getAcoesAtuais(){
        return this.acoes_atuais;
    }

    public void setAcoesAtuais(ArrayList<Acao> acoes_atuais){
        this.acoes_atuais = acoes_atuais;
    }

    public void setPl(ProcessoLocal pl){
        this.pl = pl;
    }

    public void setExpr(String expr){
        this.expressao = expr;
    }

    public String getExpr(){
        return this.expressao;
    }

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public boolean getInicio(){
        return this.inicio;
    }

    public void setInicio(boolean valor){
        this.inicio = valor;
    }

    public String getNome(){
        return this.nome;
    }

    public Processo getProcesso(){
        return this.processo;
    }

    public int getEstado(){
        return this.estado;
    }

    public int getValorIndice(){
        return this.valor_indice;
    }

    public void setEstado(int e){
        this.estado = e;
    }

    public ProcessoLocal getPl(){
        return this.pl;
    }

    public String getIndice(){
        return this.indice;
    }

    public boolean getCompartilhada(){
        return this.compartilhada;
    }

    public void setCompartilhada(boolean val){
        this.compartilhada = val;
    }

    public boolean equals(Object o){
        Acao a = (Acao)o;
        return nome.equals(a.nome) && processo.equals(a.processo);
    }

    public String toString(){
        return nome + "["+valor_indice+"], id:  "+id+", estado: "+estado+", "+processo.toString()+", shared: "+compartilhada;
    }
}