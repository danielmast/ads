package skiplist;

/**
 * A node in the skip list. Keeps a reference to the block it belongs to.
 */
public abstract class Node {
    protected Node up, left, right;
    protected byte[] label;

    // Notes whether this node contains a -Inf, +Inf, or normal element
    protected int minMaxNormal;

    public static final int MIN = -1;
    public static final int NORMAL = 0;
    public static final int MAX = 1;

    protected Node(int minMax) {
        if (!(minMax == MIN || minMax == NORMAL || minMax == MAX))
            throw new IllegalArgumentException("Argument minMax should be Node.MIN, Node.NORMAL or Node.MAX");

        minMaxNormal = minMax;
    }

    public Node getUp() {
        return up;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public boolean isPlateau() {
        return up == null;
    }

    public byte[] getLabel() {
        return label;
    }

    public int getMinMaxNormal() {
        return minMaxNormal;
    }

    /** Sets the up node for this node.
     *  Note: Do not call this method manually. Only setDown() from the other node should be called,
     *  so that up and down nodes always refer to each other.
     * @param up the up node
     */
    protected void setUp(Node up) {
        this.up = up;

        if (up != null)
            ((NonBaseNode) up).down = this;
    }

    /** Sets the left node for this node.
     *  Note: Do not call this method manually. Only setRight() from the other node should be called,
     *  so that left and right nodes always refer to each other.
     * @param left the left node
     */
    public void setLeft(Node left) {
        this.left = left;
        left.right = this;
    }

    public void setRight(Node right) {
        this.right = right;
        right.left = this;
    }

    /**
     * Recursively recomputes all labels right and below of this node if all = true.
     * Otherwise only recomputes this node's label
     * @param all whether to recompute all recursively
     * @return new label
     */
    public abstract byte[] computeLabel(boolean all);

    public byte[] computeLabel() {
        return computeLabel(false);
    }
}
