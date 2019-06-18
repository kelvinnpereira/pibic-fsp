import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

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
                    java.awt.Desktop.getDesktop().open(new File("Generated/"+generator.getArquivos().get(i).getName()));
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
    private JMenu fileMenu, helpMenu;
    private JMenuItem newAction, openAction, helpAction;
    private JTabbedPane tabbedPane;
    private JPanel tab1, form;
    private ListenerCompile listener_compile;
    private JTextArea editor_area, nome, matricula;
    private JScrollPane scroll_editor_area;
    private JButton compile;

    InterfaceGrafica(){
        main = new JFrame("Animator");

        menuBar = new JMenuBar();
        menuBar.setBounds(0, 0, 900, 20);
        fileMenu = new JMenu("File");
        newAction = new JMenuItem("New");
        newAction.addActionListener(new ListenerNew());
        openAction = new JMenuItem("Open");
        openAction.addActionListener(new ListenerOpen());
        helpMenu = new JMenu("Help");
        helpAction = new JMenuItem("Help");

        fileMenu.add(newAction);
        fileMenu.add(openAction);
        helpMenu.add(helpAction);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        form = new JPanel();
        form.setBounds(0, 30, 400, 60);
        form.setLayout(null);

        JLabel label_nome = new JLabel("Nome: ");
        label_nome.setBounds(0, 0, 120, 30);
        nome = new JTextArea();
        nome.setBounds(120, 5, 250, 20);
        JLabel label_matricula = new JLabel("Nº de Matricula: ");
        label_matricula.setBounds(0, 35, 120, 30);
        matricula = new JTextArea();
        matricula.setBounds(120, 40, 150, 20);

        form.add(label_nome);
        form.add(nome);
        form.add(label_matricula);
        form.add(matricula);

        tabbedPane = new JTabbedPane();
        
        tab1 = new JPanel();
        
        listener_compile = new ListenerCompile();
        
        editor_area = new JTextArea("A = (a->A).\nB = (b->B).\n||A_B = (A || B).");
        scroll_editor_area = new JScrollPane(editor_area);
        
        compile = new JButton("Compile");

        main.setSize(900, 610);
        main.setLayout(null);

        tab1.setLayout(null);

        //tabbedPane.setBounds(5, 100, 890, 460);
        tabbedPane.setBounds(5, 100, 890, 415);

        scroll_editor_area.setBounds(5, 5, 880, 380);
        compile.setBounds(390, 530, 110, 30);
        compile.addActionListener(listener_compile);

        tab1.add(scroll_editor_area);
        main.add(compile);

        tabbedPane.add("Editor", tab1);

        main.add(menuBar);
        main.add(form);
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

    private class ListenerNew implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                String fsp = editor_area.getText();
                JFileChooser chooser = new JFileChooser();
                JOptionPane pop_up = new JOptionPane();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    String path = chooser.getSelectedFile().getPath();
                    BufferedWriter buff = new BufferedWriter(new FileWriter(path));
                    buff.append("/*Nome: "+nome.getText()+"\nMatricula: "+matricula.getText()+"*/\n");
                    buff.append(editor_area.getText());
                    buff.close();
                }else{
                    pop_up.showMessageDialog(main, "No Selected Directory");
                }
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    private class ListenerOpen implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                String fsp = editor_area.getText();
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                JOptionPane pop_up = new JOptionPane();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    editor_area.setText(new String(Files.readAllBytes(Paths.get(chooser.getSelectedFile().getPath()))));
                }else{
                    pop_up.showMessageDialog(main, "No Selected Directory");
                }
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    private class ListenerHelp implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

}
