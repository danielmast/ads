package hashtree;

import static hashtree.Node.Position.LEFT;
import static hashtree.Node.Position.RIGHT;

/**
 * A node in the hash tree.
 */
public abstract class Node {
    enum Position { LEFT, RIGHT }

    protected byte[] hash;
    protected NonLeafNode parent;
    protected int level;

    public byte[] getHash() {
        return hash;
    }

    public NonLeafNode getParent() {
        return parent;
    }

    public int getLevel() {
        return level;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public void setParent(NonLeafNode parent) {
        this.parent = parent;
    }

    public Node getSibling() {
        if (parent == null)
            System.out.println("debug");

        if (this == parent.getLeft())
            return parent.getRight();
        return parent.getLeft();
    }

    public Position getPosition() {
        if (this == parent.getLeft())
            return LEFT;
        else
            return RIGHT;
    }
}


