package hashtree;

import framework.Authenticator;

import java.util.Arrays;

/**
 * The authenticator value of the hash tree. Consists o
 */
public class HTAuthenticator extends Authenticator {
    private final byte[] rootHash;

    public HTAuthenticator(byte[] rootHash) {
        this.rootHash = rootHash;
    }

    public byte[] getRootHash() {
        return rootHash;
    }

    public boolean equals(Authenticator other) {
        HTAuthenticator htOther;
        try {
            htOther = (HTAuthenticator) other;
        } catch (ClassCastException e) {
            return false;
        }

        return Arrays.equals(rootHash, htOther.getRootHash());
    }
}
