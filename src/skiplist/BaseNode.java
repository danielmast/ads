package skiplist;

import utils.Utils;

import java.util.Random;

/**
 * A base node in the skip list, keeping the reference to the block, and keeping its index
 */
public class BaseNode extends Node {
    private byte[] hash;

    private int id;

    public BaseNode(byte[] hash) {
        super(NORMAL);
        this.hash = hash;

        id = new Random().nextInt(1000);
    }

    public BaseNode(int minMax) {
        super(minMax);

        if (minMaxNormal == Node.MIN)
            hash = Utils.hash(Utils.MININF);

        id = new Random().nextInt(10000);
    }

    public int getId() {
        return id;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    @Override
    public byte[] computeLabel(boolean all) {
        if (minMaxNormal == MAX)
            return hash;

        if (right.isPlateau() && right.getMinMaxNormal() != MAX) {
            byte[] rightLabel;
            if (all) {
                rightLabel = right.computeLabel(all);
            } else {
                rightLabel = right.label;
            }

            label = Utils.hash(hash, rightLabel);
        } else // right is a tower node or a +Inf node
            label = hash;

        return label;
    }

    @Override
    public String toString() {
        String result = "" + id;
        if (minMaxNormal == Node.MIN)
            result += "-Inf ";
        if (minMaxNormal == Node.MAX) {
            result += "+Inf";
            return result;
        }

        result += "[" + label[0] + ", " + label[1] + "]";
        return result;
    }
}
