import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Geracao{

	private ArrayList<Processo> processos;
	private ArrayList<Const> constArray;
	private ArrayList<Range> rangeArray;
	private ArrayList<Acao> traceArray;
	private ArrayList<File> arquivos;
	private boolean stop, error;
	private BufferedWriter buff;
	private File file;
	private String nomeArq;

	Geracao(ArrayList<Processo> processos, ArrayList<Const> constArray, ArrayList<Range> rangeArray, ArrayList<Acao> traceArray){
		this.processos = processos;
		this.constArray = constArray;
		this.rangeArray = rangeArray;
		this.traceArray = traceArray;
		this.arquivos = new ArrayList<File>();
		this.nomeArq = "";
	}

	public void gerate(){
		try{        
	        int i;
	        Processo p, main = processos.get(0);
	        file = new File("Constantes"+main.getNome()+".java");
	        arquivos.add(file);
	        buff = new BufferedWriter(new FileWriter(file));
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

	        file = new File("Ranges"+main.getNome()+".java");
        	arquivos.add(file);
	        buff = new BufferedWriter(new FileWriter(file));
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
	            file = new File(nomeP+".java");
	            arquivos.add(file);
	            buff = new BufferedWriter(new FileWriter(file));
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
	            	if(!acoes.get(j).getNome().equals("STOP") && !acoes.get(j).getNome().equals("ERROR")){
		                String nomeA = acoes.get(j).getNome()+(acoes.get(j).getValorIndice() == -1 ? "": "_"+acoes.get(j).getValorIndice());
		                nomeA += acoes.get(j).getId() == -1 ? "": "_"+acoes.get(j).getId();
		                buff.append(
		                    "    public void "+nomeA+"(){\n"+
		                    "        System.out.println(\""+acoes.get(j).getNome()+
		                            (acoes.get(j).getValorIndice() != -1 ? "["+acoes.get(j).getValorIndice()+"]":"")+"\");\n"+
		                    "    }\n\n"
		                );
		            }
	            }
	            buff.append("}");
	            buff.close();
	        }
	        /*cria um arquivo .java com o nome Main+main.nome, que eh o nome do processo principal.*/
	        nomeArq = "Main"+main.getNome()+".java";
	        file = new File("Main"+main.getNome()+".java");
	        arquivos.add(file);
	        buff = new BufferedWriter(new FileWriter(file));
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
	            "                Thread.sleep(1000);\n"
	        );
	        for(i=0;i<traceArray.size();i++){
	        	if(!traceArray.get(i).getNome().equals("STOP") && !traceArray.get(i).getNome().equals("ERROR")){
		            String nomeP = traceArray.get(i).getProcesso().getNome().toLowerCase();
		            nomeP += (traceArray.get(i).getProcesso().getEstado() == -1 ? "" : "_"+traceArray.get(i).getProcesso().getEstado());
		            String nomeA = traceArray.get(i).getNome()+(traceArray.get(i).getValorIndice() == -1 ? "" : "_"+traceArray.get(i).getValorIndice());
		            nomeA += traceArray.get(i).getId() == -1 ? "": "_"+traceArray.get(i).getId();
		            buff.append(
		                "                obj_"+nomeP+"."+nomeA+"();\n"+
		                "                Thread.sleep(1000);\n"
		            );
		        }else if(traceArray.get(i).getNome().equals("STOP")){
	            	stop = true;
	            }else if(traceArray.get(i).getNome().equals("ERROR")){
	            	for(int j=0;j<arquivos.size();j++){
			            arquivos.get(j).delete();
			        }
	            	throw new Error("Chamada do processo ERROR");
	            }
	        }
	        if(stop){
	        	String nomeP = main.getNome()+(main.getEstado() == -1 ? "": "_"+main.getEstado());
	            buff.append(
	                "                System.out.println(\"STOP\");\n"+
	                "                thread"+nomeP+".interrupt();\n"
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

	public String getNomeArq(){
		return this.nomeArq;
	}

}