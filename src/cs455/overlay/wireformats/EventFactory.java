package cs455.overlay.wireformats;

import cs455.overlay.node.Node;

public class EventFactory {
    public static void parse(byte[] data, Node node){
        Event e;
        node.onEvent(e);
    }
}
