package code;

import java.util.List;

public abstract class MutexService {

    private static MutexService service = null;
    private static List<Node> allNodes = null;

    public MutexService(List<Node> nodes) {
        allNodes = nodes;
    }

    public static boolean validateMutexProtocolString(String protocolString) {
        assert protocolString != null;
        try {
            return MutexProtocol.valueOf(protocolString.toUpperCase()) != null;
        } catch (IllegalArgumentException e) {
            // invalid protocol provided
            return false;
        }
    }

    public static MutexService getService(String protocolString, List<Node> nodes) {
        if (MutexProtocol.valueOf(protocolString.toUpperCase()) == MutexProtocol.RC) {
            service = new RoucairolCarvalho(nodes);
        } else if (MutexProtocol.valueOf(protocolString.toUpperCase()) == MutexProtocol.RA) {
            service = new RicartAgrawala(nodes);
        }
        return service;
    }

    public abstract void csEnter();

    public abstract void csLeave();

    private enum MutexProtocol {
        RC, // Roucairol and Carvalho’s
        RA, // Ricart and Agrawala’s
        ;
    }

    public static void main(String[] args) {
        System.out.println("TEST RA: " + validateMutexProtocolString("RA"));
        System.out.println("TEST RC: " + validateMutexProtocolString("RC"));
        System.out.println("TEST ra: " + validateMutexProtocolString("ra"));
        System.out.println("TEST rc: " + validateMutexProtocolString("rc"));
        System.out.println("TEST rca: " + validateMutexProtocolString("rca"));
        System.out.println("TEST RCA: " + validateMutexProtocolString("RCA"));
    }

}
