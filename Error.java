public class Error extends Exception{

    Error(String msg){
        super(msg);
    }

    Error(String msg, Throwable cause){
        super(msg, cause);
    }

}