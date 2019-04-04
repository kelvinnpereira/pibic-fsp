import java.util.Scanner;
import javax.script.*;

public class Main{

	public static void main(String args[]){
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine eng = mgr.getEngineByName("JavaScript");
		String expr = "3 <= 3";
		try{
			boolean b = (boolean)eng.eval(expr);
			System.out.println(b);
		}catch(ScriptException se){}
	}
   
}