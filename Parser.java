

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.swing.*;



public class Parser {
	public static final int _EOF = 0;
	public static final int _integer = 1;
	public static final int _uppercase_id = 2;
	public static final int _lowercase_id = 3;
	public static final int maxT = 42;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	public ArrayList<Processo> processos;
    public ArrayList<ProcessoLocal> locais;
    public ArrayList<Range> rangeArray;
    public ArrayList<Const> constArray;
    public ArrayList<String> sharing_set;
    public ArrayList<ProcessInstance> pi;
    public Processo processo_atual, primeiro_atual;
    public Acao acao_atual;
    public int sup, inf, valor_expr;
    public String index, expressao = "", bool = "", composite_process_name = "", prefix;
    public ArrayList<HiperGrafo> grafoArray = new ArrayList<HiperGrafo>();
    public HiperGrafo grafo;
    public boolean acao_inicio, conjunto;
    public Vertice vertice_atual;
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine eng = manager.getEngineByName("JavaScript");
    private ArrayList<ProcessThread> pthreadArray = new ArrayList<ProcessThread>(), pthreadArrayInstance = new ArrayList<ProcessThread>();
    private Geracao generator = new Geracao(pthreadArrayInstance);
    private Trace trace;
    private InterfaceGrafica ig;

    public void init(){
        processos = new ArrayList<Processo>();
        locais = new ArrayList<ProcessoLocal>();
        rangeArray = new ArrayList<Range>();
        constArray = new ArrayList<Const>();
        processo_atual = primeiro_atual = null;
        acao_atual = null;
        inf = sup = valor_expr = -1;
        index = expressao = bool = "";
        grafo = new HiperGrafo();
        grafoArray.add(grafo);
        acao_inicio = conjunto = false;
        vertice_atual = null;
    }

    public void setTrace(Trace trace){
        this.trace = trace;
    }

    public void setIg(InterfaceGrafica ig){
        this.ig = ig;
    }

    public void newThread(){
        finalizaGrafo();
        pthreadArray.add(new ProcessThread(processos, constArray, rangeArray, primeiro_atual));
        //if(errors.count > 0) System.exit(1);
        init();
    }

    public void startInterface(){
        if(pthreadArray.size() == 1) {
            generator.setPthreadArray(pthreadArray);
            addCheckBox(pthreadArray.get(0));
        }
        trace.setGrafoArray(grafoArray);
        trace.setGenerator(generator);
        trace.start_trace();
    }

    public void p(String str){
    	System.out.println(str);
    }

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

    public void printGrafos(){
        System.out.println("--------------------------------------------------------------------");
        for(int i=0;i<grafoArray.size();i++){
            System.out.println(grafoArray.get(i)+"\n");
            System.out.println("--------------------------------------------------------------------");
        }
    }

    public Processo novoProcesso(String nome, String indice, int valor, Range range){
        Processo p = new Processo(nome, indice, valor, range);
        if(!processos.contains(p)) 
            processos.add(p);
        return p;
    }

    public Acao novaAcao(String nome, String indice, int valor_indice, int estado, Processo pa){
        if(pa == null) return null;
        Acao a = new Acao(nome, pa, indice, valor_indice, estado);
        int i = pa.getAcoes().lastIndexOf(a), num = 0;
        if(i != -1)
            a.setId(pa.getAcoes().get(i).getId() + 1);
        pa.getAcoes().add(a);
        return a;
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

    public String tiraIndice(String expr, String indice, String valor){
        if(expr.equals("") || indice.equals("") || valor.equals("")) return expr;
        return expr.contains(indice) ? expr.replace(indice, valor) : expr;
    }

    public boolean exprBool(String expr){
        if(!expr.equals("")){
            try{
                return (boolean)eng.eval(expr);
            }catch(Exception e){
                ig.output_area.setText(ig.output_area.getText()+"-- line "+t.line+" col "+t.col+": invalid expression "+expr+"\n");
            }
        }
        return true;
    }

    public int calcExpr(String expr){
        if(!expr.equals("")){
            try{
                return (int)eng.eval(expr);
            }catch(Exception e){
                ig.output_area.setText(ig.output_area.getText()+"-- line "+t.line+" col "+t.col+": invalid expression "+expr+"\n");
            }
        }
        return -1;
    }

    public void zeraAcoesAtuais(){
    	for(int i=0;i<processos.size();i++){
    		processos.get(i).setAcoesAtuais(new ArrayList<Acao>());
    	}
    }

    public void buscaNome(ArrayList<ProcessThread> pthreadArray, ArrayList<HiperGrafo> grafoArray){
        for(int i=0;i<pthreadArray.size();i++){
            ArrayList<Processo> p = pthreadArray.get(i).getProcessos();
            for(int j=0;j<p.size();j++){
                ArrayList<Acao> a = p.get(j).getAcoes();
                for(int k=0;k<a.size();k++){
                    if(buscaNome(grafoArray, a.get(k).getNome(), a.get(k).getValorIndice(), i)) a.get(k).setCompartilhada(true);
                }
            }
        }
    }

    public boolean buscaNome(ArrayList<HiperGrafo> grafoArray, String nome, int valor_indice, int indiceGrafo){
        for(int i=0;i<grafoArray.size();i++){
            Vertice v = grafoArray.get(i).busca(nome, valor_indice);
            if(v != null && i != indiceGrafo){
                v.setCompartilhada(true);
                return true;
            }
        }
        return false;
    }

    public Acao acao_simples(String nome, Processo pa, String indice, int valor_indice){
    	Acao a = null;
    	if(exprBool(tiraIndice(bool, pa.getIndice(), pa.getEstado()+""))){
	    	a = novaAcao(nome, indice, valor_indice, pa.getEstado(), pa);
	        vertice_atual = grafo.insereVertice(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice(), acao_inicio);
	        ArrayList<Acao> acoes_atuais = pa.getAcoesAtuais();
	        if(acao_inicio){
	        	a.setInicio(true);
	        }else{
		        for(int i=0;i<acoes_atuais.size();i++){
		            grafo.insereAdj(grafo.busca(acoes_atuais.get(i).getNome(), acoes_atuais.get(i).getId(), acoes_atuais.get(i).getEstado(), acoes_atuais.get(i).getValorIndice()), vertice_atual, new Aresta());
		        }
		    }
	    }
	    return a;
    }

    public void acao_range(String nome, Processo pa){
    	ArrayList<Acao> acoes_aux = new ArrayList<Acao>();
    	for(int i=inf;i<=sup;i++){
    		Acao a = acao_simples(nome, pa, index, i);
    		acoes_aux.add(a);
    	}
    	pa.setAcoesAtuais(acoes_aux);
    }

    public void acao_range_index(String nome, Processo pa){
    	ArrayList<Acao> acoes_aux = new ArrayList<Acao>(), acoes_atuais = pa.getAcoesAtuais();
    	Acao a;
    	for(int i=inf, j=0;i<=sup;i++,j++){
    		a = acoes_atuais.get(j);
    		pa.setAcoesAtuais(new ArrayList<Acao>());
    		pa.getAcoesAtuais().add(a);
    		a = acao_simples(nome, pa, index, calcExpr(tiraIndice(expressao, index, i+"")));
    		acoes_aux.add(a);
    	}
    	pa.setAcoesAtuais(acoes_aux);
    }

    public void acao_index(String nome, int pa){
    	Range r = processos.get(pa).getRange();
    	Acao a;
    	for(int i=r.getInf();i<=r.getSup();i++){
    		processo_atual = processos.get(pa);
    		if(inf != -1 && sup != -1){
    			if(processo_atual.getAcoesAtuais().size() > 1)
    				acao_range_index(nome, processo_atual);
    			else
    				acao_range(nome, processos.get(pa));
    		}else{
    			a = acao_simples(nome, processos.get(pa), processos.get(pa).getIndice(), calcExpr(tiraIndice(expressao, processos.get(pa).getIndice(), processos.get(pa).getEstado()+"")));
    			processos.get(pa).setAcoesAtuais(new ArrayList<Acao>());
    			processos.get(pa).getAcoesAtuais().add(a);
    		}
    		pa++;
    	}
    }

    public void processo_local(String nome, Processo pa, int valor_indice){
    	if(exprBool(tiraIndice(bool, pa.getIndice(), pa.getEstado()+""))){
            ProcessoLocal pl = new ProcessoLocal(nome, pa.getEstado(), valor_indice);
            int j = locais.lastIndexOf(pl);
            if(j == -1)
                locais.add(pl);
            vertice_atual = grafo.insereVertice(nome, 0, pa.getEstado(), valor_indice, acao_inicio);
            ArrayList<Acao> acoes_atuais = pa.getAcoesAtuais();
        	for(int i=0;i<acoes_atuais.size();i++){
                grafo.insereAdjSimetrica(grafo.busca(acoes_atuais.get(i).getNome(), acoes_atuais.get(i).getId(), acoes_atuais.get(i).getEstado(), acoes_atuais.get(i).getValorIndice()), vertice_atual);
            }
            pa.setAcoesAtuais(new ArrayList<Acao>());
    	}
    }

    public void processo_local_index(String nome, int pa){
    	Range r = processos.get(pa).getRange();
    	int valor_expr = -1;
    	for(int i=r.getInf();i<=r.getSup();i++){
    		valor_expr = calcExpr(tiraIndice(expressao, processos.get(pa).getIndice(), processos.get(pa).getEstado()+""));
            if(valor_expr >= r.getInf() && valor_expr <= r.getSup()){
            	processo_local(nome, processos.get(pa), valor_expr);
            }else{
            	processo_local("ERROR", processos.get(pa), -1);
            }
            pa++;
    	}
    }

    public void rename(String newName, String oldName){
        for(int i=0;i<grafoArray.size();i++){
            ArrayList<Vertice> vertices = grafoArray.get(i).getVertices();
            for(int j=0;j<vertices.size();j++){
                Vertice v = vertices.get(j);
                if(v.getNome().contains(oldName) ){
                    v.setNome(v.getNome().replace(oldName, newName));
                }
            }
        }
        for(int i=0;i<pthreadArray.size();i++){
            ArrayList<Processo> p = pthreadArray.get(i).getProcessos();
            for(int j=0;j<p.size();j++){
                ArrayList<Acao> a = p.get(j).getAcoes();
                for(int k=0;k<a.size();k++){
                    if(a.get(k).getNome().contains(oldName)){
                        a.get(k).setNome(a.get(k).getNome().replace(oldName, newName));
                    }
                }
            }
        }
    }

    public void addCheckBox(ProcessThread process){
        ArrayList<Processo> p = process.getProcessos();
        for(int i=0;i<p.size();i++){
            ArrayList<Acao> acoes = p.get(i).getAcoes();
            for(int j=0;j<acoes.size();j++){
				Acao a = acoes.get(j);
                trace.addCheckBox(a.getNome()+(a.getValorIndice() == -1 ? "" : "["+a.getValorIndice()+"]"));
            }
        }
    }



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		String s = errors.SynErr(la.line, la.col, n);
		if (errDist >= minErrDist) 
			ig.output_area.setText(ig.output_area.getText()+""+s+(s.equals("") ? "":"\n"));
		errDist = 0;
	}

	public void SemErr (String msg) {
		String s = errors.SemErr(t.line, t.col, msg);
		if (errDist >= minErrDist) 
			ig.output_area.setText(ig.output_area.getText()+""+s+(s.equals("") ? "":"\n"));
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
		init();
		
		start();
		while (StartOf(1)) {
			start();
		}
	}

	void start() {
		if (la.kind == 2) {
			primitive_process();
		} else if (la.kind == 19) {
			constant_declaration();
		} else if (la.kind == 21) {
			range_declaration();
		} else if (la.kind == 16) {
			composite_process();
		} else SynErr(43);
	}

	void primitive_process() {
		String nome = la.val;
		
		Expect(2);
		if (la.kind == 8) {
			Get();
			parameter_list();
			Expect(9);
		}
		if (la.kind == 23) {
			index_label();
		}
		int indexPl = locais.indexOf(new ProcessoLocal(nome, -1, 0));
		if(indexPl != -1 && inf != -1 && sup != -1){
		   Processo p = null;
		   for(int i=inf;i<=sup;i++){
		       p = novoProcesso(nome, index, i, new Range(inf, sup));
		       if(i == inf) processo_atual = p;
		       if(i == locais.get(indexPl).getValorIndice() && primeiro_atual == null) primeiro_atual = p;
		   }
		   valor_expr = inf = sup = -1;
		}else{
		   processo_atual = novoProcesso(nome, "", -1, null);
		}
		acao_inicio = true;
		expressao = "";
		
		Expect(20);
		if (la.kind == 2 || la.kind == 35 || la.kind == 36) {
			local_process();
		} else if (la.kind == 8) {
			Get();
			primitive_process_body();
			Expect(9);
		} else SynErr(44);
		if(la.val.equals(".")){
		   newThread();
		}
		
		if (la.kind == 26) {
			Get();
		} else if (la.kind == 28) {
			Get();
		} else SynErr(45);
	}

	void constant_declaration() {
		Expect(19);
		String nome = la.val;
		
		Expect(2);
		Expect(20);
		la();
		int n = 0;
		try{
			n = Integer.parseInt(la.val);
		}catch(Exception e){
		}
		Const c = new Const(nome, n);
		if(!constArray.contains(c))
		   constArray.add(c);
		
		expr();
	}

	void range_declaration() {
		Expect(21);
		String nome = la.val;
		
		Expect(2);
		Expect(20);
		int infLocal = -1;
		if( isNum(la.val.charAt(0)) ){
		   infLocal = Integer.parseInt(la.val);
		}else{
		   int index = constArray.indexOf(new Const(la.val, 0));
		   if(index != -1)
		       infLocal = constArray.get(index).getValor();
		}
		
		expr();
		Expect(22);
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
		Expect(16);
		generator.setCPN(la.val);
		pi = new ArrayList<ProcessInstance>();
		
		Expect(2);
		if (la.kind == 8) {
			Get();
			parameter_list();
			Expect(9);
		}
		Expect(20);
		composite_body();
		if (la.kind == 30 || la.kind == 31) {
			label_visibility();
		}
		if (la.kind == 7) {
			relabels();
		}
		Expect(26);
		ArrayList<HiperGrafo> grafoArray2 = new ArrayList<HiperGrafo>();
		for(int i=0;i<pthreadArray.size();i++){
		   for(int j=0;j<pi.size();j++){
		       if(pthreadArray.get(i).getPrimeiro().getNome().equals(pi.get(j).process)){
		           ProcessThread p = (ProcessThread) pthreadArray.get(i).clone();
		           HiperGrafo hg = (HiperGrafo) grafoArray.get(i).clone();
		           String s = (pi.get(j).share.equals("") ? "" : pi.get(j).share+"." ) + ( pi.get(j).prefix.equals("") ? "" : pi.get(j).prefix + "." );
		           p.prefix = s;
		           p.renameAll(s, !pi.get(j).share.equals(""));
		           hg.renameAll(s, !pi.get(j).share.equals(""));
		           pthreadArrayInstance.add(p);
		           grafoArray2.add(hg);
		           buscaNome(pthreadArrayInstance, grafoArray2);
		           addCheckBox(p);
		       }
		   }
		}
		grafoArray = grafoArray2;
	}

	void expr() {
		term();
		while (la.kind == 4 || la.kind == 5) {
			expressao += la.val;
			if (la.kind == 4) {
				Get();
			} else {
				Get();
			}
			term();
		}
	}

	void term() {
		factor();
		while (la.kind == 6 || la.kind == 7) {
			expressao += la.val;
			if (la.kind == 6) {
				Get();
			} else {
				Get();
			}
			factor();
		}
	}

	void factor() {
		if(la.val.charAt(0) >= 'A' && la.val.charAt(0) <= 'Z' )
			try{
		   		expressao += ""+constArray.get(constArray.indexOf(new Const(la.val, 0))).getValor();
			}catch(Exception e){}
		else
		   expressao += la.val;
		
		if (la.kind == 8) {
			Get();
			expr();
			if(la.val.charAt(0) == '(' || la.val.charAt(0) == ')' )
			      expressao += la.val;
			
			Expect(9);
		} else if (la.kind == 2) {
			Get();
		} else if (la.kind == 3) {
			Get();
		} else if (la.kind == 1) {
			Get();
		} else SynErr(46);
	}

	void boolean_expr() {
		expressao = "";
		
		expr();
		bool += expressao;
		
		while (StartOf(2)) {
			bool += la.val;
			
			switch (la.kind) {
			case 10: {
				Get();
				break;
			}
			case 11: {
				Get();
				break;
			}
			case 12: {
				Get();
				break;
			}
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
			}
			expressao = "";
			
			expr();
			bool += expressao;
			
		}
	}

	void index() {
		Expect(23);
		index = la.val;
		expressao = "";
		
		expr();
		while (la.kind == 24) {
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
			
			while (la.kind == 22) {
				Get();
				expr();
				sup = calcExpr(expressao);
				
			}
		}
		Expect(25);
	}

	void index_label() {
		index();
		while (la.kind == 23) {
			index();
		}
	}

	void simple_action() {
		String nome = la.val;
		
		Expect(3);
		if (la.kind == 23) {
			index_label();
		}
		Acao a = null;
		if(primeiro_atual == null)
		primeiro_atual = processo_atual;
		Processo p = processo_atual;
		  if(processo_atual != null && processo_atual.getEstado() == -1 ){
		  	if(inf != -1 && sup != -1){
		  		if(processo_atual.getAcoesAtuais().size() > 1)
		  			acao_range_index(nome, processo_atual);
		  		else
		  			acao_range(nome, processo_atual);
		  	}else{
		  		a = acao_simples(nome, processo_atual, processo_atual.getIndice(), calcExpr(tiraIndice(expressao, processo_atual.getIndice(), processo_atual.getEstado()+"")));
		   	if(!la.val.equals(",")){
		   		processo_atual.setAcoesAtuais(new ArrayList<Acao>());
		        processo_atual.getAcoesAtuais().add(a);
		    }
		  	}
		  }else if(processo_atual != null){
		  	acao_index(nome, processos.indexOf(processo_atual));
		  }
		  expressao = "";
		  if(la.val.equals("->")) acao_inicio = false;
		  acao_atual = a;
		  processo_atual = p;
		
	}

	void action() {
		simple_action();
		while (la.kind == 26) {
			Get();
			simple_action();
		}
	}

	void action_set() {
		ArrayList<Acao> acoes_atuais = new ArrayList<Acao>();
		
		Expect(27);
		action();
		acoes_atuais.add(acao_atual);
		
		while (la.kind == 28) {
			Get();
			action();
			acoes_atuais.add(acao_atual);
			
		}
		Expect(29);
		processo_atual.setAcoesAtuais(acoes_atuais);
		
	}

	void alphabet_extension() {
		Expect(4);
		action_set();
	}

	void label_visibility() {
		if (la.kind == 30) {
			hide_label();
		} else if (la.kind == 31) {
			expose_label();
		} else SynErr(47);
	}

	void hide_label() {
		Expect(30);
		action_set();
	}

	void expose_label() {
		Expect(31);
		action_set();
	}

	void relabels() {
		Expect(7);
		relabel_set();
	}

	void relabel_set() {
		Expect(27);
		relabel();
		while (la.kind == 28) {
			Get();
			relabel();
		}
		Expect(29);
	}

	void relabel() {
		if (la.kind == 3) {
			simple_relabel();
		} else if (la.kind == 32) {
			Get();
			index();
			relabel_set();
		} else SynErr(48);
	}

	void simple_relabel() {
		String newName = la.val;
		
		action();
		Expect(7);
		String oldName = la.val;
		
		action();
		rename(newName, oldName);
		
	}

	void parameter_list() {
		parameter();
		while (la.kind == 28) {
			Get();
			parameter();
		}
	}

	void local_process() {
		String nome = la.val;
		
		if (la.kind == 2) {
			Get();
			if (la.kind == 23) {
				index();
			}
			if(processo_atual.getEstado() == -1){
			processo_local(nome, processo_atual, calcExpr(tiraIndice(expressao, processo_atual.getIndice(), processo_atual.getEstado()+"")) );
			}else{
			processo_local_index(nome, processos.indexOf(processo_atual));
			}
			expressao = bool = "";
			
		} else if (la.kind == 35 || la.kind == 36) {
			Acao a = null;
			Processo p = processo_atual;
			if(primeiro_atual == null)
			primeiro_atual = processo_atual;
			if( processo_atual.getEstado() == -1 ){
			a = acao_simples(la.val, processo_atual, processo_atual.getIndice(), calcExpr(tiraIndice(expressao, processo_atual.getIndice(), processo_atual.getEstado()+"")));
			processo_atual.setAcoesAtuais(new ArrayList<Acao>());
			   processo_atual.getAcoesAtuais().add(a);
			}else{
			acao_index(la.val, processos.indexOf(processo_atual));
			}
			expressao = bool = "";
			processo_atual = p;
			
			if (la.kind == 35) {
				Get();
			} else {
				Get();
			}
		} else SynErr(49);
	}

	void primitive_process_body() {
		process_body();
		while (la.kind == 33 || la.kind == 34) {
			if(la.val.equals("|")){
			   acao_inicio = true;
			   zeraAcoesAtuais();
			}
			
			if (la.kind == 33) {
				Get();
			} else {
				Get();
			}
			process_body();
		}
		if (la.kind == 4) {
			alphabet_extension();
		}
		if (la.kind == 30 || la.kind == 31) {
			label_visibility();
		}
		if (la.kind == 7) {
			relabels();
		}
	}

	void parameter() {
		String nome = la.val;
		
		Expect(2);
		Expect(20);
		int n = 0;
		try{
			n = Integer.parseInt(la.val);
		}catch(Exception e){
		}
		Const c = new Const(nome, n);
		if(!constArray.contains(c))
		   constArray.add(c);
		
		Expect(1);
	}

	void process_body() {
		if (la.kind == 3 || la.kind == 27 || la.kind == 37) {
			choice();
		} else if (la.kind == 2 || la.kind == 35 || la.kind == 36) {
			local_process();
		} else if (la.kind == 38) {
			condition();
		} else SynErr(50);
	}

	void choice() {
		if (la.kind == 37) {
			Get();
			boolean_expr();
			Expect(9);
		}
		expressao = "";
		
		if (la.kind == 27) {
			action_set();
		} else if (la.kind == 3) {
			action();
		} else SynErr(51);
		Expect(33);
		process_body();
	}

	void condition() {
		Expect(38);
		boolean_expr();
		Expect(39);
		process_body();
		Expect(40);
		process_body();
	}

	void composite_body() {
		if (la.kind == 2 || la.kind == 3 || la.kind == 27) {
			process_instance();
		} else if (la.kind == 8) {
			parallel_list();
		} else if (la.kind == 38) {
			composite_conditional();
		} else if (la.kind == 32) {
			composite_replicator();
		} else SynErr(52);
	}

	void process_instance() {
		sharing_set = new ArrayList<String>();
		prefix = "";
		
		if (la.kind == 27) {
			Get();
			sharing_set.add(la.val);
			Expect(3);
			while (la.kind == 28) {
				Get();
				sharing_set.add(la.val);
				Expect(3);
			}
			Expect(41);
		}
		if (la.kind == 3) {
			prefix = la.val;
			Get();
			Expect(24);
		}
		try{
		   boolean flag = true;
		   for(int i=0;i<pthreadArray.size() && flag;i++){
		       if(la.val.equals(pthreadArray.get(i).getPrimeiro().getNome()))
		           flag = false;
		   }
		   if(flag)
		       new Exception();
		   if(sharing_set.size() > 0){
		       for(int i=0;i<sharing_set.size();i++){
		           pi.add(new ProcessInstance(sharing_set.get(i), prefix, la.val));
		       }
		   }else{
		       pi.add(new ProcessInstance("", prefix, la.val));
		   }
		}catch(Exception e){
		   ig.output_area.setText(ig.output_area.getText()+"-- line "+t.line+" col "+t.col+": The action "+la.val+" can not be instantiated\n");
		   System.exit(1);
		}
		
		Expect(2);
		if (la.kind == 8) {
			Get();
			actual_parameter_list();
			Expect(9);
		}
	}

	void parallel_list() {
		Expect(8);
		composite_body();
		while (la.kind == 16) {
			Get();
			composite_body();
		}
		Expect(9);
	}

	void composite_conditional() {
		Expect(38);
		boolean_expr();
		Expect(39);
		composite_body();
		Expect(40);
		composite_body();
	}

	void composite_replicator() {
		Expect(32);
		index();
		composite_body();
	}

	void actual_parameter_list() {
		expr();
		while (la.kind == 28) {
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
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x},
		{_x,_x,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _T,_x,_x,_T, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_T,_T, _T,_T,_T,_T, _T,_T,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	public String errorMsg = "";

	protected String printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		return b.toString();
	}
	
	public String SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "integer expected"; break;
			case 2: s = "uppercase_id expected"; break;
			case 3: s = "lowercase_id expected"; break;
			case 4: s = "\"+\" expected"; break;
			case 5: s = "\"-\" expected"; break;
			case 6: s = "\"*\" expected"; break;
			case 7: s = "\"/\" expected"; break;
			case 8: s = "\"(\" expected"; break;
			case 9: s = "\")\" expected"; break;
			case 10: s = "\">\" expected"; break;
			case 11: s = "\"<\" expected"; break;
			case 12: s = "\">=\" expected"; break;
			case 13: s = "\"<=\" expected"; break;
			case 14: s = "\"==\" expected"; break;
			case 15: s = "\"&&\" expected"; break;
			case 16: s = "\"||\" expected"; break;
			case 17: s = "\"!\" expected"; break;
			case 18: s = "\"!=\" expected"; break;
			case 19: s = "\"const\" expected"; break;
			case 20: s = "\"=\" expected"; break;
			case 21: s = "\"range\" expected"; break;
			case 22: s = "\"..\" expected"; break;
			case 23: s = "\"[\" expected"; break;
			case 24: s = "\":\" expected"; break;
			case 25: s = "\"]\" expected"; break;
			case 26: s = "\".\" expected"; break;
			case 27: s = "\"{\" expected"; break;
			case 28: s = "\",\" expected"; break;
			case 29: s = "\"}\" expected"; break;
			case 30: s = "\"\\\\\" expected"; break;
			case 31: s = "\"@\" expected"; break;
			case 32: s = "\"forall\" expected"; break;
			case 33: s = "\"->\" expected"; break;
			case 34: s = "\"|\" expected"; break;
			case 35: s = "\"STOP\" expected"; break;
			case 36: s = "\"ERROR\" expected"; break;
			case 37: s = "\"when(\" expected"; break;
			case 38: s = "\"if\" expected"; break;
			case 39: s = "\"then\" expected"; break;
			case 40: s = "\"else\" expected"; break;
			case 41: s = "\"}::\" expected"; break;
			case 42: s = "??? expected"; break;
			case 43: s = "invalid start"; break;
			case 44: s = "invalid primitive_process"; break;
			case 45: s = "invalid primitive_process"; break;
			case 46: s = "invalid factor"; break;
			case 47: s = "invalid label_visibility"; break;
			case 48: s = "invalid relabel"; break;
			case 49: s = "invalid local_process"; break;
			case 50: s = "invalid process_body"; break;
			case 51: s = "invalid choice"; break;
			case 52: s = "invalid composite_body"; break;
			default: s = "error " + n; break;
		}
		count++;
		return printMsg(line, col, s);
	}

	public String SemErr (int line, int col, String s) {	
		count++;
		return printMsg(line, col, s);
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public String Warning (int line, int col, String s) {	
		return printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
