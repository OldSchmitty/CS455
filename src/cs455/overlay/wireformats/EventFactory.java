package cs455.overlay.wireformats;

import cs455.overlay.node.Node;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.Socket;

public class EventFactory {
    public static void parse(byte[] data, Node node, Socket socket) throws java.io.IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data.clone());
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        int msgType = din.readByte();
        Event event = createEvent(msgType, data.clone());
        node.onEvent(event, socket);
    }

    public static Event createEvent(int msgType, byte[] data) throws java.io.IOException{

        switch (msgType) {
            case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
                return new OverlayNodeSendsRegistration(data.clone());
            case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
                return new RegistryReportsRegistrationStatus(data.clone());
            case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
                return new OverlayNodeSendsDeregistration(data.clone());
            case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                return new RegistryReportsDeregistrationStatus(data.clone());
            case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
                return new RegistrySendsNodeManifest(data.clone());
            case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
                return new NodeReportsOverlaySetupStatus(data.clone());
            case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
                return new RegistryRequestsTaskInitiate(data.clone());
            case Protocol.OVERLAY_NODE_SENDS_DATA:
                return new OverlayNodeSendsData(data.clone());
            case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
                return new OverlayNodeReportsTaskFinished(data.clone());
            case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                return new RegistryRequestsTrafficSummary(data.clone());
            case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                return new OverlayNodeReportsTrafficSummary(data.clone());
        }
        return null;
    }
}
