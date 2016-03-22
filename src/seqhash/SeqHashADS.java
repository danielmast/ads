package seqhash;

import framework.ADS;
import framework.Block;
import utils.Utils;

import java.util.*;

import static seqhash.Node.Position.LEFT;
import static seqhash.SeqHashADS.AddPos.END;
import static seqhash.SeqHashADS.Kind.*;

/**
 * The SeqhHash ADS. Maintains the SeqHash itself, and keeps a copy of the input data.
 */
public class SeqHashADS extends ADS<SeqHashADS, SHProof, SHAuthenticator> {

    enum Kind{mergeLeft, mergeRight, leftFringe, unknown, rightFringe}

    enum AddPos{BEGIN, END}

    // The height of the SeqHash is the largest sequence of parent-child relations, minus the leaves
    private int height;
    private List<List<Node>> leftFringes;
    private List<List<Node>> rightFringes;
    private List<Node> top;

    private List<LeafNode> leaves; // the leaf nodes of the SeqHash

    public SeqHashADS() {
        height = 0;
        leftFringes = new ArrayList<>();
        rightFringes = new ArrayList<>();
        top = new ArrayList<>();
        leaves = new ArrayList<>();
    }

    public SeqHashADS(LeafNode node) {
        this();
        top.add(node);
        leaves.add(node);
    }

    public SeqHashADS(List<Block> blocks) {
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

        List<LeafNode> leaves = createLeaves(blocks);

        initFromLeaves(leaves);
    }

    private void initFromLeaves(List<LeafNode> leaves) {
        if (leaves.size() == 0)
            return;

        this.leaves = leaves;

        int h = 0;

        // Cast LeafNodes to Nodes
        List<Node> center = new ArrayList<>();
        for (LeafNode leaf : leaves) {
            leaf.setParent(null);
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
    public SeqHashADS create(List<Block> blocks) {
        return new SeqHashADS(blocks);
    }

    private static List<LeafNode> createLeaves(List<Block> blocks) {
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

        for (Node node : nodes) {
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
                    if (sameHash || (nodeJ.getOutputBit(idx) && !nodeJPlus.getOutputBit(idx))) {
                        kinds.put(nodeJ, mergeLeft);
                        kinds.put(nodeJPlus, mergeRight);
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
    public void setThis(SeqHashADS ADS) {
        height = ADS.height;
        leftFringes = ADS.leftFringes;
        rightFringes = ADS.rightFringes;
        top = ADS.top;
        leaves = ADS.leaves;
    }

    @Override
    public SeqHashADS merge(SeqHashADS left, SeqHashADS right) {
        if (left.isEmpty())
            return right;
        if (right.isEmpty())
            return left;

        SeqHashADS result = new SeqHashADS();

        ArrayList<Node> elems = new ArrayList<>();

        // Go through all levels, building up the height of the merged SeqHash
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
                // Add the left fringes of the left SeqHash
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

        return result;
    }

    @Override
    public SeqHashADS[] split(SeqHashADS ADS, int index) {
        List<LeafNode> leftLeaves = new ArrayList<>(ADS.leaves.subList(0, index));
        List<LeafNode> rightLeaves = new ArrayList<>(ADS.leaves.subList(index, ADS.leaves.size()));

        SeqHashADS left = new SeqHashADS();
        left.initFromLeaves(leftLeaves);

        SeqHashADS right = new SeqHashADS();
        right.initFromLeaves(rightLeaves);

        return new SeqHashADS[]{left, right};
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

    /**
     * Combines the left and right fringes and the top of the SeqHash into a binary hash tree, and returns the top node.
     * @return The root node of the created hash tree
     */
    public Node finish() {
        if (isEmpty())
            return null;

        List<Node> left = new ArrayList<>();
        List<Node> right = new ArrayList<>();

        int level = 1;

        for (int i = 0; i < getHeight(); i++) {
            if (i < leftFringes.size())
                left.addAll(getLeftFringes().get(i));
            if (i < rightFringes.size())
                right.addAll(0, getRightFringes().get(i));

            left = doRound(left, false, false, level).getCenter();
            right = doRound(right, false, false, level).getCenter();

            level++;
        }

        List<Node> elems = new ArrayList<>();
        elems.addAll(left);
        elems.addAll(getTop());
        elems.addAll(right);

        while (elems.size() > 1) {
            elems = doRound(elems, false, false, level).getCenter();
            level++;
        }

        return elems.get(0);
    }

    @Override
    public SHProof getProof(int index) {
        return new SHProof(this, index);
    }

    /**
     * Returns the SeqHash authenticator, which is the root hash of the hash tree, created from the SeqHash
     * @return the SeqHash authenticator
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

        if (!authenticator.contains(rootHash))
            System.out.println("debug");

        // If the computed root hash equals the check root hash, then this proves that the received block is correct.
        return authenticator.contains(rootHash);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = true;

        if (!(obj instanceof SeqHashADS)) {
            System.out.println("Other object is no instance of SeqHash");
            return false;
        }

        SeqHashADS other = (SeqHashADS) obj;

        boolean leavesSize = true;
        if (!(leaves.size() == other.leaves.size())) {
            System.out.println("Different leaves sizes");
            result = false;
            leavesSize = false;
        }

        if (leavesSize) {
            for (int i = 0; i < leaves.size(); i++) {
                if (!leaves.get(i).equals(other.leaves.get(i))) {
                    System.out.println("Leaves #" + i + " are different");
                    result = false;
                }
            }
        }

        if (!(height == other.height)) {
            System.out.println("Different heights");
            result = false;
        }

        boolean topSize = true;
        if (!(top.size() == other.top.size())) {
            System.out.println("Different top sizes");
            result = false;
            topSize = false;
        }

        if (topSize) {
            for (int i = 0; i < top.size(); i++) {
                if (!top.get(i).equals(other.top.get(i))) {
                    System.out.println("Top nodes #" + i + " are different");
                    result = false;
                }
            }
        }

        List<List<Node>> maxList, minList;
        if (leftFringes.size() >= other.leftFringes.size()) {
            maxList = leftFringes;
            minList = other.leftFringes;
        } else {
            maxList = other.leftFringes;
            minList = leftFringes;
        }

        for (int i = 0; i < minList.size(); i++) {
            int size = leftFringes.get(i).size();

            if (size == other.leftFringes.get(i).size()) {
                for (int j = 0; j < size; j++) {
                    if (!leftFringes.get(i).get(j).equals(other.leftFringes.get(i).get(j))) {
                        System.out.println("leftFringe nodes #" + i + "." + j + " are different");
                        result = false;
                    }
                }
            } else  {
                System.out.println("leftFringes level " + i + " have different sizes");
                result = false;
            }
        }

        for (int i = minList.size(); i < maxList.size(); i++) {
            // This means that the biggest list has entries in a level that the smallest list does not have, so not equal
            if (maxList.get(i).size() > 0) {
                System.out.println("Larger leftFringe list contains more levels with nodes");
                result = false;
            }
        }


        if (rightFringes.size() >= other.rightFringes.size()) {
            maxList = rightFringes;
            minList = other.rightFringes;
        } else {
            maxList = other.rightFringes;
            minList = rightFringes;
        }

        for (int i = 0; i < minList.size(); i++) {
            int size = rightFringes.get(i).size();

            if (size == other.rightFringes.get(i).size()) {
                for (int j = 0; j < size; j++) {
                    if (!rightFringes.get(i).get(j).equals(other.rightFringes.get(i).get(j))) {
                        System.out.println("rightFringe nodes #" + i + "." + j + " are different");
                        result = false;
                    }
                }
            } else  {
                System.out.println("rightFringes level " + i + " have different sizes");
                result = false;
            }
        }

        for (int i = minList.size(); i < maxList.size(); i++) {
            // This means that the biggest list has entries in a level that the smallest list does not have, so not equal
            if (maxList.get(i).size() > 0) {
                System.out.println("Larger rightFringe list contains more levels with nodes");
                result = false;
            }
        }

        if (!result)
            System.out.println("debug");

        return result;
    }
}
