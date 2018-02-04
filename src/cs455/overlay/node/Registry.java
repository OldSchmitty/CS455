package cs455.overlay.node;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
public class Registry implements Node{
    private int portNum;
    private TCPServerThread server;
    public Registry(int portNum){
        this.portNum = portNum;
        server = new TCPServerThread(this);
    }

    private void setup() {

    }

    public void onEvent(Event event){

    }


}
