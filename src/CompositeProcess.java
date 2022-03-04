import java.util.ArrayList;

public class CompositeProcess {

    private String nome;
    private ArrayList<ProcessInstance> pis;
    private ArrayList<HiperGrafo> grafos;
    private ArrayList<ProcessThread> pthreads;

    CompositeProcess(String nome) {
        this.nome = nome;
        pis = new ArrayList<ProcessInstance>();
        grafos = new ArrayList<HiperGrafo>();
        pthreads = new ArrayList<ProcessThread>();
    }

    public String getNome() {
        return this.nome;
    }

    public ArrayList<HiperGrafo> getGrafos() {
        return this.grafos;
    }

    public ArrayList<ProcessInstance> getPis() {
        return this.pis;
    }

    public ArrayList<ProcessThread> getPthreads() {
        return this.pthreads;
    }
    
    public void concat(CompositeProcess cp) {
        this.pis.addAll(cp.pis);
        this.grafos.addAll(cp.grafos);
        this.pthreads.addAll(cp.pthreads);
    }

    public boolean equals(Object o) {
        CompositeProcess cp = (CompositeProcess)o;
        return this.nome.equals(cp.nome);
    }

}