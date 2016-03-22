package hashtree;

import framework.Proof;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hashtree.Node.Position.LEFT;

/**
 * A hash tree response object
 */
public class HTProof extends Proof<HashTreeADS> {

    // Response proof
    private List<byte[]> siblingHashes;
    private List<Node.Position> siblingPositions;

    public HTProof(HashTreeADS ADS, int index) {
        super(ADS, index);

        siblingHashes = new ArrayList<>();
        siblingPositions = new ArrayList<>();

        Node node = ADS.getLeaves().get(index);
        Node sibling;

        while(!ADS.isRoot(node)) {
            sibling = node.getSibling();

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
