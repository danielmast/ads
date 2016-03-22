package hashtree;

import utils.Utils;

/**
 * A non leaf node has a left and right child. Right may be null, when the number of leaves is not a power of 2.
 */
public class NonLeafNode extends Node {
    private Node left, right;

    public NonLeafNode(Node left, Node right) {
        this.left = left;
        this.right = right;

        level = left.level + 1;

        left.setParent(this);

        if (right != null)
            right.setParent(this);

        resetHash();
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public void setLeft(Node left) {
        if (left == null && this.left != null)
            this.left.setParent(null);
        else
            left.setParent(this);

        this.left = left;
    }

    public void setRight(Node right) {
        if (right == null && this.right != null)
            this.right.setParent(null);
        else
            right.setParent(this);

        this.right = right;
    }

    public void resetHash() {
        if (right != null) {
            hash = Utils.hash(left.hash, right.hash);
        } else {
            hash = left.hash;
        }
    }
}
