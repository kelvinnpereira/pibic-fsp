

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;



public class Parser {
	public static final int _EOF = 0;
	public static final int _integer = 1;
	public static final int _uppercase_id = 2;
	public static final int _lowercase_id = 3;
	public static final int maxT = 43;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	public BufferedWriter buff;
    public ArrayList<Processo> processos;
    public ArrayList<ProcessoLocal> locais;
    public ArrayList<Acao> traceArray, acoes_atual = new ArrayList<Acao>();
    public ArrayList<Range> rangeArray;
    public ArrayList<Const> constArray;
    public Processo processo_atual, primeiro = null;
    public Acao acao_atual = null;
    public int sup, inf, valor = 0, valor_expr = -1, tam_trace = 0;
    public String index, expressao = "", bool = "";
    public HiperGrafo grafo;
    public boolean error, stop, seta, acao_inicio, conjunto;
    public Vertice vertice_atual;
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine eng = manager.getEngineByName("JavaScript");
    private InterfaceGrafica ig = new InterfaceGrafica();

    public void la(){
        System.out.println("la.val: "+la.val);
    }

    public void t(){
        System.out.println("t.val: "+t.val);
    }

    public void print(){
        System.out.println("--------------------------------------------------------------------");
        for(int i=0;i<processos.size();i++){
            System.out.println(processos.get(i));
            processos.get(i).printAcoes();
        }
        System.out.println("\nProcessoLocal");
        for(int i=0;i<locais.size();i++){
            System.out.println("locais "+locais.get(i));
        }
        System.out.println("\n"+grafo);
        System.out.println("--------------------------------------------------------------------");
    }

    public Processo novoProcesso(String nome, String indice, int valor, Range range){
        Processo p = new Processo(nome, indice, valor, range);
        if(!processos.contains(p)) 
            processos.add(p);
        return p;
    }

    public Acao novaAcao(String nome, String indice, int valor_indice, int estado){
        if(processo_atual == null) return null;
        Acao a = new Acao(nome, processo_atual, indice, valor_indice, estado);
        int i = processo_atual.getAcoes().lastIndexOf(a), num = 0;
        if(i != -1)
            a.setId(processo_atual.getAcoes().get(i).getId() + 1);
        processo_atual.getAcoes().add(a);
        ig.addCheckBox(nome + (valor_indice == -1 ? "" : "["+valor_indice+"]"));
        System.out.println(a);
        return processo_atual.getAcoes().get(processo_atual.getAcoes().lastIndexOf(a));
    }

    public Acao achaAcao(String nome, int estado, int valor_indice){
        for(int i=0;i<processos.size();i++){
            ArrayList<Acao> acoes = processos.get(i).getAcoes();
            for(int j=0;j<acoes.size();j++){
                if(acoes.get(j).getNome().equals(nome) && acoes.get(j).getEstado() == estado && acoes.get(j).getValorIndice() == valor_indice) 
                    return acoes.get(j);
            }
        }
        return null;
    }

    public void printTrace(){
        for(int i=0;i<traceArray.size();i++){
            System.out.println("trace: "+traceArray.get(i));
        }
    }

    public void novoTrace(String nome, int valor_indice) throws Error {
        Acao a;
        if(traceArray.size() == 0){
            int i = primeiro.getAcoes().indexOf(new Acao(nome, primeiro));
            if(i == -1) 
                throw new Error("Trace invalido!Acao nao elegivel ou nao existe!!");
            if(!primeiro.getAcoes().get(i).getInicio()) 
                throw new Error("Trace invalido!Acao nao elegivel ou nao existe!!");
            vertice_atual = grafo.busca(nome, primeiro.getAcoes().get(i).getId(), primeiro.getAcoes().get(i).getEstado(), valor_indice);
            a = primeiro.getAcoes().get(i);
        }else{
            vertice_atual = vertice_atual == null ? null : vertice_atual.buscaVizinho(nome, valor_indice, 0);
            System.out.println(nome +", "+(vertice_atual == null ? -1 : vertice_atual.getEstado())+", "+valor_indice);
            a = achaAcao(nome, vertice_atual == null ? -1 : vertice_atual.getEstado(), valor_indice);
        }
        if(a == null) throw new Error("Trace invalido! Acao nao elegivel ou nao existe!!");
        traceArray.add(a);
    }

    public boolean isNum(char c){
        return c >= '0' && c <= '9';
    }

    public void finalizaGrafo(){
        ArrayList<Vertice> vertices = grafo.getVertices();
        int i = 0;
        while(i<vertices.size()){
            Vertice v = vertices.get(i);
            int p_index = processos.indexOf(new Processo(v.getNome(), v.getValorIndice()));
            if(!v.getNome().equals("ERROR") && !v.getNome().equals("STOP") && v.getNome().charAt(0) >= 'A' && v.getNome().charAt(0) <= 'Z'){
                if(p_index != -1){
                    ArrayList<Acao> acoes = processos.get(p_index).getAcoes();
                    ArrayList<Aresta> arestas = v.getArestas();
                    for(int a=0;a<arestas.size();a++){
                        ArrayList<Vertice> vadj = arestas.get(a).getVertices();
                        for(int b=0;b<vadj.size();b++){
                            for(int j=0;j<acoes.size();j++){
                                if(acoes.get(j).getInicio() )
                                    grafo.insereAdj(vadj.get(b), grafo.buscaUltimo(acoes.get(j).getNome(), acoes.get(j).getId(), acoes.get(j).getEstado(), acoes.get(j).getValorIndice()), new Aresta());
                            }
                            vadj.get(b).remove(v);
                        }
                    }
                }
                grafo.getVertices().remove(v);
                i=0;
            }
            i++;
        }
    }

    public boolean andaTrace(Vertice v, int t){
        ArrayList<Aresta> arestas = v.getArestas();
        tam_trace = t+1;
        if(arestas.size() == 1 && v.buscaNome("STOP") != null) {
            stop = true;
            return true;
        }
        if(arestas.size() == 1 && v.buscaNome("ERROR") != null) {
            error = true;
            return true;
        }
        if(traceArray.size() == t+1) return true;
        for(int i=0;i<arestas.size();i++){
            ArrayList<Vertice> vadj = arestas.get(i).getVertices();
            for(int j=0;j<vadj.size();j++){
                if(traceArray.size() > t+1 && traceArray.get(t+1).getNome().equals(vadj.get(j).getNome()) && traceArray.get(t+1).getValorIndice() == vadj.get(j).getValorIndice() && andaTrace(vadj.get(j), t+1)) return true;
            }
        }
        return false;
    }

    public void verificaTrace() throws Error{
        if(traceArray.size() == 0) return;
        Acao acao = null;
        ArrayList<Acao> acoes = primeiro.getAcoes();
        for(int i=0;i<acoes.size()&&acao==null;i++){
            if( acoes.get(i).getInicio() && acoes.get(i).getNome().equals(traceArray.get(0).getNome()) && acoes.get(i).getValorIndice() == traceArray.get(0).getValorIndice())
                acao = acoes.get(i);      
        }
        if(acao == null){
            throw new Error("Trace invalido!!");
        }
        ArrayList<Vertice> vertices = grafo.getVertices();
        for(int i=0;i<vertices.size();i++){
            if(vertices.get(i).getNome().equals(acao.getNome()) && vertices.get(i).getValorIndice() == acao.getValorIndice() && andaTrace(vertices.get(i), 0)) return;
        }
        if(true)throw new Error("Trace invalido!!");
    }

    public String tiraIndice(String expr, String indice, String valor){
        if(expr.equals("") || indice.equals("") || valor.equals("")) return expr;
        return expr.contains(indice) ? expr.replace(indice, valor) : expr;
    }

    public boolean exprBool(String expr){
        if(!expr.equals("")){
            try{
                return (boolean)eng.eval(expr);
            }catch(Exception e){
                System.out.println("Erro de expressao em: "+expr);
                System.exit(1);
            }
        }
        return true;
    }

    public int calcExpr(String expr){
        if(!expr.equals("")){
            try{
                return (int)eng.eval(expr);
            }catch(Exception e){
                System.out.println("Erro de expressao em: "+expr);
                System.exit(1);
            }
        }
        return -1;
    }



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void FSP() {
		processos = new ArrayList<Processo>();
		traceArray = new ArrayList<Acao>();
		rangeArray = new ArrayList<Range>();
		constArray = new ArrayList<Const>();
		locais = new ArrayList<ProcessoLocal>();
		grafo = new HiperGrafo();
		inf = sup = -1;
		
		start();
		while (StartOf(1)) {
			start();
		}
		finalizaGrafo();
		
		trace();
		print();
		ig.start_interface();
		try{
		   verificaTrace();
		   
		   int i;
		   Processo p, main = processos.get(0);
		   buff = new BufferedWriter(new FileWriter("Constantes"+main.getNome()+".java"));
		   buff.append(
		       "public class Constantes"+main.getNome()+"{\n\n"
		   );
		   for(i=0;i<constArray.size();i++){
		       buff.append(
		           "    public static final int "+constArray.get(i).getNome()+" = "+constArray.get(i).getValor()+";\n"
		       );
		   }
		   buff.append("}");
		   buff.close();
		    buff = new BufferedWriter(new FileWriter("Ranges"+main.getNome()+".java"));
		   buff.append(
		       "public class Ranges"+main.getNome()+"{\n\n"
		   );
		   for(i=0;i<rangeArray.size();i++){
		       buff.append(
		           "    public static final Range "+rangeArray.get(i).getNome()+" = new Range("+rangeArray.get(i).getInf()+", "+rangeArray.get(i).getSup()+");\n"
		       );
		   }
		   buff.append("}");
		   buff.close();
		    for(i=0;i<processos.size();i++){
		       p = processos.get(i);
		       /*cria um arquivo .java com o nome do processo.*/
		       String nomeP = p.getNome()+(p.getEstado() == -1 ? "": "_"+p.getEstado());
		       buff = new BufferedWriter(new FileWriter(nomeP+".java"));
		       buff.append(
		           /*o nome da classe e o proprio do processo.*/
		           "public class "+nomeP+"{\n\n"+
		           /*contrutor da classe.*/
		           "    "+nomeP+"(){\n"+
		           "    }\n\n"
		       );
		       /*cada acao sera tranformada em um metodo da classe.*/
		       ArrayList<Acao> acoes = p.getAcoes();
		       for(int j=0;j<acoes.size();j++){
		           String nomeA = acoes.get(j).getNome()+(acoes.get(j).getValorIndice() == -1 ? "": "_"+acoes.get(j).getValorIndice());
		           buff.append(
		               "    public void "+nomeA+"(){\n"+
		               "        System.out.println(\""+acoes.get(j).getNome()+
		                       (acoes.get(j).getValorIndice() != -1 ? "["+acoes.get(j).getValorIndice()+"]":"")+"\");\n"+
		               "    }\n\n"
		           );
		       }
		       buff.append("}");
		       buff.close();
		   }
		   /*cria um arquivo .java com o nome Main+main.nome, que eh o nome do processo principal.*/
		   buff = new BufferedWriter(new FileWriter("Main"+main.getNome()+".java"));
		   /*nome da classe com o mesmo nome do arquivo.*/
		   buff.append(
		       "public class Main"+main.getNome()+" implements Runnable{\n\n"+
		       /*thread com o nome thread+main.nome.*/
		       "    Thread thread"+main.getNome()+";\n\n"
		   );
		   /*adiciona todos os objetos de cada classe(processo) como atributo da classe principal*/
		   for(i=0;i<processos.size();i++){
		       p = processos.get(i);
		       String nomeP = p.getNome()+(p.getEstado() == -1 ? "": "_"+p.getEstado());
		       buff.append("    "+nomeP+" obj_"+nomeP.toLowerCase()+";\n\n");
		   }
		   /*construtor da classe principal.*/
		   buff.append("    Main"+main.getNome()+"(){\n");
		   /*instancia cada atributo da classe principal.*/
		   for(i=0;i<processos.size();i++){
		       p = processos.get(i);
		       String nomeP = p.getNome()+(p.getEstado() == -1 ? "": "_"+p.getEstado());
		       buff.append(
		           "        obj_"+nomeP.toLowerCase()+" = new "+nomeP+"();\n"
		       );
		   }
		   buff.append(
		       /*instancia a thread*/
		       "        thread"+main.getNome()+" = new Thread(this);\n"+
		       /*inicia a execucao da thread*/
		       "        thread"+main.getNome()+".start();\n"+
		       "    }\n\n"+
		       /*execucao do programa com o metodo run*/
		       "    public void run(){\n"+
		       "        try{\n"+
		       "            while(true){\n"+
		       "                Thread.sleep(500);\n"
		   );
		   for(i=0;i<tam_trace&&i<traceArray.size();i++){
		       String nomeP = traceArray.get(i).getProcesso().getNome().toLowerCase();
		       nomeP += (traceArray.get(i).getProcesso().getEstado() == -1 ? "" : "_"+traceArray.get(i).getProcesso().getEstado());
		       String nomeA = traceArray.get(i).getNome()+(traceArray.get(i).getValorIndice() == -1 ? "" : "_"+traceArray.get(i).getValorIndice());
		       buff.append(
		           "                obj_"+nomeP+"."+nomeA+"();\n"+
		           "                Thread.sleep(1000);\n"
		       );
		   }
		   if(stop){
		       buff.append(
		           "                System.out.println(\"STOP\");\n"
		       );
		   }
		   if(error){
		       buff.append(
		           "                System.out.println(\"ERROR\");\n"+
		           "                throw new Error(\"Chamada do processo ERROR\");\n"
		       );
		   }else{
		       buff.append(
		           "                System.exit(1);\n"
		       );
		   }
		   buff.append(
		       "            }\n"+
		       "        }catch(Exception e){}\n"+
		       "    }\n\n"+
		       /*metodo main que instancia a classe principal*/
		       "    public static void main(String args[]){\n"+
		       "        Main"+main.getNome()+" main = new Main"+main.getNome()+"();\n"+
		       "    }\n\n"+
		       "}"
		   );
		   buff.close();
		}catch(Exception e){
		   System.out.println("Execessao: "+e.toString());
		}
		
	}

	void start() {
		if (la.kind == 2) {
			primitive_process();
		} else if (la.kind == 22) {
			constant_declaration();
		} else if (la.kind == 24) {
			range_declaration();
		} else if (la.kind == 19) {
			composite_process();
		} else SynErr(44);
	}

	void trace() {
		Expect(4);
		if (la.kind == 3) {
			trace_action();
			while (la.kind == 5) {
				Get();
				trace_action();
			}
		}
		Expect(6);
	}

	void trace_action() {
		String nome = la.val;
		
		Expect(3);
		if (la.kind == 26) {
			index_label();
		}
		try{
		   int temp = calcExpr(expressao);
		   novoTrace(nome, temp);
		}catch(Error e){
		   System.out.println(e.toString());
		   System.exit(1);
		}
		
	}

	void primitive_process() {
		String nome = la.val;
		
		Expect(2);
		if (la.kind == 11) {
			Get();
			parameter_list();
			Expect(12);
		}
		if (la.kind == 26) {
			index_label();
		}
		int indexPl = locais.indexOf(new ProcessoLocal(nome, -1, 0));
		if(indexPl != -1 && inf != -1 && sup != -1){
		   Processo p = null;
		   for(int i=inf;i<=sup;i++){
		       p = novoProcesso(nome, index, i, new Range(inf, sup));
		       if(i == locais.get(indexPl).getValorIndice()) processo_atual = p;
		   }
		   valor_expr = inf = sup = -1;
		}else{
		   processo_atual = novoProcesso(nome, "", -1, null);
		}
		acao_atual = null;
		seta = acao_inicio = true;
		expressao = "";
		
		Expect(23);
		if (la.kind == 2 || la.kind == 37 || la.kind == 38) {
			local_process();
		} else if (la.kind == 11) {
			Get();
			primitive_process_body();
			Expect(12);
		} else SynErr(45);
		if (la.kind == 29) {
			Get();
		} else if (la.kind == 31) {
			Get();
		} else SynErr(46);
	}

	void constant_declaration() {
		Expect(22);
		String nome = la.val;
		
		Expect(2);
		Expect(23);
		Const c = new Const(nome, Integer.parseInt(la.val));
		if(!constArray.contains(c))
		   constArray.add(c);
		
		expr();
	}

	void range_declaration() {
		Expect(24);
		String nome = la.val;
		
		Expect(2);
		Expect(23);
		int infLocal = -1;
		if( isNum(la.val.charAt(0)) ){
		   infLocal = Integer.parseInt(la.val);
		}else{
		   int index = constArray.indexOf(new Const(la.val, 0));
		   if(index != -1)
		       infLocal = constArray.get(index).getValor();
		}
		
		expr();
		Expect(25);
		int supLocal = -1;
		if( isNum(la.val.charAt(0)) ){
		   supLocal = Integer.parseInt(la.val);
		}else{
		   int index = constArray.indexOf(new Const(la.val, 0));
		   if(index != -1)
		       supLocal = constArray.get(index).getValor();
		}
		
		expr();
		Range r = new Range(nome, infLocal, supLocal);
		if(!rangeArray.contains(r))
		   rangeArray.add(r);
		r = rangeArray.get(rangeArray.size()-1);
		
	}

	void composite_process() {
		Expect(19);
		Expect(2);
		if (la.kind == 11) {
			Get();
			parameter_list();
			Expect(12);
		}
		Expect(23);
		composite_body();
		if (la.kind == 33 || la.kind == 34) {
			label_visibility();
		}
		if (la.kind == 10) {
			relabels();
		}
		Expect(29);
	}

	void expr() {
		term();
		while (la.kind == 7 || la.kind == 8) {
			expressao += la.val;
			if (la.kind == 7) {
				Get();
			} else {
				Get();
			}
			term();
		}
	}

	void term() {
		factor();
		while (la.kind == 9 || la.kind == 10) {
			expressao += la.val;
			if (la.kind == 9) {
				Get();
			} else {
				Get();
			}
			factor();
		}
	}

	void factor() {
		if(la.val.charAt(0) >= 'A' && la.val.charAt(0) <= 'Z' )
		   expressao += ""+constArray.get(constArray.indexOf(new Const(la.val, 0))).getValor();
		else
		   expressao += la.val;
		
		if (la.kind == 11) {
			Get();
			expr();
			if(la.val.charAt(0) == '(' || la.val.charAt(0) == ')' )
			      expressao += la.val;
			
			Expect(12);
		} else if (la.kind == 2) {
			Get();
		} else if (la.kind == 3) {
			Get();
		} else if (la.kind == 1) {
			Get();
		} else SynErr(47);
	}

	void boolean_expr() {
		expressao = "";
		expr();
		bool += expressao;
		while (StartOf(2)) {
			bool += la.val;
			switch (la.kind) {
			case 13: {
				Get();
				break;
			}
			case 14: {
				Get();
				break;
			}
			case 15: {
				Get();
				break;
			}
			case 16: {
				Get();
				break;
			}
			case 17: {
				Get();
				break;
			}
			case 18: {
				Get();
				break;
			}
			case 19: {
				Get();
				break;
			}
			case 20: {
				Get();
				break;
			}
			case 21: {
				Get();
				break;
			}
			}
			expressao = "";
			expr();
			bool += expressao;
		}
	}

	void index() {
		Expect(26);
		index = la.val;
		expressao = "";
		
		expr();
		while (la.kind == 27) {
			Get();
			String id = la.val;
			expressao = "";
			
			expr();
			int i = rangeArray.indexOf(new Range(la.val));
			if(i != -1){
			   inf = rangeArray.get(i).getInf();
			   sup = rangeArray.get(i).getSup();
			}else{
			   inf = calcExpr(expressao);
			}
			expressao = "";
			
			while (la.kind == 25) {
				Get();
				expr();
				sup = calcExpr(expressao);
				
			}
		}
		Expect(28);
	}

	void index_label() {
		index();
		while (la.kind == 26) {
			index();
		}
	}

	void simple_action() {
		String str = la.val;
		
		Expect(3);
		if (la.kind == 26) {
			index_label();
		}
		if(primeiro == null)
		   primeiro = processo_atual;
		int inicio = processo_atual.getRange() == null ? -1 : processo_atual.getRange().getInf();
		int pa = processos.indexOf( new Processo(processo_atual.getNome(), inicio)), pa_2 = pa, valor_expr = -1;
		boolean valor_bool = true;
		Range r = processo_atual.getRange();
		int i = r != null ? r.getSup() - r.getInf() + 1 : 1;
		Acao a = null;
		Vertice v = null;
		while(i-- != 0){
		   processo_atual = processos.get(pa);
		   if(exprBool(tiraIndice(bool, processos.get(pa).getIndice(), processos.get(pa).getEstado()+""))){
		       if(inf != - 1 && sup != -1){
		           if(acoes_atual.size() == 0){
		               for(int cont=inf;cont<=sup;cont++){
		                   a = novaAcao(str, index, cont, processo_atual.getEstado());
		                   if(acao_inicio) a.setInicio(true);
		                   v = grafo.insereVertice(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice());
		                   acoes_atual.add(a);
		               }
		           }else{
		               ArrayList<Acao> acoes_atual_temp = new ArrayList<Acao>();
		               for(int cont=0;cont<acoes_atual.size();cont++){
		                   a = novaAcao(str, acoes_atual.get(cont).getIndice(), acoes_atual.get(cont).getValorIndice(), processo_atual.getEstado());
		                   if(acao_inicio) a.setInicio(true);
		                   v = grafo.insereVertice(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice());
		                   valor_expr = calcExpr(tiraIndice(expressao, acoes_atual.get(cont).getIndice(), acoes_atual.get(cont).getValorIndice()+""));
		                   grafo.insereAdj(grafo.busca(acoes_atual.get(cont).getNome(), acoes_atual.get(cont).getId(), acoes_atual.get(cont).getEstado(), acoes_atual.get(cont).getValorIndice()), v, new Aresta());
		                   acoes_atual_temp.add(a);
		               }
		               acoes_atual = acoes_atual_temp;
		           }
		       }else{
		           valor_expr = calcExpr(tiraIndice(expressao, processos.get(pa).getIndice(), processos.get(pa).getEstado()+""));
		           a = novaAcao(str, processo_atual.getIndice(), valor_expr, processo_atual.getEstado());
		           v = grafo.insereVertice(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice());
		           if(acao_atual != null && r == null){
		               grafo.insereAdj(grafo.busca(acao_atual.getNome(), acao_atual.getId(), acao_atual.getEstado(), acao_atual.getValorIndice()), v, new Aresta());
		           }
		           if(!conjunto && r == null)
		               for(int cont=0;cont<acoes_atual.size();cont++){
		                   valor_expr = calcExpr(tiraIndice(expressao, acoes_atual.get(cont).getIndice(), acoes_atual.get(cont).getValorIndice()+""));
		                   grafo.insereAdj(grafo.busca(acoes_atual.get(cont).getNome(), acoes_atual.get(cont).getId(), acoes_atual.get(cont).getEstado(), acoes_atual.get(cont).getValorIndice()), v, new Aresta());
		               }
		           if(!conjunto) acoes_atual = new ArrayList<Acao>();
		           acoes_atual.add(a);
		       }
		       if(acao_inicio) a.setInicio(true);
		       if(!conjunto) acao_atual = a;
		   }
		   pa++;
		}
		expressao = "";
		bool = "";
		processo_atual = processos.get(pa_2);
		if(!conjunto) acao_inicio = false;
		
	}

	void action() {
		simple_action();
		while (la.kind == 29) {
			Get();
			simple_action();
		}
	}

	void action_set() {
		conjunto = true;
		Expect(30);
		action();
		while (la.kind == 31) {
			Get();
			action();
		}
		Expect(32);
		conjunto = false;
	}

	void alphabet_extension() {
		Expect(7);
		action_set();
	}

	void label_visibility() {
		if (la.kind == 33) {
			hide_label();
		} else if (la.kind == 34) {
			expose_label();
		} else SynErr(48);
	}

	void hide_label() {
		Expect(33);
		action_set();
	}

	void expose_label() {
		Expect(34);
		action_set();
	}

	void relabels() {
		Expect(10);
		relabel_set();
	}

	void relabel_set() {
		Expect(30);
		relabel();
		while (la.kind == 31) {
			Get();
			relabel();
		}
		Expect(32);
	}

	void relabel() {
		if (la.kind == 3) {
			simple_relabel();
		} else if (la.kind == 35) {
			Get();
			index();
			relabel_set();
		} else SynErr(49);
	}

	void simple_relabel() {
		action();
		Expect(10);
		action();
	}

	void parameter_list() {
		parameter();
		while (la.kind == 31) {
			Get();
			parameter();
		}
	}

	void local_process() {
		String nome = la.val;
		
		if (la.kind == 2) {
			Get();
			if (la.kind == 26) {
				index();
			}
			int pa = processos.indexOf(processo_atual), pa_2 = pa, temp;
			Range r = processo_atual.getRange();
			int i = r != null ? r.getSup() - r.getInf() + 1 : 1;
			ProcessoLocal pl = null;
			Vertice v = null;
			while(i-- != 0 && r != null && pa != -1 && pa < processos.size()){
			   processo_atual = processos.get(pa);
			   if(exprBool(tiraIndice(bool, processos.get(pa).getIndice(), processos.get(pa).getEstado()+""))){
			       valor_expr = calcExpr(tiraIndice(expressao, processos.get(pa).getIndice(), processos.get(pa).getEstado()+""));
			       if(valor_expr >= r.getInf() && valor_expr <= r.getSup()){
			           pl = new ProcessoLocal(nome, processo_atual.getEstado(), valor_expr);
			           int j = locais.lastIndexOf(pl);
			           if(j == -1)
			               locais.add(pl);
			           v = grafo.insereVertice(nome, 0, processo_atual.getEstado(), valor_expr);
			           if(acao_atual != null){
			               int k = processos.get(pa).getAcoes().indexOf(new Acao(acao_atual.getNome(), processos.get(pa)));
			               if(k != -1){
			                   Acao a = processos.get(pa).getAcoes().get(k);
			                   a.setPl(pl);
			                   grafo.insereAdjSimetrica(grafo.busca(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice()), v);
			               }
			           }
			       }else if(exprBool(tiraIndice(bool, processos.get(pa).getIndice(), valor_expr+""))){
			           if(acao_atual != null){
			               int k = processos.get(pa).getAcoes().indexOf(new Acao(acao_atual.getNome(), processos.get(pa)));
			               if(k != -1){
			                   Acao a = processos.get(pa).getAcoes().get(k);
			                   grafo.insereAdj(grafo.busca(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice()), grafo.insereVertice("ERROR", 0, -1, -1), new Aresta());
			               }
			           }
			       }
			   }
			   pa++;
			}
			processo_atual = processos.get(pa_2);
			if(processo_atual.getIndice().equals("")){
			   valor_expr = calcExpr(tiraIndice(expressao, processos.get(pa).getIndice(), processos.get(pa).getEstado()+""));
			   pl = new ProcessoLocal(nome, processo_atual.getEstado(), valor_expr);
			   int j = locais.lastIndexOf(pl);
			   if(j == -1){
			       locais.add(pl);
			       v = grafo.insereVertice(nome, 0, processo_atual.getEstado(), valor_expr);
			   }else{
			       v = grafo.busca(nome, 0, processo_atual.getEstado(), valor_expr);
			       pl = locais.get(j);
			   }
			   for(i=0;i<acoes_atual.size();i++){
			       grafo.insereAdjSimetrica(grafo.buscaUltimo(acoes_atual.get(i).getNome(), acoes_atual.get(i).getId(), acoes_atual.get(i).getEstado(), acoes_atual.get(i).getValorIndice()), v);
			       acoes_atual.get(i).setPl(pl);
			   }
			}
			acao_atual = null;
			acoes_atual = new ArrayList<Acao>();
			expressao = "";
			
		} else if (la.kind == 37 || la.kind == 38) {
			if(primeiro == null)
			   primeiro = processo_atual;
			int inicio = processo_atual.getRange() == null ? -1 : processo_atual.getRange().getInf();
			int pa = processos.indexOf( new Processo(processo_atual.getNome(), inicio)), pa_2 = pa, valor_expr = -1;
			boolean valor_bool = true;
			Range r = processo_atual.getRange();
			int i = r != null ? r.getSup() - r.getInf() + 1 : 1;
			Acao a = null;
			while(i-- != 0){
			   processo_atual = processos.get(pa);
			   if(exprBool(tiraIndice(bool, processos.get(pa).getIndice(), processos.get(pa).getEstado()+""))){
			       a = novaAcao(la.val, "", -1, processo_atual.getEstado());
			       Vertice v = grafo.insereVertice(a.getNome(), 0, -1, -1);
			       if(acao_atual != null){
			           if(acoes_atual.size() > 1){
			               for(int ia=0;ia<acoes_atual.size();ia++){
			                   grafo.insereAdj(grafo.busca(acoes_atual.get(ia).getNome(), acoes_atual.get(ia).getId(), acoes_atual.get(ia).getEstado(), acoes_atual.get(ia).getValorIndice()), v, new Aresta());
			               }
			           }else{
			               int k = processos.get(pa).getAcoes().indexOf(new Acao(acao_atual.getNome(), processos.get(pa)));
			               if(k != -1){
			                   a = processos.get(pa).getAcoes().get(k);
			                   grafo.insereAdj(grafo.busca(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice()), v, new Aresta());
			               }
			           }
			       }else{
			           a.setInicio(true);
			       }
			       if(r == null)acoes_atual.add(a);
			   }
			   pa++;
			}
			acao_atual = a;
			processo_atual = processos.get(pa_2);
			
			if (la.kind == 37) {
				Get();
			} else {
				Get();
			}
		} else SynErr(50);
	}

	void primitive_process_body() {
		process_body();
		while (la.kind == 5 || la.kind == 36) {
			if(la.val.equals("|"))
			   acao_inicio = true;
			
			if (la.kind == 5) {
				Get();
			} else {
				Get();
			}
			process_body();
		}
		if (la.kind == 7) {
			alphabet_extension();
		}
		if (la.kind == 33 || la.kind == 34) {
			label_visibility();
		}
		if (la.kind == 10) {
			relabels();
		}
	}

	void parameter() {
		String nome = la.val;
		
		Expect(2);
		Expect(23);
		Const c = new Const(nome, Integer.parseInt(la.val));
		if(!constArray.contains(c))
		   constArray.add(c);
		
		Expect(1);
	}

	void process_body() {
		if (la.kind == 3 || la.kind == 30 || la.kind == 39) {
			choice();
		} else if (la.kind == 2 || la.kind == 37 || la.kind == 38) {
			local_process();
		} else if (la.kind == 40) {
			condition();
		} else SynErr(51);
	}

	void choice() {
		if (la.kind == 39) {
			Get();
			boolean_expr();
			Expect(12);
		}
		expressao = "";
		
		if (la.kind == 30) {
			action_set();
		} else if (la.kind == 3) {
			action();
		} else SynErr(52);
		seta = true;
		Expect(5);
		process_body();
	}

	void condition() {
		Expect(40);
		boolean_expr();
		Expect(41);
		process_body();
		Expect(42);
		process_body();
	}

	void composite_body() {
		if (la.kind == 2 || la.kind == 3) {
			process_instance();
		} else if (la.kind == 11) {
			parallel_list();
		} else if (la.kind == 40) {
			composite_conditional();
		} else if (la.kind == 35) {
			compositi_replicator();
		} else SynErr(53);
	}

	void process_instance() {
		if (la.kind == 3) {
			action();
			Expect(27);
		}
		Expect(2);
		if (la.kind == 11) {
			Get();
			actual_parameter_list();
			Expect(12);
		}
	}

	void parallel_list() {
		Expect(11);
		composite_body();
		while (la.kind == 19) {
			Get();
			composite_body();
		}
		Expect(12);
	}

	void composite_conditional() {
		Expect(40);
		boolean_expr();
		Expect(41);
		composite_body();
		Expect(42);
		composite_body();
	}

	void compositi_replicator() {
		Expect(35);
		index();
		composite_body();
	}

	void actual_parameter_list() {
		expr();
		while (la.kind == 31) {
			Get();
			expr();
		}
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		FSP();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_x,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_T, _x,_x,_T,_x, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_T,_T, _T,_T,_T,_T, _T,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "integer expected"; break;
			case 2: s = "uppercase_id expected"; break;
			case 3: s = "lowercase_id expected"; break;
			case 4: s = "\"TRACE\" expected"; break;
			case 5: s = "\"->\" expected"; break;
			case 6: s = "\"ENDTRACE\" expected"; break;
			case 7: s = "\"+\" expected"; break;
			case 8: s = "\"-\" expected"; break;
			case 9: s = "\"*\" expected"; break;
			case 10: s = "\"/\" expected"; break;
			case 11: s = "\"(\" expected"; break;
			case 12: s = "\")\" expected"; break;
			case 13: s = "\">\" expected"; break;
			case 14: s = "\"<\" expected"; break;
			case 15: s = "\">=\" expected"; break;
			case 16: s = "\"<=\" expected"; break;
			case 17: s = "\"==\" expected"; break;
			case 18: s = "\"&&\" expected"; break;
			case 19: s = "\"||\" expected"; break;
			case 20: s = "\"!\" expected"; break;
			case 21: s = "\"!=\" expected"; break;
			case 22: s = "\"const\" expected"; break;
			case 23: s = "\"=\" expected"; break;
			case 24: s = "\"range\" expected"; break;
			case 25: s = "\"..\" expected"; break;
			case 26: s = "\"[\" expected"; break;
			case 27: s = "\":\" expected"; break;
			case 28: s = "\"]\" expected"; break;
			case 29: s = "\".\" expected"; break;
			case 30: s = "\"{\" expected"; break;
			case 31: s = "\",\" expected"; break;
			case 32: s = "\"}\" expected"; break;
			case 33: s = "\"\\\\\" expected"; break;
			case 34: s = "\"@\" expected"; break;
			case 35: s = "\"forall\" expected"; break;
			case 36: s = "\"|\" expected"; break;
			case 37: s = "\"STOP\" expected"; break;
			case 38: s = "\"ERROR\" expected"; break;
			case 39: s = "\"when(\" expected"; break;
			case 40: s = "\"if\" expected"; break;
			case 41: s = "\"then\" expected"; break;
			case 42: s = "\"else\" expected"; break;
			case 43: s = "??? expected"; break;
			case 44: s = "invalid start"; break;
			case 45: s = "invalid primitive_process"; break;
			case 46: s = "invalid primitive_process"; break;
			case 47: s = "invalid factor"; break;
			case 48: s = "invalid label_visibility"; break;
			case 49: s = "invalid relabel"; break;
			case 50: s = "invalid local_process"; break;
			case 51: s = "invalid process_body"; break;
			case 52: s = "invalid choice"; break;
			case 53: s = "invalid composite_body"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
