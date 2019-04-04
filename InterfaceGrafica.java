import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class InterfaceGrafica{

	public static final int N = 30;

	JFrame frame;
	JPanel panel;
    JButton compile;
    ArrayList<AcaoCheckBox> boxes;
    JTextArea text_area;
    JScrollPane scroll_text_area, scroll_panel;
    ListenerBox listener_box;
    ListenerButton listener_button;

    InterfaceGrafica(){
        frame = new JFrame("Animator");
        panel = new JPanel();
        scroll_panel = new JScrollPane(panel);
        listener_box = new ListenerBox();
        listener_button = new ListenerButton();
        text_area = new JTextArea("");
        scroll_text_area = new JScrollPane(text_area);
        compile = new JButton("compile");
    	boxes = new ArrayList<AcaoCheckBox>();
    }

    public void start_interface(){
    	frame.setSize(320, 340);
        frame.setLayout(null);

    	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    	scroll_panel.setBounds(160, 40, 155, 265);
    	
    	text_area.setEditable(false);
    	scroll_text_area.setBounds(5, 5, 150, 300);

    	compile.setBounds(195, 5, 90, 30);
    	compile.addActionListener(listener_button);
    	
    	for(int i=0;i<boxes.size();i++){
            System.out.println("inicio: "+boxes.get(i).getAcao().getInicio());
            if(boxes.get(i).getAcao().getInicio()){
        		boxes.get(i).getBox().setEnabled(true);
        		boxes.get(i).getBox().setSelected(true);
            }else{
                boxes.get(i).getBox().setEnabled(false);
        		boxes.get(i).getBox().setSelected(false);
            }
    		boxes.get(i).getBox().addActionListener(listener_box);
    		panel.add(boxes.get(i).getBox());
    	}

    	frame.add(scroll_text_area);
    	frame.add(compile);
    	frame.add(scroll_panel);

    	frame.setLocationRelativeTo(null);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    	frame.setVisible(true);
        
    }

    private class ListenerBox implements ActionListener{
    	public void actionPerformed(ActionEvent e){
    		for(int i=0;i<boxes.size();i++){
    			if(e.getSource() == boxes.get(i).getBox() && !boxes.get(i).getBox().isSelected())
	        		text_area.setText(text_area.getText()+""+boxes.get(i).getBox().getText()+"\n");
    		}
	    }
    }

    private class ListenerButton implements ActionListener{
    	public void actionPerformed(ActionEvent e){
    		AbstractButton button = (AbstractButton)e.getSource();
    		text_area.setText(text_area.getText()+""+button.getText()+"\n");
            frame.dispose();
	    }
	}
	
	public void addCheckBox(Acao acao){
		boxes.add(new AcaoCheckBox(acao, new JCheckBox(acao.getNome()+(acao.getValorIndice() == -1 ? "" : "["+acao.getValorIndice()+"]"))));
	}

    public ArrayList<AcaoCheckBox> getBoxes(){
        return this.boxes;
    }

}
