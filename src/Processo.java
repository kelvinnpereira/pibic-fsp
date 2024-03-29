import java.util.ArrayList;

public class Processo{

    private String nome;
    private String indice;
    private ArrayList<Acao> acoes;
    private ArrayList<Acao> acoes_atuais;
    private int estado;
    private Range range;
    private Processo processoLocal;

    Processo(String nome, int estado) {
        this(nome, "", estado, null);
    }

    Processo(String nome, String indice, int estado, Range range){
        this.nome = nome;
        this.indice = indice;
        this.estado = estado;
        this.range = range;
        acoes = new ArrayList<Acao>();
        acoes_atuais = new ArrayList<Acao>();
    }

    public ArrayList<Acao> getAcoes(){
        return this.acoes;
    }

    public void setAcoes(ArrayList<Acao> acoes){
        this.acoes = acoes;
    }

    public ArrayList<Acao> getAcoesAtuais(){
        return this.acoes_atuais;
    }

    public void setAcoesAtuais(ArrayList<Acao> acoes_atuais){
        this.acoes_atuais = acoes_atuais;
    }

    public Processo getProcessoLocal() {
        return this.processoLocal;
    }

    public void setProcessoLocal(Processo pl) {
        this.processoLocal = pl;
    }

    public String getNome(){
        return this.nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public Range getRange(){
        return this.range;
    }
    
    public boolean equals(Object o){
        Processo p = (Processo)o;
        return nome.equals(p.nome) && estado == p.estado;
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
        System.out.println("acoes de " + nome);
        for(Acao acao: acoes){
            System.out.println(acao);
        }
    }

    public void printAcoesAtuais(){
        System.out.println("acoes atuais de " + nome);
        for(Acao acao: acoes_atuais){
            System.out.println(acao);
        }
    }

    public Object clone(){
        Processo p = new Processo(nome, indice, estado, range);
        ArrayList<Acao> a = new ArrayList<Acao>();
        for(int i=0;i<acoes.size();i++){
            a.add((Acao)acoes.get(i).clone());
        }
        p.setAcoes(a);
        return p;
    }

}