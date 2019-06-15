import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

class Trace{

    private JFrame trace;
    private JPanel panel;
    private JScrollPane scroll_panel, scroll_text_area;
    private ListenerBox listener_box;
    private ListenerGenerate listener_generate;
    private ListenerView listener_view;
    private JTextArea text_area;
    private JButton generate, view;
    private Geracao generator;
    private ArrayList<HiperGrafo> grafoArray;
    private ArrayList<AcaoCheckBox> boxes;
    private String last = "";

    Trace(){
        this.boxes = new ArrayList<AcaoCheckBox>();
    }

    public void setGenerator(Geracao generator){
        this.generator = generator;
    }

    public void setGrafoArray(ArrayList<HiperGrafo> grafoArray){
        this.grafoArray = grafoArray;
    }
    
    public void start_trace(){
        
        trace = new JFrame("Trace");
        trace.setSize(320, 380);
        trace.setLayout(null);
        panel = new JPanel();
        scroll_panel = new JScrollPane(panel);
        listener_box = new ListenerBox();
        listener_generate = new ListenerGenerate();
        listener_view = new ListenerView();
        text_area = new JTextArea("");
        scroll_text_area = new JScrollPane(text_area);
        generate = new JButton("Generate");
        view = new JButton("View Code");

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

    	trace.add(scroll_text_area);
    	trace.add(generate);
        trace.add(view);
    	trace.add(scroll_panel);

        trace.setLocationRelativeTo(null);
    	//trace.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        trace.setResizable(false);
    	trace.setVisible(true);
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
            generator.gerate();
            JOptionPane pop_up = new JOptionPane();
            pop_up.showMessageDialog(trace, "Generated Code Successful");
            generate.setEnabled(false);
            if( !last.equals("ERROR") ) view.setEnabled(true);
	    }
	}

    private class ListenerView implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                for(int i=0;i<generator.getArquivos().size();i++){
                    java.awt.Desktop.getDesktop().open(new File(generator.getArquivos().get(i).getName()));
                } 
            }catch(Exception e1){
                System.out.println(e1);
            }
        }
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

    public void addCheckBox(Acao acao, HiperGrafo grafo){
        if(acao.getNome().equals("STOP") || acao.getNome().equals("ERROR")) return;
		boxes.add(new AcaoCheckBox(acao, new JCheckBox(acao.getNome()+(acao.getValorIndice() == -1 ? "" : "["+acao.getValorIndice()+"]")), grafo));
	}
} 

public class InterfaceGrafica{

	public static final int N = 30;

    private JFrame main;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newAction;
    private JMenuItem openAction;
    private JTabbedPane tabbedPane;
    private JPanel tab1;
    private ListenerCompile listener_compile;
    private JTextArea editor_area;
    private JScrollPane scroll_editor_area;
    private JButton compile;

    InterfaceGrafica(){
        main = new JFrame("Animator");

        menuBar = new JMenuBar();
        menuBar.setBounds(0, 0, 60, 30);
        fileMenu = new JMenu("File");
        newAction = new JMenuItem("New");
        openAction = new JMenuItem("Open");

        fileMenu.add(newAction);
        fileMenu.add(openAction);

        menuBar.add(fileMenu);

        tabbedPane = new JTabbedPane();
        
        tab1 = new JPanel();
        
        listener_compile = new ListenerCompile();
        
        editor_area = new JTextArea("A = (a->A).\nB = (b->B).\n||A_B = (A || B).");
        scroll_editor_area = new JScrollPane(editor_area);
        
        compile = new JButton("Compile");

        main.setSize(900, 600);
        main.setLayout(null);

        tab1.setLayout(null);

        tabbedPane.setBounds(5, 100, 890, 460);

        scroll_editor_area.setBounds(5, 5, 307, 300);
        compile.setBounds(0, 50, 110, 30);
        compile.addActionListener(listener_compile);

        tab1.add(scroll_editor_area);
        main.add(compile);

        tabbedPane.add("Editor", tab1);

        main.add(menuBar);
        main.add(tabbedPane);

    	main.setLocationRelativeTo(null);
    	main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setResizable(false);
    	main.setVisible(true);
    }

    private class ListenerCompile implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                String string = editor_area.getText();
				InputStream inputStream = new ByteArrayInputStream(string.getBytes(Charset.forName("UTF-8")));
				Scanner scanner = new Scanner(inputStream);
				Parser parser = new Parser(scanner);
                parser.setTrace(new Trace());
				parser.Parse();
            }catch(Exception e1){
                System.out.println();
                e1.printStackTrace();
            }
        }
    }

}
