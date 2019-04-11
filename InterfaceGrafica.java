import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.File;

public class InterfaceGrafica{

	public static final int N = 30;

	JFrame frame;
	JPanel panel;
    JButton generate, view;
    ArrayList<AcaoCheckBox> boxes;
    JTextArea text_area;
    JScrollPane scroll_text_area, scroll_panel;
    JOptionPane pop_up;
    ListenerBox listener_box;
    ListenerGenerate listener_generate;
    ListenerView listener_view;
    HiperGrafo grafo;
    Processo primeiro;
    ArrayList<Acao> traceArray;
    Geracao generator;

    InterfaceGrafica(HiperGrafo grafo, ArrayList<Acao> traceArray){
        frame = new JFrame("Animator");
        panel = new JPanel();
        scroll_panel = new JScrollPane(panel);
        listener_box = new ListenerBox();
        listener_generate = new ListenerGenerate();
        listener_view = new ListenerView();
        text_area = new JTextArea("");
        scroll_text_area = new JScrollPane(text_area);
        generate = new JButton("Generate");
        view = new JButton("View Code");
    	boxes = new ArrayList<AcaoCheckBox>();
        pop_up = new JOptionPane();
        this.grafo = grafo;
        this.traceArray = traceArray;
    }

    public void setPrimeiro(Processo primeiro){
        this.primeiro = primeiro;
    }

    public void setGenerator(Geracao generator){
        this.generator = generator;
    }

    public void start_interface(){
    	frame.setSize(320, 380);
        frame.setLayout(null);

    	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    	scroll_panel.setBounds(160, 40, 155, 265);
    	
    	text_area.setEditable(false);
    	scroll_text_area.setBounds(5, 5, 150, 300);

    	generate.setBounds(190, 5, 110, 30);
    	generate.addActionListener(listener_generate);

        view.setBounds(100, 310, 110, 30);
        view.setEnabled(false);
        view.addActionListener(listener_view);
    	
    	for(int i=0;i<boxes.size();i++){
            if(boxes.get(i).getAcao().getInicio() && boxes.get(i).getAcao().getProcesso() == primeiro){
        		boxes.get(i).getBox().setEnabled(true);
            }else{
                boxes.get(i).getBox().setEnabled(false);
        		boxes.get(i).getBox().setSelected(false);
            }
    		boxes.get(i).getBox().addActionListener(listener_box);
    		panel.add(boxes.get(i).getBox());
    	}

    	frame.add(scroll_text_area);
    	frame.add(generate);
        frame.add(view);
    	frame.add(scroll_panel);

    	frame.setLocationRelativeTo(null);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    	frame.setVisible(true);
        
    }

    private class ListenerBox implements ActionListener{
    	public void actionPerformed(ActionEvent e){
            for(int i=0;i<boxes.size();i++){
                boxes.get(i).getBox().setEnabled(false);
                boxes.get(i).getBox().setSelected(false);
            }
    		for(int i=0;i<boxes.size();i++){
    			if(e.getSource() == boxes.get(i).getBox()){
	        		text_area.setText(text_area.getText()+""+boxes.get(i).getBox().getText()+"\n");
                    traceArray.add(boxes.get(i).getAcao());
                    atualizaInterface(boxes.get(i));
                }
    		}
	    }
    }

    private class ListenerGenerate implements ActionListener{
    	public void actionPerformed(ActionEvent e){
            generator.gerate();
            pop_up.showMessageDialog(frame, "Generated Code Successful");
            generate.setEnabled(false);
            if(!traceArray.get(traceArray.size()-1).getNome().equals("ERROR")) view.setEnabled(true);
	    }
	}

    private class ListenerView implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                java.awt.Desktop.getDesktop().open(new File(generator.getNomeArq())); 
            }catch(Exception e1){
                System.out.println(e1);
            }
        }
    }
	
	public void addCheckBox(Acao acao){
        if(acao.getNome().equals("STOP") || acao.getNome().equals("ERROR")) return;
		boxes.add(new AcaoCheckBox(acao, new JCheckBox(acao.getNome()+(acao.getValorIndice() == -1 ? "" : "["+acao.getValorIndice()+"]"))));
	}

    public ArrayList<AcaoCheckBox> getBoxes(){
        return this.boxes;
    }

    private void atualizaInterface(AcaoCheckBox box){
        Acao a = box.getAcao();
        Vertice v = grafo.busca(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice()), stop_error = v.only();
        if(stop_error != null){
            traceArray.add(new Acao(stop_error.getNome(), null));
            text_area.setText(text_area.getText()+""+stop_error.getNome()+"\n");
            return;
        }
        for(int i=0;i<boxes.size();i++){
            ArrayList<Aresta> arestas = v.getArestas();
            for(int j=0;j<arestas.size();j++){
                ArrayList<Vertice> vertices = arestas.get(j).getVertices();
                for(int k=0;k<vertices.size();k++){
                    a = boxes.get(i).getAcao();                    
                    if(a.getNome().equals(vertices.get(k).getNome()) && a.getId() == vertices.get(k).getId() && a.getEstado() == vertices.get(k).getEstado() && a.getValorIndice() == vertices.get(k).getValorIndice()){
                        boxes.get(i).getBox().setEnabled(true);
                    }
                }
            }
        }
    }

}
