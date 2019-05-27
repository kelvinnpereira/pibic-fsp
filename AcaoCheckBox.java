import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AcaoCheckBox{

    private Acao acao;
    private JCheckBox box;
    private HiperGrafo grafo;

    AcaoCheckBox(){
    }

    AcaoCheckBox(Acao acao, JCheckBox box, HiperGrafo grafo){
        this.acao = acao;
        this.box = box;
        this.grafo = grafo;
    }

    public Acao getAcao(){
        return this.acao;
    }

    public JCheckBox getBox(){
        return this.box;
    }

    public HiperGrafo getGrafo(){
        return this.grafo;
    }

}
