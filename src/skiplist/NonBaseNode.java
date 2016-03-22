package skiplist;

import utils.Utils;

/**
 * A node in the skip list that is not a base node. Instead, it is located in the tower somewhere above a base node.
 */
public class NonBaseNode extends Node {
    private BaseNode base;
    protected Node down;

    public NonBaseNode(Node down) {
        this(down, NORMAL);
    }

    public NonBaseNode(Node down, int minMax) {
        super(minMax);

        setDown(down);

        if (down instanceof BaseNode)
            base = (BaseNode)down;
        else
            base = ((NonBaseNode)down).getBase();
    }

    public BaseNode getBase() {
        return base;
    }

    public Node getDown() {
        return down;
    }

    public void setDown(Node down) {
        this.down = down;
        down.setUp(this);
    }

    @Override
    public byte[] computeLabel(boolean all) {
        if (minMaxNormal == MAX)
            return base.getHash();

        byte[] downLabel;
        if (all) {
            downLabel = down.computeLabel(all);
        } else {
            downLabel = down.label;
        }

        if (right.isPlateau() && right.getMinMaxNormal() != MAX) {
            byte[] rightLabel;

            if (all) {
                rightLabel = right.computeLabel(all);
            } else {
                rightLabel = right.label;
            }

            label = Utils.hash(downLabel, rightLabel);
        } else // right is a tower node
            label = downLabel;

        return label;
    }

    @Override
    public String toString() {
        //return base.toString();
        String result = "" + base.getId();
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
