import javax.swing.*;
import javax.swing.UIManager.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;

class Trace{

    private JFrame trace;
    private JPanel panel;
    private JScrollPane scroll_panel, scroll_text_area;
    private JTextArea text_area;
    private JButton generate, view;
    private ListenerBox listener_box;
    private Geracao generator;
    private ArrayList<HiperGrafo> grafoArray;
    private ArrayList<JCheckBox> boxes;
    private String last = "";

    Trace(){
        this.boxes = new ArrayList<JCheckBox>();
    }

    public void setGenerator(Geracao generator){
        this.generator = generator;
    }

    public void setGrafoArray(ArrayList<HiperGrafo> grafoArray){
        this.grafoArray = grafoArray;
    }

    public boolean inAtual(String nome){
        for(HiperGrafo grafo: grafoArray){
            if(grafo.inAtual(nome) != null) return true;
        }
        return false;
    }
    
    public void start_trace(){
        Font font = new Font("Dialog", Font.BOLD, 16);
        trace = new JFrame("Trace");
        trace.setSize(435, 485);
        trace.setLayout(null);
        trace.setFont(font);
        panel = new JPanel();
        panel.setFont(font);
        scroll_panel = new JScrollPane(panel);
        listener_box = new ListenerBox();
        text_area = new JTextArea("");
        text_area.setFont(font);
        scroll_text_area = new JScrollPane(text_area);
        generate = new JButton("Generate");
        generate.setFont(font);
        view = new JButton("View Code");
        view.setFont(font);

    	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    	scroll_panel.setBounds(210, 40, 205, 365);
    	
    	text_area.setEditable(false);
    	scroll_text_area.setBounds(5, 5, 200, 400);

    	generate.setBounds(250, 5, 130, 30);
    	generate.addActionListener(new ListenerGenerate());

        view.setBounds(140, 410, 130, 30);
        view.setEnabled(false);
        view.addActionListener(new ListenerView());
    	
    	for(int i=0;i<boxes.size();i++){
            JCheckBox box = boxes.get(i);
            box.setEnabled(false);
            box.setFont(font);
            if( inAtual(box.getText()) ){
                atualizaInterface(box);
            }
    		box.addActionListener(listener_box);
    		panel.add(box);
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
            boxes.get(i).setEnabled(false);
            boxes.get(i).setSelected(false);
        }
    } 

    private int enableAllAtual(){
        for(int i=0;i<grafoArray.size();i++){
            if(grafoArray.get(i).getAtual().size() == 0) grafoArray.get(i).setAtual(new ArrayList<Vertice>(grafoArray.get(i).getTrava()));
        }
        return -1;
    }

    private class ListenerBox implements ActionListener{
    	public void actionPerformed(ActionEvent e){
            JCheckBox box = null;
            for(int i=0;i<boxes.size();i++){
                if(e.getSource() == boxes.get(i)) 
                    box = boxes.get(i);
            }
            text_area.setText(text_area.getText()+""+box.getText()+"\n");
            disableAll();
            boolean flag = true;
            boolean all = true;
            Vertice stop_error = null;
            for(int i=0;i<grafoArray.size();i++){
                Vertice v = grafoArray.get(i).inAtual(box.getText());
                if(v != null){
                    Acao a = new Acao(v.getNome(), null, "", v.getValorIndice(), v.getEstado(), v.getTrava());
                    a.setId(v.getId());
                    generator.getPthreadArray().get(i).getTraceArray().add(a);
                    stop_error = v.only();
                    grafoArray.get(i).setAtual(new ArrayList<Vertice>());
                    if(stop_error != null){
                        generator.getPthreadArray().get(i).getTraceArray().add(new Acao(stop_error.getNome(), null));
                        last = stop_error.getNome();
                        if (!stop_error.getCompartilhada())
                            disableAll();
                    }
                    ArrayList<Aresta> arestas = v.getArestas();
                    for(int j=0;j<arestas.size();j++){
                        ArrayList<Vertice> vertices = arestas.get(j).getVertices();
                        for(int k=0;k<vertices.size();k++){
                            grafoArray.get(i).getAtual().add(vertices.get(k));
                        }
                    }
                    if(grafoArray.get(i).AtualETrava()){
                        flag = false;
                        enableAllAtual();
                    }
                }
            }
            if(flag){
                for(int i=0;i<grafoArray.size();i++){
                    if(grafoArray.get(i).AtualETrava()) grafoArray.get(i).setAtual(new ArrayList<Vertice>());
                }
            }
            flag = true;
            for (HiperGrafo grafo: grafoArray) {
                if (!grafo.isEmpty()) {
                    ArrayList<Vertice> atual = grafo.getAtual();
                    flag &= atual.size() == 1 && (atual.get(0).getNome().equals("STOP") || atual.get(0).getNome().equals("ERROR"));
                }
            }
            if (flag) {
                text_area.setText(text_area.getText()+""+stop_error.getNome()+"\n");
            }
            //if(last.equals("STOP") || last.equals("ERROR")) return;
            for(int i=0;i<boxes.size();i++){
                if(inAtual(boxes.get(i).getText())){
                    atualizaInterface(boxes.get(i));
                }
            }
            last = box.getText();
	    }
    }

    private void atualizaInterface(JCheckBox box){
        for(int i=0;i<grafoArray.size();i++){
            if(grafoArray.get(i).inVertices(box.getText()) && grafoArray.get(i).inAtual(box.getText()) == null){
                return;
            }
        }
        box.setEnabled(true);
    }

    private class ListenerGenerate implements ActionListener{
    	public void actionPerformed(ActionEvent e){
            generator.gerate();
            JOptionPane.showMessageDialog(trace, "Generated Code Successful");
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

    public ArrayList<JCheckBox> getBoxes(){
        return this.boxes;
    }

    public void addCheckBox(String nome){
        if(nome.equals("STOP") || nome.equals("ERROR")) return;
        for(int i=0;i<boxes.size();i++){
            if(boxes.get(i).getText().equals(nome)) return;
        }
		boxes.add(new JCheckBox(nome));
	}
} 

public class InterfaceGrafica{

	public static final int N = 30;

    JFrame main;
    JMenuBar menuBar;
    JMenu fileMenu, helpMenu;
    JMenuItem newAction, openAction, helpAction, saveAction;
    JTabbedPane tabbedPane;
    JPanel editor, output, form, help;
    JTextArea editor_area, nome, matricula, output_area;
    JScrollPane scroll_editor_area, scroll_output_area;
    JButton compile, tracing;
    Parser parser;
    JEditorPane edit;
    ArrayList<URL> urlArray = new ArrayList<URL>();
    int x = 900, y = 600;

    InterfaceGrafica(){
        try{
        	for(LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()){
                if("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())){   
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                } 
            }
    	}catch(Exception ignored){}
        Font font = new Font("Dialog", Font.BOLD, 16);
        main = new JFrame("Animator");

        menuBar = new JMenuBar();
        menuBar.setBounds(0, 0, x, 20);
        fileMenu = new JMenu("File");
        fileMenu.setFont(font);
        newAction = new JMenuItem("New");
        newAction.addActionListener(new ListenerNew());
        newAction.setFont(font);
        saveAction = new JMenuItem("Save");
        saveAction.addActionListener(new ListenerSave());
        saveAction.setFont(font);
        openAction = new JMenuItem("Open");
        openAction.addActionListener(new ListenerOpen());
        openAction.setFont(font);
        helpMenu = new JMenu("Help");
        helpMenu.setFont(font);
        helpAction = new JMenuItem("Help");
        helpAction.addActionListener(new ListenerHelp());
        helpAction.setFont(font);

        fileMenu.add(newAction);
        fileMenu.add(saveAction);
        fileMenu.add(openAction);
        helpMenu.add(helpAction);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        form = new JPanel();
        form.setBounds(0, 30, x, 100);
        form.setLayout(null);


        JLabel label_nome = new JLabel("Student Name: ");
        label_nome.setBounds(10, 0, 170, 30);
        label_nome.setFont(font);
        nome = new JTextArea();
        nome.setBounds(170, 5, x-200, 25);
        nome.setFont(font);
        nome.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
        JLabel label_matricula = new JLabel("Student iD: ");
        label_matricula.setBounds(10, 50, 170, 30);
        label_matricula.setFont(font);
        matricula = new JTextArea();
        matricula.setBounds(170, 55, 150, 25);
        matricula.setFont(font);
        matricula.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        form.add(label_nome);
        form.add(nome);
        form.add(label_matricula);
        form.add(matricula);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(font);
        
        editor = new JPanel();
        output = new JPanel();
        
        editor_area = new JTextArea("A = (a->A).\nB = (b->B).\n||A_B = (A || B).");
        editor_area.setFont(font);
        editor_area.setTabSize(4);
        output_area = new JTextArea("");
        output_area.setEditable(false);
        output_area.setFont(font);
        scroll_editor_area = new JScrollPane(editor_area);
        scroll_output_area = new JScrollPane(output_area);
        
        compile = new JButton("Compile");
        compile.setFont(font);
        tracing = new JButton("Tracing");
        tracing.setFont(font);
        tracing.setEnabled(false);

        main.setSize(x, y);
        main.setLayout(null);

        editor.setLayout(null);
        output.setLayout(null);

        tabbedPane.setBounds(5, 150, x-25, y-290);
        scroll_editor_area.setBounds(5, 5, x-40, y-330);
        scroll_output_area.setBounds(5, 5, x-40, y-330);

        compile.setBounds(x/2-120, y-100, 110, 30);
        compile.addActionListener(new ListenerCompile());
        tracing.setBounds(x/2+10, y-100, 110, 30);
        tracing.addActionListener(new ListenerTracing());

        editor.add(scroll_editor_area);
        output.add(scroll_output_area);
        main.add(compile);
        main.add(tracing);

        tabbedPane.add("Editor", editor);
        tabbedPane.add("Output", output);

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
                output_area.setText("");
                tabbedPane.setSelectedIndex(1);
                String string = editor_area.getText();
				InputStream inputStream = new ByteArrayInputStream(string.getBytes(Charset.forName("UTF-8")));
				Scanner scanner = new Scanner(inputStream);
				parser = new Parser(scanner);
                parser.setTrace(new Trace());
                parser.setIg(InterfaceGrafica.this);
				parser.Parse();
                if(parser.errors.count == 0){
                    output_area.setText("Compile Successful");
                    tracing.setEnabled(true);
                }else{   
                    String s = parser.baos.toString();
                    output_area.setText(output_area.getText() + "" + s + (s.equals("") ? "":"\n"));
                    tracing.setEnabled(false);
                }
            }catch(Exception e1){
                System.out.println();
                e1.printStackTrace();
            }
        }
    }

    private class ListenerTracing implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                parser.startInterface();
                tracing.setEnabled(false);
            }catch(Exception e1){
                System.out.println();
                e1.printStackTrace();
            }
        }
    }

    private class ListenerNew implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                editor_area.setText("");
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    private class ListenerSave implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save");
                chooser.setApproveButtonText("Save");
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    String path = chooser.getSelectedFile().getPath();
                    BufferedWriter buff = new BufferedWriter(new FileWriter(path));
                    buff.append("/*Nome: "+nome.getText()+"\nMatricula: "+matricula.getText()+"*/\n");
                    buff.append(editor_area.getText());
                    buff.close();
                }else{
                    JOptionPane.showMessageDialog(main, "No Selected Directory");
                }
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    private class ListenerOpen implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    editor_area.setText(new String(Files.readAllBytes(Paths.get(chooser.getSelectedFile().getPath()))));
                }else{
                    JOptionPane.showMessageDialog(main, "No Selected Directory");
                }
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    private class ListenerLink extends MouseAdapter{
        public void mousePressed(MouseEvent e){
            try{
                Point pt = new Point(e.getX(), e.getY());
                JEditorPane edit_pane = (JEditorPane) e.getSource();
                @SuppressWarnings("deprecation")
                int pos = edit_pane.viewToModel(pt);
                if(pos < 0) return;
                Document doc = edit_pane.getDocument();
                HTMLDocument hdoc = (HTMLDocument) doc;
                Element elem = hdoc.getCharacterElement(pos);
                AttributeSet a = elem.getAttributes();
                AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);
                String href = (anchor != null) ? (String) anchor.getAttribute(HTML.Attribute.HREF) : null;
                if (href != null) {
                    edit.setPage(getClass().getResource("/help/"+href));
                }
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    private class ListenerHelp implements ActionListener{
        public void actionPerformed(ActionEvent e){
            try{
                if(tabbedPane.getTabCount() == 3) tabbedPane.remove(2);
                help = new JPanel();
                help.setLayout(null);
                edit = new JEditorPane();
                edit.setEditable(false);
                edit.setPage(getClass().getResource("/help/help.html"));
                edit.addMouseListener(new ListenerLink());
                JScrollPane editorScrollPane = new JScrollPane(edit);
                editorScrollPane.setBounds(5, 5, x-40, y-330);
                help.add(editorScrollPane);
                tabbedPane.add("Help", help);
                tabbedPane.setSelectedIndex(2);
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

}
