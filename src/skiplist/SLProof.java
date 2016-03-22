package skiplist;

import framework.Proof;

import java.util.ArrayList;
import java.util.List;

/**
 * A skip list response object
 */
public class SLProof extends Proof<SkipListADS> {

    private List<byte[]> Q;
    private List<Boolean> pos;

    public SLProof(SkipListADS ADS, int index) {
        super(ADS, index);

        Node current = ADS.getBaseNodes().get(index+1);

        Q = new ArrayList<>();
        pos = new ArrayList<>();

        Node w = current.getRight();

        if (w.isPlateau() && w.minMaxNormal != Node.MAX) {
            Q.add(w.getLabel());
            pos.add(true);
        }

        while(true) {
            if (current.getUp() != null) { // climb up the tower
                current = current.getUp();

                w = current.getRight();

                if (w.isPlateau() && w.minMaxNormal != Node.MAX) {
                    Q.add(w.getLabel());
                    pos.add(true);
                }
            } else if (current.getLeft() != null) { // reached the top of the tower, so go to the left
                current = current.getLeft();

                if (current instanceof BaseNode) {
                    BaseNode baseCurrent = (BaseNode) current;

                    Q.add(baseCurrent.getHash());
                    pos.add(false);
                } else {
                    NonBaseNode nonBaseCurrent = (NonBaseNode) current;

                    Q.add(nonBaseCurrent.getDown().getLabel());
                    pos.add(false);
                }
            } else { // start node has been reached (has no up or left node)
                break;
            }
        }
    }

    public List<byte[]> getQ() {
        return Q;
    }

    public List<Boolean> getPos() {
        return pos;
    }
}
