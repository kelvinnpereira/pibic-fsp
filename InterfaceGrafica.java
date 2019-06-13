import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

public class InterfaceGrafica{

	public static final int N = 30;

	JFrame frame;
	JPanel panel, tab1, tab2;
    JButton generate, view, compile;
    ArrayList<AcaoCheckBox> boxes;
    JTextArea text_area, editor_area;
    JScrollPane scroll_text_area, scroll_panel, scroll_editor_area;
    JOptionPane pop_up;
    JTabbedPane tabbedPane;
    ListenerBox listener_box;
    ListenerGenerate listener_generate;
    ListenerView listener_view;
    ListenerCompile listener_compile;
    ArrayList<HiperGrafo> grafoArray;
    Processo primeiro;
    Geracao generator;
    String last = "", dir = "";

    public void reset(){
        this.boxes = new ArrayList<AcaoCheckBox>();
    }

    InterfaceGrafica(){
        frame = new JFrame("Animator");
        tabbedPane = new JTabbedPane();
        
        tab1 = new JPanel();
        tab2 = new JPanel();
        
        listener_compile = new ListenerCompile();
        
        editor_area = new JTextArea("A = (a->A).");
        scroll_editor_area = new JScrollPane(editor_area);
        
        compile = new JButton("Compile");
    	boxes = new ArrayList<AcaoCheckBox>();
        pop_up = new JOptionPane();

        frame.setSize(320, 400);
        frame.setLayout(null);

        tab1.setLayout(null);
        tab2.setLayout(null);

        tabbedPane.setBounds(0, 0, 320, 380);

        scroll_editor_area.setBounds(5, 5, 307, 300);
        compile.setBounds(100, 310, 110, 30);
        compile.addActionListener(listener_compile);

        tab1.add(scroll_editor_area);
        tab1.add(compile);

        tabbedPane.add("Editor", tab1);
        tabbedPane.add("Generator", tab2);

        frame.add(tabbedPane);

    	frame.setLocationRelativeTo(null);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    	frame.setVisible(true);
    }

    public void setGrafoArray(ArrayList<HiperGrafo> grafoArray){
        this.grafoArray = grafoArray;
    }

    public void setGenerator(Geracao generator){
        this.generator = generator;
    }

    public void start_interface(){
        panel = new JPanel();
        scroll_panel = new JScrollPane(panel);
        listener_box = new ListenerBox();
        listener_generate = new ListenerGenerate();
        listener_view = new ListenerView();
        text_area = new JTextArea("");
        scroll_text_area = new JScrollPane(text_area);
        generate = new JButton("Generate");
        view = new JButton("View Code");
        //tb2
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
            boxes.get(i).getBox().setEnabled(false);
            boxes.get(i).getBox().setSelected(false);
            if(boxes.get(i).getAcao().getInicio() && generator.isPrimeiro(boxes.get(i).getAcao().getProcesso())) {
                if(boxes.get(i).getAcao().getCompartilhada()){
                    boxes.get(i).setCompartilhada(true);
        		    if(allAcao(boxes.get(i).getAcao().getNome()))
                        boxes.get(i).getBox().setEnabled(true);
                }else{
                    boxes.get(i).getBox().setEnabled(true);
                }
            }
    		boxes.get(i).getBox().addActionListener(listener_box);
    		panel.add(boxes.get(i).getBox());
    	}

    	tab2.add(scroll_text_area);
    	tab2.add(generate);
        tab2.add(view);
    	tab2.add(scroll_panel);
    }

    private void disableAll(){
        for(int i=0;i<boxes.size();i++){
            boxes.get(i).getBox().setEnabled(false);
            boxes.get(i).getBox().setSelected(false);
        }
    } 

    private int indexGrafo(HiperGrafo grafo){
        for(int i=0;i<grafoArray.size();i++){
            if(grafoArray.get(i) == grafo) return i;
        }
        return -1;
    }

    private class ListenerBox implements ActionListener{
    	public void actionPerformed(ActionEvent e){
            AcaoCheckBox box = null;
            for(int i=0;i<boxes.size();i++){
                if(e.getSource() == boxes.get(i).getBox()) box = boxes.get(i);
            }
            for(int i=0;i<boxes.size();i++){
                if(box.getGrafo() == boxes.get(i).getGrafo()){
                    boxes.get(i).getBox().setEnabled(false);
                    boxes.get(i).getBox().setSelected(false);
                }
            }
    		for(int i=0;i<boxes.size();i++){
    			if(e.getSource() == boxes.get(i).getBox()){
	        		text_area.setText(text_area.getText()+""+boxes.get(i).getBox().getText()+"\n");
                    last = boxes.get(i).getBox().getText();
                    generator.getPthreadArray().get(indexGrafo(boxes.get(i).getGrafo())).getTraceArray().add(boxes.get(i).getAcao());
                    if(atualizaInterface(boxes.get(i))){
                        disableAll();
                        break;
                    }
                }else if(box.getAcao().getCompartilhada() && boxes.get(i).getAcao().getNome().equals(box.getAcao().getNome())){
                    boxes.get(i).getBox().setEnabled(false);
                    boxes.get(i).getBox().setSelected(false);
                    generator.getPthreadArray().get(indexGrafo(boxes.get(i).getGrafo())).getTraceArray().add(boxes.get(i).getAcao());
                    if(atualizaInterface(boxes.get(i))){
                        disableAll();
                        break;
                    }
                }
    		}
	    }
    }

    private class ListenerGenerate implements ActionListener{
    	public void actionPerformed(ActionEvent e){
            JFileChooser chooser = new JFileChooser(); 
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Select the directory where to save");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){ 
                InterfaceGrafica.this.dir = chooser.getSelectedFile().getPath();
                generator.gerate(dir);
                pop_up.showMessageDialog(frame, "Generated Code Successful");
                generate.setEnabled(false);
                if( !last.equals("ERROR") ) view.setEnabled(true);
            }else{
                pop_up.showMessageDialog(frame, "No Selection");
            }
	    }
	}

    private class ListenerView implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                java.awt.Desktop.getDesktop().open(new File(dir+"/"+generator.getNomeArq())); 
            }catch(Exception e1){
                System.out.println(e1);
            }
        }
    }

    private class ListenerCompile implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                InterfaceGrafica.this.reset();
                tab2.removeAll();
                String string = editor_area.getText();
				InputStream inputStream = new ByteArrayInputStream(string.getBytes(Charset.forName("UTF-8")));
				Scanner scanner = new Scanner(inputStream);
				Parser parser = new Parser(scanner);
                parser.setIg(InterfaceGrafica.this);
				parser.Parse();
            }catch(Exception e1){
                System.out.println();
                e1.printStackTrace();
            }
        }
    }
	
	public void addCheckBox(Acao acao, HiperGrafo grafo){
        if(acao.getNome().equals("STOP") || acao.getNome().equals("ERROR")) return;
		boxes.add(new AcaoCheckBox(acao, new JCheckBox(acao.getNome()+(acao.getValorIndice() == -1 ? "" : "["+acao.getValorIndice()+"]")), grafo));
	}

    public ArrayList<AcaoCheckBox> getBoxes(){
        return this.boxes;
    }

    public boolean allAcao(String nome){
        for(int i=0;i<boxes.size();i++){
            if(boxes.get(i).getAcao().getNome().equals(nome) && !boxes.get(i).getCompartilhada()) return false;
        }
        for(int i=0;i<boxes.size();i++){
            if(boxes.get(i).getAcao().getNome().equals(nome)){
                boxes.get(i).setCompartilhada(false);
                boxes.get(i).getBox().setEnabled(true);
            }
        }
        return true;
    }

    private boolean atualizaInterface(AcaoCheckBox box){
        HiperGrafo grafo;
        Acao a = box.getAcao();
        for(int cont=0;cont<grafoArray.size();cont++){
            if(box.getGrafo() == grafoArray.get(cont)){
                grafo = grafoArray.get(cont);
                Vertice v = grafo.busca(a.getNome(), a.getId(), a.getEstado(), a.getValorIndice()), stop_error = v.only();
                if(stop_error != null){
                    generator.getPthreadArray().get(indexGrafo(box.getGrafo())).getTraceArray().add(new Acao(stop_error.getNome(), null));
                    text_area.setText(text_area.getText()+""+stop_error.getNome()+"\n");
                    last = stop_error.getNome();
                    return true;
                }
                for(int i=0;i<boxes.size();i++){
                    ArrayList<Aresta> arestas = v.getArestas();
                    for(int j=0;j<arestas.size();j++){
                        ArrayList<Vertice> vertices = arestas.get(j).getVertices();
                        for(int k=0;k<vertices.size();k++){
                            a = boxes.get(i).getAcao();                    
                            if(boxes.get(i).getGrafo() == grafo && a.getNome().equals(vertices.get(k).getNome()) && a.getId() == vertices.get(k).getId() && a.getEstado() == vertices.get(k).getEstado() && a.getValorIndice() == vertices.get(k).getValorIndice()){
                                if(vertices.get(k).getCompartilhada()){
                                    boxes.get(i).setCompartilhada(true);
                                    if(allAcao(boxes.get(i).getAcao().getNome())) boxes.get(i).getBox().setEnabled(true);
                                }else{
                                    boxes.get(i).getBox().setEnabled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
