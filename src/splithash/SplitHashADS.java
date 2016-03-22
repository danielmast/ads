package splithash;

import framework.ADS;
import framework.Block;
import utils.Utils;

import java.util.*;

import static splithash.Node.Position.LEFT;
import static splithash.SplitHashADS.AddPos.BEGIN;
import static splithash.SplitHashADS.AddPos.END;
import static splithash.SplitHashADS.Kind.*;

/**
 * The SplitHash ADS.
 */
public class SplitHashADS extends ADS<SplitHashADS, SHProof, SHAuthenticator> {
    enum Kind{mergeLeft, mergeRight, leftFringe, unknown, rightFringe}

    enum AddPos{BEGIN, END}

    // The height of the SplitHash is the largest sequence of parent-child relations, minus the leaves
    private int height;
    private List<List<Node>> leftFringes;
    private List<List<Node>> rightFringes;
    private List<Node> top;

    private List<LeafNode> leaves; // the leaf nodes of the SplitHash

    public SplitHashADS() {
        super();
        height = 0;
        leftFringes = new ArrayList<>();
        rightFringes = new ArrayList<>();
        top = new ArrayList<>();
        leaves = new ArrayList<>();
    }

    public SplitHashADS(LeafNode node) {
        this();
        top.add(node);
        leaves.add(node);
    }

    public SplitHashADS(List<Block> blocks) {
        this();

        if (blocks.size() == 0) {
            return;
        } else if (blocks.size() == 1) {
            LeafNode node = new LeafNode(blocks.get(0));
            top.add(node);
            leaves.add(node);
            return;
        }

        // else if blocks.size() > 1 ...

        leaves = createLeaves(blocks);

        int h = 0;

        // Cast LeafNodes to Nodes
        List<Node> center = new ArrayList<>();
        for (LeafNode leaf : leaves) {
            center.add(leaf);
        }

        while(center.size() > 0) {
            RoundResult roundResult = doRound(center, true, true, h+1);
            center = roundResult.getCenter();
            leftFringes.add(roundResult.getLeftFringe());
            rightFringes.add(roundResult.getRightFringe());
            h++;
        }

        h--;

        if (h < leftFringes.size()) {
            top.addAll(leftFringes.get(h));
            leftFringes.remove(h);
        }
        if (h < rightFringes.size()) {
            top.addAll(rightFringes.get(h));
            rightFringes.remove(h);
        }

        height = h;
    }

    @Override
    public SplitHashADS create(List<Block> blocks) {
        return new SplitHashADS(blocks);
    }

    private List<LeafNode> createLeaves(List<Block> blocks) {
        List<LeafNode> leaves = new ArrayList<>();

        for (Block block : blocks) {
            LeafNode node = new LeafNode(block);
            leaves.add(node);
        }

        return leaves;
    }

    public int getHeight() {
        return height;
    }

    private List<List<Node>> getLeftFringes() {
        return leftFringes;
    }

    private void addLeftFringe(Node node) {
        addLeftFringe(node, END);
    }

    private void addLeftFringe(Node node, AddPos pos) {
        node.setParentNull();
        int level = node.getLevel();

        while (level >= leftFringes.size()) {
            leftFringes.add(new ArrayList<Node>());
        }

        if (pos == END)
            leftFringes.get(level).add(node);
        else
            leftFringes.get(level).add(0, node);
    }

    private void addLeftFringes(List<Node> leftFringes) {
        for (Node node : leftFringes)
            addLeftFringe(node);
    }

    private List<List<Node>> getRightFringes() {
        return rightFringes;
    }

    private void addRightFringe(Node node) {
        addRightFringe(node, END);
    }

    private void addRightFringe(Node node, AddPos pos) {
        node.setParentNull();
        int level = node.getLevel();

        while (level >= rightFringes.size()) {
            rightFringes.add(new ArrayList<Node>());
        }

        if (pos == END)
            rightFringes.get(level).add(node);
        else
            rightFringes.get(level).add(0, node);
    }

    private void addRightFringes(List<Node> rightFringes) {
        for (Node node : rightFringes)
            addRightFringe(node);
    }

    private List<Node> getTop() {
        return top;
    }

    private void setTop(List<Node> top) {
        this.top = top;
    }

    public List<LeafNode> getLeaves() {
        return leaves;
    }

    private boolean isEmpty() {
        return height == 0 && top.size() == 0;
    }

    private static RoundResult doRound(List<Node> nodes, boolean volatileLeft, boolean volatileRight, int level) {
        int n = nodes.size();
        int left = 0;
        int right = n - 1;

        Map<Node, Kind> kinds = new HashMap<>(nodes.size());

        Node prev = null;
        for (Node node : nodes) {

            // Set the prev and next values in each level
            if (prev != null)
                prev.setNext(node);

            prev = node;

            kinds.put(node, unknown);
        }

        boolean done;
        int idx;
        for (idx = 0; ; idx++) {
            done = true;

            if (volatileLeft) {

                if (left < n && kinds.get(nodes.get(left)) == unknown && !nodes.get(left).getOutputBit(idx)) {
                    kinds.put(nodes.get(left), leftFringe);
                    left++;
                }

                if (left < n && kinds.get(nodes.get(left)) == unknown) {
                    done = false;
                }
            }

            if (volatileRight) {
                if (right >= 0 && kinds.get(nodes.get(right)) == unknown && nodes.get(right).getOutputBit(idx)) {
                    kinds.put(nodes.get(right), rightFringe);
                    right--;
                }

                if (right >= 0 && kinds.get(nodes.get(right)) == unknown) {
                    done = false;
                }
            }

            for (int j = 0; j < n-1; j++) {
                Node nodeJ = nodes.get(j);
                Node nodeJPlus = nodes.get(j+1);

                if (kinds.get(nodeJ) == unknown && kinds.get(nodeJPlus) == unknown) {
                    boolean sameHash = Arrays.equals(nodeJ.getHash(), nodeJPlus.getHash());

                    if (sameHash || nodeJ.getOutputBit(idx) && !nodeJPlus.getOutputBit(idx)) {
                        kinds.put(nodeJ, mergeLeft);
                        kinds.put(nodeJPlus, mergeRight);

                        nodeJ.setBitcount(idx);
                        nodeJPlus.setBitcount(idx);
                    } else {
                        done = false;
                    }
                }
            }

            if (done)
                break;
        }

        // we go over the node list, and decide which (unmerged) nodes should be merged
        RoundResult r = new RoundResult();

        for (int i = 0; i < n; i++) {
            Node parent;

            switch (kinds.get(nodes.get(i))) {
                case unknown: // nodes that could not merge with neighbours, but also aren't part of the fringes (orphan)
                    // Add a single parent in the new level
                    parent = new Node(nodes.get(i), level);
                    r.addCenter(parent);
                    break;
                case mergeLeft:
                    parent = new Node(nodes.get(i), nodes.get(i+1), level);

                    r.addCenter(parent);
                    i++; // to skip the right child
                    break;
                case leftFringe:
                    r.addLeftFringe(nodes.get(i));
                    break;
                case rightFringe:
                    r.addRightFringe(nodes.get(i));
            }
        }

        return r;
    }

    @Override
    public void setThis(SplitHashADS ADS) {
        height = ADS.height;
        leftFringes = ADS.leftFringes;
        rightFringes = ADS.rightFringes;
        top = ADS.top;
        leaves = ADS.leaves;
    }

    @Override
    public SplitHashADS merge(SplitHashADS left, SplitHashADS right) {
        if (left.isEmpty())
            return right;
        if (right.isEmpty())
            return left;

        SplitHashADS result = new SplitHashADS();

        ArrayList<Node> elems = new ArrayList<>();

        // Go through all levels, building up the height of the merged SplitHash
        while (true) {
            int h = result.getHeight(); // we need the height of merged a lot, so make a short variable

            boolean volatileLeft = h >= left.getHeight();
            boolean volatileRight = h >= right.getHeight();

            if (!volatileLeft) { // If merged is smaller
                // Add all left's rightFringes to the left of elems
                if (h < left.getRightFringes().size())
                    elems.addAll(0, left.getRightFringes().get(h));
            } else if (h == left.getHeight()) { // If merged is as tall as left
                elems.addAll(0, left.getTop()); // Add left's top nodes to the left of elems
            }

            // Check the same for right, but mirrored
            if (!volatileRight) {
                if (h < right.getLeftFringes().size())
                    elems.addAll(right.getLeftFringes().get(h));
            } else if (h == right.getHeight()) {
                elems.addAll(right.getTop());
            }

            // Continue until we have gone through all levels and all elems have been processed
            if (volatileLeft && volatileRight && elems.size() == 0) {
                break;
            }

            RoundResult roundResult = doRound(elems, volatileLeft, volatileRight, h+1);
            elems = roundResult.getCenter();

            if (volatileLeft) {
                // Add the left fringes of the round result
                result.addLeftFringes(roundResult.getLeftFringe());
            } else {
                // Add the left fringes of the left SplitHash
                if (h < left.getLeftFringes().size())
                    result.addLeftFringes(left.getLeftFringes().get(h));
            }

            if (volatileRight) {
                result.addRightFringes(roundResult.getRightFringe());
            } else {
                if (h < right.getRightFringes().size())
                    result.addRightFringes(right.getRightFringes().get(h));
            }

            result.height++;
        }

        result.height--; // Cancel the last height inc
        int h = result.getHeight();

        // Concatenate the left and right fringes and set them to the top
        // And remove the left and right fringes at the highest height (as they are now added to the top)
        result.setTop(new ArrayList<Node>());
        if (h < result.getLeftFringes().size()) {
            result.setTop(result.getLeftFringes().get(h));
            result.getLeftFringes().remove(h);
        }
        if (h < result.getRightFringes().size()) {
            result.getTop().addAll(result.getRightFringes().get(h));
            result.getRightFringes().remove(h);
        }

        result.leaves.addAll(left.leaves);
        result.leaves.addAll(right.leaves);

        int lSize = left.leaves.size();
        int rSize = right.leaves.size();
        if (lSize > 0 && rSize > 0)
            left.leaves.get(lSize-1).setNext(right.leaves.get(0));

        return result;
    }

    @Override
    public SplitHashADS[] split(SplitHashADS ADS, int index) {
        // Initialize left and right SplitHashADS
        SplitHashADS left = new SplitHashADS();
        SplitHashADS right = new SplitHashADS();

        // Check for cases in which split does not have to be performed
        if (index == 0) {
            return new SplitHashADS[]{left, ADS};
        } else if (index == ADS.leaves.size()) {
            return new SplitHashADS[]{ADS, right};
        }

        // Get the two leaf nodes directly left and right of the split
        Node leftNode = ADS.leaves.get(index-1);
        Node rightNode = ADS.leaves.get(index);

        Node node = leftNode;

        int idx = 0;
        while (node != null) {
            // If a node enters this loop, it means he has a parent (otherwise, idx < bitcount is never true, because bitcount = -1)
            while (node != null && idx < node.getBitcount()) {
                if (node.getOutputBit(idx)) {
                    if (node.getPrev() == null) // top is reached
                        break;
                    else {
                        left.addRightFringe(node, BEGIN);

                        node.setBitcount(-1);
                        idx++;
                        node = node.getPrev();
                    }
                } else {
                    idx++;
                }
            }

            /*
            If a node is not null and jumps out of the previous loop, a few things can hold:

            1. node.getPrev() = null, meaning that there is no node to the left of the current node with equal height
            In this case, we can add all following roots to the left fringe

            2. idx has become equal to node.getBitcount(). Now we have to check whether to get the node's parent
            and continue, or add the current node to the fringe. This depends on the node being a left or right child
            of its parent. Another possibility is that the node has a single parent.
            */

            if (node != null) {
                if (node.getPrev() == null) { // top is reached
                    while (node != null) {
                        left.addLeftFringe(node, BEGIN);

                        node.setBitcount(-1);

                        node = node.getPrevRoot();
                    }
                } else if (node.getParent() != null && node.getPosition() == Node.Position.RIGHT) {
                    node = node.getParent();
                    idx = 0;
                } else { // Node is not a right child of its parent
                    // Increase idx until node's output bit become true (becoming a right fringe)
                    while (!node.getOutputBit(idx))
                        idx++;

                    left.addRightFringe(node, BEGIN);
                    node.setBitcount(-1);
                    idx++;
                    node = node.getPrev();
                }
            }
        }

        node = leftNode;
        node.setNext(null);
        while (node != null) {
            if (node.getParent() != null) {
                node = node.getParent();
                node.setNext(null);
            } else if (node.getPrev() != null) {
                node = node.getPrev();
            } else {
                break;
            }
        }

        // Repeat the same procedure for the part at the right side of the split, with minor differences checking
        // for a node being a single or double parent
        node = rightNode;

        idx = 0;
        while (node != null) {
            // If a node enters this loop, it means he has a parent (otherwise, idx < bitcount is never true, because bitcount = -1)
            while (node != null && idx < node.getBitcount()) {
                if (!node.getOutputBit(idx)) {
                    if (node.getNext() == null) // top is reached
                        break;
                    else {
                        right.addLeftFringe(node);

                        node.setBitcount(-1);
                        idx++;

                        node = node.getNext();
                    }
                } else {
                    idx++;
                }
            }

            // If node is a root (has no parent), or the top has been reached
            if (node != null) {
                if (node.getNext() == null) { // top is reached
                    while (node != null) {
                        right.addRightFringe(node);

                        node.setBitcount(-1);

                        // Traverses the nodes to get the root of the tree, standing after the current node's tree.
                        node = node.getNextRoot();
                    }
                } else if (node.getParent() != null && node.getPosition() == Node.Position.LEFT && !node.getParent().isSingleParent()) {
                    node = node.getParent();
                    idx = 0;
                } else {
                    while (node.getOutputBit(idx))
                        idx++;

                    right.addLeftFringe(node);
                    node.setBitcount(-1);
                    idx++;
                    node = node.getNext();
                }
            }
        }

        // Set heights
        left.height = Math.max(left.leftFringes.size(), left.rightFringes.size()) - 1;
        right.height = Math.max(right.leftFringes.size(), right.rightFringes.size()) - 1;

        // Set top fringes to top, and remove from fringes
        left.top = new ArrayList<>();

        if (left.height + 1 == left.leftFringes.size()) {
            left.top.addAll(left.leftFringes.get(left.height));
            left.leftFringes.remove(left.height);
        }
        if (left.height + 1 == left.rightFringes.size()) {
            left.top.addAll(left.rightFringes.get(left.height));
            left.rightFringes.remove(left.height);
        }

        right.top = new ArrayList<>();

        if (right.height + 1 == right.leftFringes.size()) {
            right.top.addAll(right.leftFringes.get(right.height));
            right.leftFringes.remove(right.height);
        }
        if (right.height + 1 == right.rightFringes.size()) {
            right.top.addAll(right.rightFringes.get(right.height));
            right.rightFringes.remove(right.height);
        }

        // Distribute the leaves
        List<LeafNode> leftLeaves = new ArrayList<>(ADS.leaves.subList(0, index));
        left.leaves.addAll(leftLeaves);
        List<LeafNode> rightLeaves = new ArrayList<>(ADS.leaves.subList(index, ADS.leaves.size()));
        right.leaves.addAll(rightLeaves);

        return new SplitHashADS[]{left, right};
    }

    /**
     * Lists all roots from (all levels of) leftFringes, top, and (all levels of) rightFringes.
     * @return the list of combined roots
     */
    public List<Node> getAllRoots() {
        List<Node> roots = new ArrayList<>();

        for (List<Node> leftFringeLevel : leftFringes) {
            roots.addAll(leftFringeLevel);
        }

        roots.addAll(top);

        for (List<Node> rightFringeLevel : rightFringes) {
            roots.addAll(rightFringeLevel);
        }

        return roots;
    }

    @Override
    public SHProof getProof(int index) {
        return new SHProof(this, index);
    }

    /**
     * Returns the SplitHash authenticator, which is the root hash of the hash tree, created from the SplitHash
     * @return the SplitHash authenticator
     */
    @Override
    public SHAuthenticator getAuthenticator() {
        List<byte[]> roots = new ArrayList<>();

        for (Node root : getAllRoots()) {
            roots.add(root.getHash());
        }

        return new SHAuthenticator(roots);
    }

    @Override
    public boolean verify(Block block, SHProof proof, SHAuthenticator authenticator) {
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

        // If the computed root hash equals the check root hash, then this proves that the received block is correct.
        return authenticator.contains(rootHash);
    }
}
