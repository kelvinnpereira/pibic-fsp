import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AcaoCheckBox{

    private Acao acao;
    private JCheckBox box;
    private HiperGrafo grafo;
    private boolean compartilhada;

    AcaoCheckBox(){
    }

    AcaoCheckBox(Acao acao, JCheckBox box, HiperGrafo grafo){
        this.acao = acao;
        this.box = box;
        this.grafo = grafo;
        this.compartilhada = false;
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

    public boolean getCompartilhada(){
        return this.compartilhada;
    }

    public void setCompartilhada(boolean val){
        this.compartilhada = val;
    }

}