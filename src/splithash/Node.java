package splithash;

import utils.Utils;

import java.util.Arrays;

import static splithash.Node.Position.LEFT;
import static splithash.Node.Position.RIGHT;

/**
 * A node in the tree of the SplitHash forest
 */
public class Node {
    enum Position { LEFT, RIGHT }

    private Node parent, left, right;

    private Node prev, next;

    protected byte[] hash;

    // the round number in which a merge led to this node (0 for leaves)
    // round count starts with 1, to equal the height of the SplitHash
    protected int level;

    // The number of output bits that were generated until this node merged.
    // When bitcount = -1, this means it has not (yet) merged with another node into a parent.
    protected int bitcount;

    public Node() {}

    public Node(Node left, Node right, int level) {
        this.left = left;
        this.right = right;

        left.parent = this;
        right.parent = this;

        hash = Utils.hash(left.getHash(), right.getHash());

        this.level = level;
        bitcount = -1;
    }

    /**
     * Constructor to create a single parent. Only the left child is set.
     * @param left The only child node
     * @param level The level of the new node
     */
    public Node(Node left, int level) {
        this.left = left;

        left.parent = this;

        hash = left.hash;

        this.level = level;
        bitcount = -1;
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

    public Node getPrev() {
        return prev;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        if (this.next != null) // if the old next was set
            this.next.prev = null; // then reset its prev

        if (next != null)
            next.prev = this;

        this.next = next;
    }

    public byte[] getHash() {
        return hash;
    }

    public int getLevel() {
        return level;
    }

    public int getBitcount() {
        return bitcount;
    }

    public void setBitcount(int bitcount) {
        this.bitcount = bitcount;
    }

    /**
     * Sets the parent to null. There is no method that can set the parent to another value, as this might result into
     * inconsistency issues. The parent should only be set when a new node is created.
     */
    public void setParentNull() {
        this.parent = null;
    }

    public Node getSibling() {
        if (this == parent.getLeft())
            return parent.getRight();
        return parent.getLeft();
    }

    public Node getRoot() {
        Node node = this;

        while (node.getParent() != null)
            node = node.getParent();

        return node;
    }

    public Position getPosition() {
        if (this == parent.getLeft())
            return LEFT;
        else
            return RIGHT;
    }

    public Node getPrevRoot() {
        Node node = this;

        // Traverses the nodes to get the root of the tree, standing before the current node's tree.
        while (node.getPrev() == null) {
            node = node.getLeft();

            if (node == null) // node was the leftmost leaf
                break;
        }

        if (node == null)
            return null;

        node = node.getPrev();

        return node.getRoot();
    }

    public Node getNextRoot() {
        Node node = this;

        // Traverses the nodes to get the root of the tree, standing after the current node's tree.
        while (node.getNext() == null) {
            if (node.isSingleParent())
                node = node.getLeft();
            else
                node = node.getRight();

            if (node == null) // node was the rightmost leaf
                break;
        }

        if (node == null)
            return null;

        node = node.getNext();

        return node.getRoot();
    }

    /**
     * Returns whether the node is a single parent.
     * @return true if single parent, false if leaf or non-single parent
     */
    public boolean isSingleParent() {
        return left != null && right == null;
    }

    /**
     * Returns a boolean that is constructed by combining the hash and the given index,
     * using it as a seed for a pseudo-random generator.
     * @param idx index of the output bit round
     * @return a boolean, representing the output bit
     */
    public boolean getOutputBit(int idx) {
        if (idx/8 == 20)
            System.out.println("debug");

        int b = hash[idx/8];
        int result = (b >> (idx%8)) & 1;

        return result == 1;
    }

    /**
     * Compares all fields of two nodes for equality. For referred nodes, it checks whether the hashes match.
     * @param obj the other object/node
     * @return True if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node))
            return false;

        Node other = (Node) obj;

        if (level != other.level) return false;

        if (bitcount != other.bitcount) return false;

        if (!Arrays.equals(hash, other.getHash())) return false;

        if (parent != null) {
            if (other.parent == null)
                return false;
            else
                if (!Arrays.equals(parent.hash, other.parent.hash)) return false;
        }

        if (left != null) {
            if (other.left == null)
                return false;
            else
            if (!Arrays.equals(left.hash, other.left.hash)) return false;
        }

        if (right != null) {
            if (other.right == null)
                return false;
            else
            if (!Arrays.equals(right.hash, other.right.hash)) return false;
        }

        return true;
    }
}
