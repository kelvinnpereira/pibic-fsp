public class ProcessoLocal{

	private String nome;
	private Processo processo;
	private int estado, id, valor_indice;

	ProcessoLocal(String nome, int estado, int valor_indice){
		this.nome = nome;
		this.estado = estado;
		this.valor_indice = valor_indice;
	}

	ProcessoLocal(String nome){
		this.nome = nome;
		this.estado = -1;
	}

	public int getEstado(){
		return this.estado;
	}

	public int getValorIndice(){
		return this.valor_indice;
	}

	public String getNome(){
		return this.nome;
	}

	public Processo getProcesso(){
		return this.processo;
	}

	public void setProcesso(Processo processo){
		this.processo = processo;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return this.id;
	}

	public String toString(){
		return nome+"["+valor_indice+"], estado: "+estado+", "+processo;
	}

	public boolean equals(Object o){
		ProcessoLocal pl = (ProcessoLocal)o;
		boolean expr = (valor_indice != -1 && pl.valor_indice != -1 ) || (valor_indice == -1 && pl.valor_indice == -1);
		return nome.equals(pl.nome) && estado == pl.estado && expr;
	}

}