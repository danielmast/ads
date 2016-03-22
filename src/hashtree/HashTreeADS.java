package hashtree;

import framework.ADS;
import framework.Block;
import hashtree.Node.Position;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hashtree.Node.Position.LEFT;

/**
 * Hash tree ADS. In the constructor, the ADS is built from the original data,
 * setting the leaves, and building the tree from bottom to top all the way to the root.
 */
public class HashTreeADS extends ADS<HashTreeADS, HTProof, HTAuthenticator> {
    private List<Node> leaves;
    private Node root;

    public HashTreeADS() { // used in split
    }

    public HashTreeADS(List<Block> blocks) {
        leaves = createLeaves(blocks);

        buildTree();
    }

    // usesLeaves is used to distinguish this constructor from HashTreeADS(List<Block)
    public HashTreeADS(List<Node> leaves, boolean usesLeaves) {
        this.leaves = leaves;

        buildTree();
    }

    @Override
    public HashTreeADS create(List<Block> blocks) {
        return new HashTreeADS(blocks);
    }

    /**
     * Creates a list of leaves from a list of blocks. Used by the constructor.
     * @param blocks list of blocks
     * @return list of leaves
     */
    private static List<Node> createLeaves(List<Block> blocks) {
        List<Node> leaves = new ArrayList<>();

        for (Block block : blocks) {
            LeafNode node = new LeafNode(block);
            leaves.add(node);
        }

        return leaves;
    }

    /**
     * Builds the tree from the leaves to the top, setting the root node and its children
     */
    private void buildTree() {
        // Create a 2D list, with the leaves in the 1st element, and an empty list in the 2nd.
        // This will be used for storing and referencing the nodes from bottom to top in the tree
        List<List<Node>> nodes = new ArrayList<>(2);
        nodes.add(leaves);
        nodes.add(new ArrayList<Node>());

        // Used for switching between the 2 lists of nodes. Every iteration, one list is emptied, and filled with the
        // parents of the nodes in the other list.
        int i = 0;

        NonLeafNode parent = null;

        // While we haven't reached the root yet
        while (nodes.get(i).size() >= 2) {
            // Iterate over the indices of the selected array
            for (int j = 0; j < nodes.get(i).size(); j+=2) {
                Node left = nodes.get(i).get(j); // Get the left child node

                Node right = null;
                if (nodes.get(i).size() != j+1) // If there still exists a right child
                    right = nodes.get(i).get(j + 1); // Get the right child node

                parent = new NonLeafNode(left, right); // Create the parent node
                nodes.get(1-i).add(parent); // Add the parent to the other list
            }

            nodes.set(i, new ArrayList<Node>()); // empty the first list
            i = 1 - i; // switch the nodes list index from 0 to 1 or vice versa
        }

        if (parent != null)
            root = parent; // The last node that was set to parent is the root
        else if (leaves.size() == 1) {
            // If parent was never set, and the leaves contain exactly one node, then that node is the root
            root = leaves.get(0);
        }
    }

    public List<Node> getLeaves() {
        return leaves;
    }

    public Node getRoot() {
        return root;
    }

    private void rebuildTree(int index, List<Node> nonChangingNodes) {
        // Create a 2D list, with the leaves in the 1st element, and an empty list in the 2nd.
        // This will be used for storing and referencing the nodes from bottom to top in the tree
        List<List<Node>> nodes = new ArrayList<>(2);

        List<Node> leaves = new ArrayList<>();

        int ncn_index = 0; // non changing nodes index
        if (nonChangingNodes.size() > 0 && nonChangingNodes.get(0).getLevel() == 0) {
            leaves.add(nonChangingNodes.get(0));
            ncn_index++;
        }

        // Add all leaves at the right of index
        leaves.addAll(new ArrayList<>(this.leaves.subList(index, this.leaves.size())));

        nodes.add(leaves);
        nodes.add(new ArrayList<Node>());

        // Used for switching between the 2 lists of nodes. Every iteration, one list is emptied, and filled with the
        // parents of the nodes in the other list.
        int i = 0;

        int level = 0;

        // While we haven't reached the root yet
        while (nodes.get(i).size() >= 2 || ncn_index < nonChangingNodes.size()) {
            // Iterate over the indices of the selected array
            for (int j = 0; j < nodes.get(i).size(); j+=2) {

                Node left = nodes.get(i).get(j); // Get the left child node

                Node right = null;
                if (nodes.get(i).size() != j+1) // If there still exists a right child
                    right = nodes.get(i).get(j + 1); // Get the right child node

                NonLeafNode parent = new NonLeafNode(left, right); // Create the parent node
                nodes.get(1-i).add(parent); // Add the parent to the other list
            }

            nodes.set(i, new ArrayList<Node>()); // empty the previous list
            i = 1 - i; // switch the nodes list index from 0 to 1 or vice versa
            level++;

            // Add the non changed node at the start when its level has reached
            if (ncn_index < nonChangingNodes.size() && level == nonChangingNodes.get(ncn_index).getLevel()) {
                nodes.get(i).add(0, nonChangingNodes.get(ncn_index));
                ncn_index++;
            }
        }

        root = nodes.get(i).get(0); // Get the single remaining node
    }

    private List<Node> getNonChangingNodesAppend() {
        List<Node> result = new ArrayList<>();

        if (leaves.size() == 0)
            return result;
        else if (leaves.size() == 1) {
            result.add(leaves.get(0));
            return result;
        }

        // else...

        Node node = leaves.get(leaves.size() - 1);

        boolean found = false;
        Node prev = null;
        while (node != null) {
            if (found) {
                if (prev == ((NonLeafNode)node).getRight()) {
                    result.add(((NonLeafNode)node).getLeft());
                }
            } else if (node.getParent() == null) {
                break;
            } else if (node.getPosition() == Position.LEFT) {
                result.add(node);
                found = true;
            }

            prev = node;
            node = node.getParent();
        }

        // Check if the tree is going to get a new root. This happens when the size before appending is a power of 2
        if (isPowerOfTwo(leaves.size()))
            result.add(root);

        return result;
    }

    private static boolean isPowerOfTwo(int value) {
        return ((value & -value) == value);
    }

    @Override
    public void setThis(HashTreeADS ADS) {
        this.leaves = ADS.leaves;
        this.root = ADS.root;
    }

    @Override
    public HashTreeADS merge(HashTreeADS left, HashTreeADS right) {
        if (left == null) {
            return right;
        } else if (right == null) {
            return left;
        }

        // else...

        List<Node> nonChangingNodes = left.getNonChangingNodesAppend();

        List<Node> rightLeaves = right.getLeaves();

        for (Node leaf : rightLeaves) // Remove tree traces from old structure
            leaf.setParent(null);

        int index = left.leaves.size();
        left.leaves.addAll(rightLeaves);

        left.rebuildTree(index, nonChangingNodes);

        return left;
    }

    @Override
    public HashTreeADS[] split(HashTreeADS ADS, int index) {
        if (index == 0) {
            return new HashTreeADS[]{null, ADS};
        } else if (index == ADS.leaves.size()) {
            return new HashTreeADS[]{ADS, null};
        }

        // else if 0 < index < leaves.size ...
        Node leftNode = ADS.leaves.get(index-1);
        Node rightNode = ADS.leaves.get(index);

        while (leftNode != rightNode) {
            leftNode = leftNode.getParent();
            rightNode = rightNode.getParent();
        }

        NonLeafNode node = (NonLeafNode) leftNode;
        node.setRight(null);
        node.resetHash();

        while (node.getParent() != null) {
            NonLeafNode parent = node.getParent();
            if (node.getPosition() == Position.LEFT && parent.getRight() != null) {
                parent.setRight(null);

            }
            parent.resetHash();
            node = parent;
        }

        // Set the new lowest root that all left leaves are under
        // Remember that node is now set at root, due to previous loop
        Node leftMost = ADS.leaves.get(0);
        Node rightMost = ADS.leaves.get(index-1);

        while (leftMost != rightMost) {
            leftMost = leftMost.getParent();
            rightMost = rightMost.getParent();
        }

        ADS.root = leftMost;

        if (ADS.root instanceof NonLeafNode)
            ((NonLeafNode)ADS.root).resetHash();

        ADS.root.setParent(null);

        List<Node> leftLeaves = new ArrayList<>(ADS.leaves.subList(0, index));
        List<Node> rightLeaves = new ArrayList<>(ADS.leaves.subList(index, ADS.leaves.size()));

        for (Node leaf : rightLeaves)
            leaf.setParent(null);

        HashTreeADS left = new HashTreeADS();
        left.leaves = leftLeaves;
        left.root = ADS.root;

        HashTreeADS right = new HashTreeADS(rightLeaves, true);

        return new HashTreeADS[]{left, right};
    }

    @Override
    public HTProof getProof(int index) {
        return new HTProof(this, index);
    }

    @Override
    public HTAuthenticator getAuthenticator() {
        return new HTAuthenticator(root.getHash());
    }

    @Override
    public boolean verify(Block block, HTProof proof, HTAuthenticator authenticator) {
        byte[] rootHash = block.hash();
        byte[] siblingHash;

        // For each hash in the sibling list
        for (int i = 0; i < proof.getSiblingHashes().size(); i++) {
            // Get the sibling hash
            siblingHash = proof.getSiblingHashes().get(i);

            // Check how the nodes are positioned relative to each other, to correctly compute the root hash.
            // Then, concatenate their hashes accordingly.
            if (proof.getSiblingPositions().get(i) == LEFT) {
                rootHash = Utils.hash(siblingHash, rootHash);
            } else {
                rootHash = Utils.hash(rootHash, siblingHash);
            }
        }

        if (!Arrays.equals(rootHash, authenticator.getRootHash()))
            System.out.println("debug");

        // If the computed root hash equals the check root hash, then this proves that the received block is correct.
        return Arrays.equals(rootHash, authenticator.getRootHash());
    }

    /**
     * Verifies whether a given node is the root
     * @param node the node we want to inspect
     * @return true if the node is the root, otherwise false
     */
    public boolean isRoot(Node node) {
        return node == root;
    }
}
