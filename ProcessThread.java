import java.util.ArrayList;

public class ProcessThread{

	private ArrayList<Processo> processos;
	private ArrayList<Const> constArray;
	private ArrayList<Range> rangeArray;
	private ArrayList<Acao> traceArray;
	private ArrayList<Acao> shared;
	private Processo primeiro;
	String prefix;

	ProcessThread(ArrayList<Processo> processos, ArrayList<Const> constArray, ArrayList<Range> rangeArray, Processo primeiro){
		this.processos = processos;
		this.constArray = constArray;
		this.rangeArray = rangeArray;
		this.traceArray = new ArrayList<Acao>();
		this.shared = new ArrayList<Acao>();
		this.primeiro = primeiro;
		prefix = "";
	}

	public ArrayList<Processo> getProcessos(){
		return this.processos;
	}

	public ArrayList<Const> getConstArray(){
		return this.constArray;
	}

	public ArrayList<Range> getRangeArray(){
		return this.rangeArray;
	}

	public Processo getPrimeiro(){
		return this.primeiro;
	}

	public ArrayList<Acao> getTraceArray(){
		return this.traceArray;
	}

	public ArrayList<Acao> getShared(){
		return this.shared;
	}

	public Object clone(){
		ArrayList<Processo> p = new ArrayList<Processo>();
		ProcessThread pt = new ProcessThread(p , constArray, rangeArray, primeiro);
		for(int i=0;i<processos.size();i++)
			p.add((Processo)processos.get(i).clone());
		return pt;
	}

	public void renameAll(String prefix){
		ArrayList<Processo> p = this.getProcessos();
        for(int i=0;i<p.size();i++){
            ArrayList<Acao> acoes = p.get(i).getAcoes();
            for(int j=0;j<acoes.size();j++){
                acoes.get(j).setNome(prefix+acoes.get(j).getNome());
            }
        }
	}

}