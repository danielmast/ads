package seqhash;

import framework.Authenticator;

import java.util.Arrays;
import java.util.List;

/**
 * A SeqHash authenticator value
 */
public class SHAuthenticator extends Authenticator {
    private final List<byte[]> roots;

    public SHAuthenticator(List<byte[]> roots) {
        this.roots = roots;
    }

    public List<byte[]> getRoots() {
        return roots;
    }

    public boolean contains(byte[] otherRoot) {
        for (byte[] root : roots) {
            if (Arrays.equals(root, otherRoot))
                return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SHAuthenticator))
            return false;

        SHAuthenticator shOther = (SHAuthenticator) other;

        if (roots.size() != shOther.roots.size())
            return false;

        for (int i = 0; i < roots.size(); i++) {
            if (!Arrays.equals(roots.get(i), shOther.roots.get(i)))
                return false;
        }

        return true;
    }
}
