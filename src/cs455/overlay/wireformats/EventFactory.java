package cs455.overlay.wireformats;

import cs455.overlay.node.Node;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class EventFactory {
    public static void parse(byte[] data, Node node) throws java.io.IOException{
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(data.clone());
        DataInputStream din = new DataInputStream (new BufferedInputStream(baInputStream));
        int msgType = din.readInt();
        Event event = createEvent(msgType, data);
        node.onEvent(event);
    }

    public static Event createEvent(int msgType, byte[] data) throws java.io.IOException{

        switch (msgType) {
            case Protocol.OVERLAY_NODE_SENDS_REGISTRATION:
                return new OverlayNodeSendsRegistration(data);
            case Protocol.REGISTRY_REPORTS_REGISTRATION_STATUS:
                return new RegistryReportsRegistrationStatus(data);
            case Protocol.OVERLAY_NODE_SENDS_DEREGISTRATION:
                return new OverlayNodeSendsDeregistration(data);
            case Protocol.REGISTRY_REPORTS_DEREGISTRATION_STATUS:
                return new RegistryReportsDeregistrationStatus(data);
            case Protocol.REGISTRY_SENDS_NODE_MANIFEST:
                return new RegistrySendsNodeManifest(data);
            case Protocol.NODE_REPORTS_OVERLAY_SETUP_STATUS:
                return new NodeReportsOverlaySetupStatus(data);
            case Protocol.REGISTRY_REQUESTS_TASK_INITIATE:
                return new RegistryRequestsTaskInitiate(data);
            case Protocol.OVERLAY_NODE_SENDS_DATA:
                return new OverlayNodeSendsData(data);
            case Protocol.OVERLAY_NODE_REPORTS_TASK_FINISHED:
                return new OverlayNodeReportsTaskFinished(data);
            case Protocol.REGISTRY_REQUESTS_TRAFFIC_SUMMARY:
                return new RegistryRequestsTrafficSummary(data);
            case Protocol.OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY:
                return new OverlayNodeReportsTrafficSummary(data);
        }
        return null;
    }
}
