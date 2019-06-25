public class ProcessInstance{

    String share, prefix;
    Process process;

    ProcessInstance(String share, String prefix, Process process){
        this.share = share;
        this.prefix = prefix;
        this.process = process;
    }

}