package code;

public class MutexService {

    private static MutexService service = null;

    public static boolean validateMutexProtocolString(String protocolString) {
        assert protocolString != null;
        try {
            return MutexProtocol.valueOf(protocolString.toUpperCase()) != null;
        } catch (IllegalArgumentException e) {
            // invalid protocol provided
            return false;
        }
    }

    public static MutexService getService(String protocolString) {
        System.out.println("TBD");
        return service;
    }

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
