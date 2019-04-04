import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AcaoCheckBox{

    private Acao acao;
    private JCheckBox box;

    AcaoCheckBox(){
    }

    AcaoCheckBox(Acao acao, JCheckBox box){
        this.acao = acao;
        this.box = box;
    }

    public Acao getAcao(){
        return this.acao;
    }

    public JCheckBox getBox(){
        return this.box;
    }

}
