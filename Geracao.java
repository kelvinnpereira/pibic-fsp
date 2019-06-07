import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Geracao{

	private ArrayList<Acao> traceArray, shared;
	private ArrayList<ProcessThread> pthreadArray;
	private BufferedWriter buff;
	private File file;
	private ArrayList<File> arquivos;
	private String nomeArq, composite_process_name;
	private boolean stop, error;

	Geracao(ArrayList<ProcessThread> pthreadArray){
		this.pthreadArray = pthreadArray;
		this.arquivos = new ArrayList<File>();
		this.shared = new ArrayList<Acao>();
		this.nomeArq = "";
		this.composite_process_name = "MainMain";
	}

	public ArrayList<ProcessThread> getPthreadArray(){
		return this.pthreadArray;
	}

	public void setCPN(String nome){
		this.composite_process_name = nome;
	}

	public String getCPN(){
		return this.composite_process_name;
	}

	public void isShared(Acao acao){
		for(int i=0;i<pthreadArray.size();i++){
			ArrayList<Processo> processos = pthreadArray.get(i).getProcessos();
			for(int j=0;j<processos.size();j++){
				ArrayList<Acao> acoes = processos.get(j).getAcoes();
				for(int k=0;k<acoes.size();k++){
					if(acao.getNome().equals(acoes.get(k).getNome())){
						acao.setCompartilhada(true);
						acoes.get(k).setCompartilhada(true);
					}
				}
			}
		}
	}

	public boolean isPrimeiro(Processo p){
		Processo processos;
		for(int i=0;i<pthreadArray.size();i++){
			if(pthreadArray.get(i).getPrimeiro() == p) return true;
		}
		return false;
	}

	public int conta(String nome){
		int cont = 0;
		for(int i=0;i<shared.size();i++){
			if(shared.get(i) != null && shared.get(i).getNome().equals(nome)){
				shared.set(i, null);
				cont++;
			}
		}
		return cont;
	}

	public void gerate(){
		try{
			File fileMain = new File(composite_process_name+".java");
			arquivos.add(file);
			BufferedWriter buffMain = new BufferedWriter(new FileWriter(fileMain));
			buffMain.append(
				/*metodo main que instancia a classe principal*/
				"public class "+composite_process_name+"{\n\n"+
				"    public static void main(String args[]){\n"
			);
			for(int cont=0;cont<pthreadArray.size();cont++){
		        int i;

		        Processo p, main = pthreadArray.get(cont).getProcessos().get(0);
		        /*file = new File("Constantes"+main.getNome()+".java");
		        arquivos.add(file);
		        buff = new BufferedWriter(new FileWriter(file));
		        buff.append(
		            "public class Constantes"+main.getNome()+"{\n\n"
		        );
		        ArrayList<Const> constArray = pthreadArray.get(cont).getConstArray();
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
		        ArrayList<Range> rangeArray = pthreadArray.get(cont).getRangeArray();
		        for(i=0;i<rangeArray.size();i++){
		            buff.append(
		                "    public static final Range "+rangeArray.get(i).getNome()+" = new Range("+rangeArray.get(i).getInf()+", "+rangeArray.get(i).getSup()+");\n"
		            );
		        }
		        buff.append("}");
		        buff.close();*/

		        ArrayList<Processo> processos = pthreadArray.get(cont).getProcessos();

				/*cria um arquivo .java com o nome do processo.*/
				String nomeP = main.getNome()+(main.getEstado() == -1 ? "": "_"+main.getEstado());
				file = new File(nomeP+".java");
				arquivos.add(file);
				buff = new BufferedWriter(new FileWriter(file));
				buff.append(
					/*o nome da classe e o proprio do processo.*/
					"public class "+nomeP+" implements Runnable{\n\n"+
					"    Thread thread"+main.getNome()+";\n\n"
				);

				int last_index_shared = shared.size();

				for(i=0;i<processos.size();i++){
					ArrayList<Acao> acoes = processos.get(i).getAcoes();
					for(int j=0;j<acoes.size();j++){
						if(acoes.get(j).getCompartilhada() && !acoes.get(j).getNome().equals("STOP") && !acoes.get(j).getNome().equals("ERROR")){
							buff.append(
								"    Semaforo "+acoes.get(j).getNome()+"_shared;\n\n"
							);
							shared.add(acoes.get(j));
							pthreadArray.get(cont).getShared().add(acoes.get(j));
						}
					}
				}

				buff.append(
					/*contrutor da classe.*/
					"    "+nomeP+"("
				);
				for(i=last_index_shared;i<shared.size();i++){
					if(i == shared.size() -1)
						buff.append("Semaforo "+shared.get(i).getNome()+"_shared");
					else
						buff.append("Semaforo "+shared.get(i).getNome()+"_shared, ");
				}
				buff.append("){\n");
				for(i=last_index_shared;i<shared.size();i++){
					buff.append(
						"        this."+shared.get(i).getNome()+"_shared = "+shared.get(i).getNome()+"_shared;\n"
					);
				}
				buff.append(
					/*instancia a thread*/
					"        thread"+main.getNome()+" = new Thread(this);\n"+
					/*inicia a execucao da thread*/
					"        thread"+main.getNome()+".start();\n"+
					"    }\n\n"
				);
				

		        for(i=0;i<processos.size();i++){
		            /*cada acao sera tranformada em um metodo da classe.*/
		            ArrayList<Acao> acoes = processos.get(i).getAcoes();
		            for(int j=0;j<acoes.size();j++){
		            	if(!acoes.get(j).getNome().equals("STOP") && !acoes.get(j).getNome().equals("ERROR")){
			                String nomeA = acoes.get(j).getNome()+(acoes.get(j).getValorIndice() == -1 ? "": "_"+acoes.get(j).getValorIndice());
			                nomeA += acoes.get(j).getId() == -1 ? "": "_"+acoes.get(j).getId();
							if(acoes.get(j).getCompartilhada()){
								buff.append(
									"    public synchronized void "+nomeA+"()throws InterruptedException{\n"+
									"        "+acoes.get(j).getNome()+"_shared.dec();\n"+
									"        System.out.println(\""+acoes.get(j).getNome()+
											(acoes.get(j).getValorIndice() != -1 ? "["+acoes.get(j).getValorIndice()+"]":"")+"\");\n"+
									"    }\n\n"
								);
							}else{
								buff.append(
									"    public void "+nomeA+"(){\n"+
									"        System.out.println(\""+acoes.get(j).getNome()+
											(acoes.get(j).getValorIndice() != -1 ? "["+acoes.get(j).getValorIndice()+"]":"")+"\");\n"+
									"    }\n\n"
								);
							}
			            }
		            }
		        }

		        buff.append(
		            /*execucao do programa com o metodo run*/
		            "    public void run(){\n"+
		            "        try{\n"+
		            "            while(true){\n"+
		            "                Thread.sleep(1000);\n"
		        );
				traceArray = pthreadArray.get(cont).getTraceArray();
		        for(i=0;i<traceArray.size();i++){
		        	if(!traceArray.get(i).getNome().equals("STOP") && !traceArray.get(i).getNome().equals("ERROR")){
			            nomeP = traceArray.get(i).getProcesso().getNome().toLowerCase();
			            nomeP += (traceArray.get(i).getProcesso().getEstado() == -1 ? "" : "_"+traceArray.get(i).getProcesso().getEstado());
			            String nomeA = traceArray.get(i).getNome()+(traceArray.get(i).getValorIndice() == -1 ? "" : "_"+traceArray.get(i).getValorIndice());
			            nomeA += traceArray.get(i).getId() == -1 ? "": "_"+traceArray.get(i).getId();
			            buff.append(
			                "                "+nomeA+"();\n"+
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
		        	nomeP = main.getNome()+(main.getEstado() == -1 ? "": "_"+main.getEstado());
		            buff.append(
		                "                System.out.println(\"STOP\");\n"+
		                "                thread"+nomeP+".interrupt();\n"
		            );
		        }
		        buff.append(
		            "            }\n"+
		            "        }catch(Exception e){}\n"+
		            "    }\n\n"+
		            "}"
		        );
		        buff.close();
		    }
			for(int i=0;i<shared.size();i++){
				if(shared.get(i) != null){
					String nome = shared.get(i).getNome();
					buffMain.append("        Semaforo "+nome+"_shared = new Semaforo("+conta(nome)+");\n");
				}
			}
			for(int i=0;i<pthreadArray.size();i++){
				Processo p = pthreadArray.get(i).getProcessos().get(0);
				String nome = p.getNome();
				buffMain.append(
					"        "+nome+" obj_"+nome.toLowerCase()+" = new "+nome+"("
				);
				for(int j=0;j<pthreadArray.get(i).getShared().size();j++){
					String nomeA = pthreadArray.get(i).getShared().get(j).getNome();
					if(j == pthreadArray.get(i).getShared().size()-1)
						buffMain.append(nomeA+"_shared");
					else
						buffMain.append(nomeA+"_shared, ");
				}
				buffMain.append(");\n");
			}
			buffMain.append(
				"    }\n\n"+
				"}"
			);
			buffMain.close();
	    }catch(Exception e){
	        System.out.println("Execessao: "+e.toString());
	    }
	}

	public String getNomeArq(){
		return this.nomeArq;
	}

}