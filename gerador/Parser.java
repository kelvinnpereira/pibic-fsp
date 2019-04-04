

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;



public class Parser {
	public static final int _EOF = 0;
	public static final int _numero_inteiro = 1;
	public static final int _id_maiusculo = 2;
	public static final int _id_minusculo = 3;
	public static final int maxT = 42;

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
    public ArrayList<String> traceArray;
    public ArrayList<Range> rangeArray;
    public ArrayList<Const> constArray;
    public Processo last;
    public int index, sup, inf;

    public void la(){
        System.out.println("la.val: "+la.val);
    }

    public void t(){
        System.out.println("t.val: "+t.val);
    }

    public void print(){
        for(int i=0;i<processos.size();i++){
            System.out.println(processos.get(i));
            processos.get(i).printAcoes();
        }
        System.out.println();
    }

    public Processo novoProcesso(String nome, int i){
        Processo p = new Processo(nome, i);
        if(!processos.contains(p)) 
            processos.add(p);
        return p;
    }

    public Acao novaAcao(String nome){
        if(last == null) return null;
        Acao a = new Acao(nome, last);
        if(!last.getAcoes().contains(a))
            last.getAcoes().add(a);
        return a;
    }

    public Acao achaAcao(String nome){
        for(int i=0;i<processos.size();i++){
            for(int j=0;j<processos.get(i).getAcoes().size();j++){
                if(processos.get(i).getAcoes().get(j).getNome().equals(nome)) 
                    return processos.get(i).getAcoes().get(j);
            }
        }
        return null;
    }

    public void printTrace(){
        for(int i=0;i<traceArray.size();i++){
            System.out.println(traceArray.get(i));
        }
    }

    public void novoTrace() throws Error{
        Acao a = achaAcao(la.val);
        if(a == null) 
            throw new Error("Trace invalido!!");
        traceArray.add(new String("obj_"+a.getProcesso().getNome().toLowerCase()+"."+a.getNome()+"();"));
    }

    public boolean isNum(char c){
        return c >= '0' && c <= '9';
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
		traceArray = new ArrayList<String>();
		rangeArray = new ArrayList<Range>();
		constArray = new ArrayList<Const>();
		index = inf = sup = -1;
		
		init();
		while (la.kind == 2 || la.kind == 19 || la.kind == 21) {
			init();
		}
		trace();
		print();
		try{
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
		       buff = new BufferedWriter(new FileWriter(p.getNome()+".java"));
		       buff.append(
		           /*o nome da classe e o proprio do processo.*/
		           "public class "+p.getNome()+"{\n\n"+
		           /*contrutor da classe.*/
		           "    "+p.getNome()+"(){\n"+
		           "    }\n\n"
		       );
		       /*cada acao sera tranformada em um metodo da classe.*/
		       for(int j=0;j<p.getAcoes().size();j++){
		           buff.append(
		               "    public void "+p.getAcoes().get(j).getNome()+"(){\n"+
		               "        System.out.println(\""+p.getAcoes().get(j).getNome()+"\");\n"+
		               (p.getAcoes().get(j).getNome().equals("STOP") ? "        System.exit(1);\n" : "")+
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
		       buff.append("    "+processos.get(i).getNome()+" obj_"+processos.get(i).getNome().toLowerCase()+";\n\n");
		   }
		   /*construtor da classe principal.*/
		   buff.append("    Main"+main.getNome()+"(){\n");
		   /*instancia cada atributo da classe principal.*/
		   for(i=0;i<processos.size();i++){
		       buff.append(
		           "        obj_"+processos.get(i).getNome().toLowerCase()+" = new "+processos.get(i).getNome()+"();\n"
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
		       "                Thread.sleep(1000);\n"
		   );
		   for(i=0;i<traceArray.size();i++){
		       System.out.println(traceArray.get(i));
		       buff.append(
		           "                "+traceArray.get(i)+"\n"+
		           "                Thread.sleep(1000);\n"
		       );
		   }
		   buff.append(
		       "            }\n"+
		       "        }catch(InterruptedException e){}\n"+
		       "    }\n\n"+
		       /*metodo main que instancia a classe principal*/
		       "    public static void main(String args[]){\n"+
		       "        Main"+main.getNome()+" main = new Main"+main.getNome()+"();\n"+
		       "    }\n\n"+
		       "}"
		   );
		   buff.close();
		}catch(Exception e){}
		
	}

	void init() {
		if (la.kind == 2) {
			processo_simples();
		} else if (la.kind == 19) {
			declara_constante();
		} else if (la.kind == 21) {
			declara_intervalo();
		} else SynErr(43);
	}

	void trace() {
		Expect(4);
		acao_trace();
		while (la.kind == 5) {
			Get();
			acao_trace();
		}
		Expect(6);
	}

	void acao_trace() {
		if (la.kind == 3) {
			try{
			   novoTrace();
			}catch(Error e){
			   System.out.println(e.toString());
			   System.exit(1);
			}
			
			Get();
		} else if (la.kind == 23) {
			index_rotulos();
		} else SynErr(44);
	}

	void processo_simples() {
		String nome = la.val;
		
		Expect(2);
		if (la.kind == 33) {
			Get();
			lista_parametros();
			Expect(34);
		}
		if (la.kind == 23) {
			index_rotulos();
		}
		System.out.println("index "+index+" inf "+inf+" sup "+sup);
		int i = processos.indexOf(new Processo(nome, index));
		if(i != -1 && processos.get(i).getIndex() != -1 && inf != -1 && sup != -1){
		   last = processos.get(i);
		   last.setRange(new Range(inf, sup));
		   System.out.println("entrou no i != -1");
		}else{
		   last = novoProcesso(nome, index);
		   System.out.println("entrou no else");
		}
		System.out.println("last "+last);
		last = processos.get(processos.indexOf(last));
		System.out.println("last "+last);
		System.out.println(last.getIndex());
		
		Expect(20);
		if (la.kind == 2 || la.kind == 36 || la.kind == 37) {
			processo_local();
		} else if (la.kind == 33) {
			Get();
			corpo_processo_simples();
			Expect(34);
		} else SynErr(45);
		if (la.kind == 26) {
			Get();
		} else if (la.kind == 28) {
			Get();
		} else SynErr(46);
	}

	void declara_constante() {
		Expect(19);
		String nome = la.val;
		
		Expect(2);
		Expect(20);
		Const c = new Const(nome, Integer.parseInt(la.val));
		if(!constArray.contains(c))
		   constArray.add(c);
		
		expr();
	}

	void declara_intervalo() {
		Expect(21);
		String nome = la.val;
		la();
		
		Expect(2);
		Expect(20);
		int infLocal = Integer.parseInt(la.val);
		
		expr();
		Expect(22);
		int supLocal = Integer.parseInt(la.val);
		
		expr();
		System.out.println("nome "+nome);
		Range r = new Range(nome, infLocal, supLocal);
		if(!rangeArray.contains(r))
		   rangeArray.add(r);
		r = rangeArray.get(rangeArray.size()-1);
		System.out.println(r.getNome()+" "+r.getInf()+" "+r.getSup());
		
	}

	void expr() {
		term();
		while (la.kind == 7 || la.kind == 8) {
			if (la.kind == 7) {
				Get();
			} else {
				Get();
			}
			term();
		}
	}

	void term() {
		fator();
		while (la.kind == 9 || la.kind == 10) {
			if (la.kind == 9) {
				Get();
			} else {
				Get();
			}
			fator();
		}
	}

	void fator() {
		if (la.kind == 2) {
			Get();
		} else if (la.kind == 3) {
			Get();
		} else if (la.kind == 1) {
			Get();
		} else SynErr(47);
	}

	void expr_booleana() {
		expr();
		while (StartOf(1)) {
			switch (la.kind) {
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
			expr();
		}
	}

	void index() {
		Expect(23);
		la();
		if( isNum(la.val.charAt(0)) ){
		   index = Integer.parseInt(la.val);
		}else{
		   int i = constArray.indexOf(new Const(la.val, 0));
		   if(i != -1)
		       index = constArray.get(i).getValor();
		}
		
		expr();
		while (la.kind == 24) {
			Get();
			la();
			if(isNum(la.val.charAt(0)))
			   inf = Integer.parseInt(la.val);
			else
			   inf = -1;
			
			expr();
			while (la.kind == 22) {
				Get();
				la();
				if(isNum(la.val.charAt(0)) && inf != -1){
				   sup = Integer.parseInt(la.val);
				   rangeArray.add(new Range(inf, sup)); 
				}
				
				expr();
			}
		}
		Expect(25);
	}

	void index_rotulos() {
		index();
		while (la.kind == 23) {
			index();
		}
	}

	void acao_simples() {
		Acao a = novaAcao(la.val);
		
		Expect(3);
		if (la.kind == 23) {
			index_rotulos();
		}
		a.setIndex(index);
		System.out.println(a.getIndex());
		
	}

	void acao() {
		acao_simples();
		while (la.kind == 26) {
			Get();
			acao_simples();
		}
	}

	void conjunto_acoes() {
		Expect(27);
		acao();
		while (la.kind == 28) {
			Get();
			acao();
		}
		Expect(29);
	}

	void extensao_alfabeto() {
		Expect(7);
		conjunto_acoes();
	}

	void visibilidade_rotulo() {
		if (la.kind == 30) {
			rotulo_escondido();
		} else if (la.kind == 31) {
			rotulo_exposto();
		} else SynErr(48);
	}

	void rotulo_escondido() {
		Expect(30);
		conjunto_acoes();
	}

	void rotulo_exposto() {
		Expect(31);
		conjunto_acoes();
	}

	void renomeacoes() {
		Expect(10);
		conjunto_renomeacao();
	}

	void conjunto_renomeacao() {
		Expect(27);
		renomeacao();
		while (la.kind == 28) {
			Get();
			renomeacao();
		}
		Expect(29);
	}

	void renomeacao() {
		if (la.kind == 3) {
			renomeacao_simples();
		} else if (la.kind == 32) {
			Get();
			index();
			conjunto_renomeacao();
		} else SynErr(49);
	}

	void renomeacao_simples() {
		acao();
		Expect(10);
		acao();
	}

	void lista_parametros() {
		parametro();
		while (la.kind == 28) {
			Get();
			parametro();
		}
	}

	void processo_local() {
		if (la.kind == 2) {
			String nome = la.val;
			
			Get();
			if (la.kind == 23) {
				index();
			}
			last = novoProcesso(nome, index);
			last = processos.get(processos.indexOf(last));
			try{
			   if(index != -1)
			       last.setIndex(index);
			}catch(Error e){
			   System.out.println(e);
			   System.exit(1);
			}
			System.out.println("processo_local "+last.getIndex());
			
		} else if (la.kind == 36) {
			novaAcao(la.val);
			
			Get();
		} else if (la.kind == 37) {
			novaAcao(la.val);
			
			Get();
		} else SynErr(50);
	}

	void corpo_processo_simples() {
		corpo_processo();
		while (la.kind == 5 || la.kind == 35) {
			if (la.kind == 5) {
				Get();
			} else {
				Get();
			}
			corpo_processo();
		}
		if (la.kind == 7) {
			extensao_alfabeto();
		}
		if (la.kind == 30 || la.kind == 31) {
			visibilidade_rotulo();
		}
		if (la.kind == 10) {
			renomeacoes();
		}
	}

	void parametro() {
		Expect(2);
		Expect(20);
		Expect(1);
	}

	void corpo_processo() {
		if (la.kind == 3 || la.kind == 27 || la.kind == 38) {
			escolha();
		} else if (la.kind == 2 || la.kind == 36 || la.kind == 37) {
			processo_local();
		} else if (la.kind == 39) {
			condicao();
		} else SynErr(51);
	}

	void escolha() {
		if (la.kind == 38) {
			Get();
			expr_booleana();
		}
		if (la.kind == 27) {
			conjunto_acoes();
		} else if (la.kind == 3) {
			acao();
		} else SynErr(52);
		Expect(5);
		corpo_processo();
	}

	void condicao() {
		Expect(39);
		expr_booleana();
		Expect(40);
		corpo_processo();
		Expect(41);
		corpo_processo();
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
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_T, _T,_T,_T,_T, _T,_T,_T,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x}

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
			case 1: s = "numero_inteiro expected"; break;
			case 2: s = "id_maiusculo expected"; break;
			case 3: s = "id_minusculo expected"; break;
			case 4: s = "\"TRACE\" expected"; break;
			case 5: s = "\"->\" expected"; break;
			case 6: s = "\"FIMTRACE\" expected"; break;
			case 7: s = "\"+\" expected"; break;
			case 8: s = "\"-\" expected"; break;
			case 9: s = "\"*\" expected"; break;
			case 10: s = "\"/\" expected"; break;
			case 11: s = "\">\" expected"; break;
			case 12: s = "\"<\" expected"; break;
			case 13: s = "\">=\" expected"; break;
			case 14: s = "\"<=\" expected"; break;
			case 15: s = "\"==\" expected"; break;
			case 16: s = "\"&&\" expected"; break;
			case 17: s = "\"||\" expected"; break;
			case 18: s = "\"!\" expected"; break;
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
			case 33: s = "\"(\" expected"; break;
			case 34: s = "\")\" expected"; break;
			case 35: s = "\"|\" expected"; break;
			case 36: s = "\"STOP\" expected"; break;
			case 37: s = "\"ERROR\" expected"; break;
			case 38: s = "\"when\" expected"; break;
			case 39: s = "\"if\" expected"; break;
			case 40: s = "\"then\" expected"; break;
			case 41: s = "\"else\" expected"; break;
			case 42: s = "??? expected"; break;
			case 43: s = "invalid init"; break;
			case 44: s = "invalid acao_trace"; break;
			case 45: s = "invalid processo_simples"; break;
			case 46: s = "invalid processo_simples"; break;
			case 47: s = "invalid fator"; break;
			case 48: s = "invalid visibilidade_rotulo"; break;
			case 49: s = "invalid renomeacao"; break;
			case 50: s = "invalid processo_local"; break;
			case 51: s = "invalid corpo_processo"; break;
			case 52: s = "invalid escolha"; break;
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
