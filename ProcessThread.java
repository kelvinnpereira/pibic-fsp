import java.util.ArrayList;

public class ProcessThread{

	private ArrayList<Processo> processos;
	private ArrayList<Const> constArray;
	private ArrayList<Range> rangeArray;
	private ArrayList<Acao> traceArray;
	private ArrayList<Acao> shared;
	private Processo primeiro;

	ProcessThread(ArrayList<Processo> processos, ArrayList<Const> constArray, ArrayList<Range> rangeArray, Processo primeiro){
		this.processos = processos;
		this.constArray = constArray;
		this.rangeArray = rangeArray;
		this.traceArray = new ArrayList<Acao>();
		this.shared = new ArrayList<Acao>();
		this.primeiro = primeiro;
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


}