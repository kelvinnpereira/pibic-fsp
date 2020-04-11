public class Const{

    private String nome;
    private int valor;

    Const(String nome, int valor){
        this.nome = nome;
        this.valor = valor;
    }

    public boolean equals(Object o){
        Const c = (Const)o;
        return nome.equals(c.nome);
    }

    public int getValor(){
        return this.valor;
    }

    public String getNome(){
        return this.nome;
    }

} 