package splithash;

import framework.Proof;

import java.util.ArrayList;
import java.util.List;

/**
 * A SplitHash response object
 */
public class SHProof extends Proof<SplitHashADS> {

    // Response proof
    private List<byte[]> siblingHashes;
    private List<Node.Position> siblingPositions;

    public SHProof(SplitHashADS ADS, int index) {
        super(ADS, index);

        siblingHashes = new ArrayList<>();
        siblingPositions = new ArrayList<>();

        Node node = ADS.getLeaves().get(index);
        Node sibling;

        while(node.getParent() != null) {
            sibling = node.getSibling();

            // Skip single parents
            if (sibling != null) {
                siblingHashes.add(sibling.getHash());
                siblingPositions.add(sibling.getPosition());
            }

            node = node.getParent();
        }
    }

    public List<byte[]> getSiblingHashes() {
        return siblingHashes;
    }

    public List<Node.Position> getSiblingPositions() {
        return siblingPositions;
    }
}
