package seqhash;

import framework.Block;
import utils.Utils;

import java.util.Arrays;

import static seqhash.Node.Position.LEFT;
import static seqhash.Node.Position.RIGHT;

/**
 * A node in the tree of the SeqHash forest
 */
public class Node {
    enum Position { LEFT, RIGHT }

    private Node parent, left, right;

    private final byte[] hash;

    private int min, max; // the index of the leftmost and rightmost child that span this node's tree
    // the round number in which a merge led to this node (0 for leaves)
    // round count starts with 1, to equal the height of the SeqHash
    private int level;

    public Node(Block block) {
        hash = block.hash();
        level = 0;
    }

    public Node(Node left, Node right, int level) {
        this.left = left;
        this.right = right;

        left.setParent(this);
        right.setParent(this);

        hash = Utils.hash(left.getHash(), right.getHash());

        min = left.min;
        max = right.max;

        this.level = level;
    }

    /**
     * Constructor to create a single parent. Only the left child is set.
     * @param left The only child node
     * @param level The level of the new node
     */
    public Node(Node left, int level) {
        this.left = left;

        left.setParent(this);

        hash = left.hash;

        min = left.min;
        max = left.max;

        this.level = level;
    }

    public Node getParent() {
        return parent;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public byte[] getHash() {
        return hash;
    }

    public int getLevel() {
        return level;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getSibling() {
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

    /**
     * Returns a boolean that is constructed by combining the hash and the given index,
     * using it as a seed for a pseudo-random generator.
     * @param idx index of the output bit round
     * @return a boolean, representing the output bit
     */
    public boolean getOutputBit(int idx) {
        int b = hash[idx/8];
        int result = (b >> (idx%8)) & 1;

        return result == 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node))
            return false;

        Node other = (Node) obj;

        return Arrays.equals(hash, other.getHash());
    }
}
