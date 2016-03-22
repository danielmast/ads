package skiplist;

import framework.Authenticator;

import java.util.Arrays;

/**
 * A skip list authenticator value
 */
public class SLAuthenticator extends Authenticator {
    private final byte[] hash;

    public SLAuthenticator(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean equals(Authenticator other) {
        SLAuthenticator slOther;
        try {
            slOther = (SLAuthenticator) other;
        } catch (ClassCastException e) {
            return false;
        }

        return Arrays.equals(hash, slOther.getHash());
    }
}
