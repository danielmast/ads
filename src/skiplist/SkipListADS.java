package skiplist;

import framework.ADS;
import framework.Block;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * (Authenticated) skip list ADS. Builds up the actual skip list.
 */
public class SkipListADS extends ADS<SkipListADS, SLProof, SLAuthenticator> {
    private Node startNode;
    private Node topRightNode;
    private List<BaseNode> baseNodes;

    public SkipListADS(List<Block> blocks) {
        startNode = new BaseNode(Node.MIN);

        baseNodes = new ArrayList<>();
        baseNodes.add((BaseNode)startNode);

        List<Node> rightMosts = new ArrayList<>();
        rightMosts.add(startNode);

        for (Block block : blocks) {
            byte[] hash = block.hash();

            Node node;
            Node down = null;

            int idx = 0;
            boolean coin = true;

            while (coin) {
                if (idx == 0) {
                    node = new BaseNode(hash);
                    baseNodes.add((BaseNode)node);
                } else {
                    node = new NonBaseNode(down);
                }

                if (idx < rightMosts.size()) {
                    rightMosts.get(idx).setRight(node);
                    rightMosts.set(idx, node);
                } else {
                    startNode = new NonBaseNode(startNode, Node.MIN);
                    startNode.setRight(node);
                    rightMosts.add(node);
                }

                down = node;

                coin = getOutputBit(hash, idx);
                idx++;
            }
        }

        for (int i = 0; i < rightMosts.size(); i++) {
            Node rightMost = rightMosts.get(i);

            if (i == 0) {
                topRightNode = new BaseNode(Node.MAX);
                baseNodes.add((BaseNode)topRightNode);
            } else {
                topRightNode = new NonBaseNode(topRightNode, Node.MAX);
            }

            rightMost.setRight(topRightNode);
        }

        refreshAllLabels();
    }

    public SkipListADS(Node startNode, Node topRightNode, List<BaseNode> baseNodes) {
        this.startNode = startNode;
        this.topRightNode = topRightNode;
        this.baseNodes = baseNodes;
    }

    public Node getStartNode() {
        return startNode;
    }

    public List<BaseNode> getBaseNodes() {
        return baseNodes;
    }

    @Override
    public SkipListADS create(List<Block> blocks) {
        return new SkipListADS(blocks);
    }

    @Override
    public void setThis(SkipListADS ADS) {
        startNode = ADS.startNode;
        topRightNode = ADS.topRightNode;
        baseNodes = ADS.baseNodes;
    }

    @Override
    public SkipListADS merge(SkipListADS left, SkipListADS right) {
        if (left == null)
            return right;
        if (right == null)
             return left;

        Node leftMaxInf = left.baseNodes.get(left.baseNodes.size()-1);
        Node rightMinInf = right.baseNodes.get(0);

        // Connect the node left of left.MaxInf and right of right.MinInf
        leftMaxInf.getLeft().setRight(rightMinInf.getRight());
        computeLabelWhileRightIsPlateau(leftMaxInf.getLeft());

        Node leftMinInf, rightMaxInf;
        while (true) {
            if (!leftMaxInf.isPlateau() && rightMinInf.isPlateau()) {
                leftMaxInf = leftMaxInf.getUp();

                // Increase size of right's +Inf tower
                rightMaxInf = new NonBaseNode(right.topRightNode, Node.MAX);
                right.topRightNode = rightMaxInf;

                leftMaxInf.getLeft().setRight(rightMaxInf);
                computeLabelWhileRightIsPlateau(leftMaxInf.getLeft());
            } else if (leftMaxInf.isPlateau() && !rightMinInf.isPlateau()) {
                leftMinInf = new NonBaseNode(left.startNode, Node.MIN);
                left.startNode = leftMinInf;

                rightMinInf = rightMinInf.getUp();

                leftMinInf.setRight(rightMinInf.getRight());
                computeLabelWhileRightIsPlateau(leftMinInf);
            } else if (leftMaxInf.isPlateau() && rightMinInf.isPlateau()) { // Inf sentinels are equally sized
                break;
            } else { // tops aren't reached yet, so simply connect nodes next to Inf sentinels
                leftMaxInf = leftMaxInf.getUp();
                rightMinInf = rightMinInf.getUp();

                leftMaxInf.getLeft().setRight(rightMinInf.getRight());
                computeLabelWhileRightIsPlateau(leftMaxInf.getLeft());
            }
        }

        List<BaseNode> baseNodes = new ArrayList<>(left.baseNodes.subList(0, left.baseNodes.size()-1));
        baseNodes.addAll(new ArrayList<>(right.baseNodes.subList(1, right.baseNodes.size())));

        return new SkipListADS(left.startNode, right.topRightNode, baseNodes);
    }

    private void computeLabelWhileRightIsPlateau(Node node) {
        node.computeLabel();
        node = node.getLeft();

        while (node != null && node.getRight().isPlateau()) {
            node.computeLabel();
            node = node.getLeft();
        }
    }

    @Override
    public SkipListADS[] split(SkipListADS ADS, int index) {

        if (index == 0) {
            return new SkipListADS[]{null, ADS};
        } else if (index == ADS.baseNodes.size()-2) {
            return new SkipListADS[]{ADS, null};
        }

        // else if 0 < index < leaves.size ...

        // +1 because of -Inf node; -1 because we want to get left neighbour of block at index
        Node node = ADS.baseNodes.get(index+1-1);

        // Construct -Inf tower of right ADS, and +Inf tower of left ADS
        BaseNode leftMaxInfBase = new BaseNode(Node.MAX);
        BaseNode rightMinInfBase = new BaseNode(Node.MIN);

        rightMinInfBase.setRight(node.getRight());
        node.setRight(leftMaxInfBase);

        node.computeLabel();

        Node leftMaxInf = leftMaxInfBase;
        Node rightMinInf = rightMinInfBase;

        leftMaxInf.computeLabel();
        rightMinInf.computeLabel();

        Node leftStartNode = ADS.baseNodes.get(0);
        Node rightTopRightNode = ADS.baseNodes.get(ADS.baseNodes.size()-1);

        boolean done = false;
        boolean leftHeightReached = false;
        while (true) {
            // Get the next node that is adjacent to the split, and recompute labels of intermediary nodes on path
            while (node.isPlateau()) {
                if (node.getLeft() != null) {
                    node = node.getLeft();
                    node.computeLabel();

                    if (node.getMinMaxNormal() == Node.MIN) { // -Inf node
                        leftHeightReached = true;
                    }
                } else { // node == ADS.startNode
                    done = true;
                    break;
                }
            }

            if (done)
                break;

            node = node.getUp();

            if (node.getRight().getMinMaxNormal() != Node.MAX) { // if node.right is not a +Inf node
                rightMinInf = new NonBaseNode(rightMinInf, Node.MIN);
                rightMinInf.setRight(node.getRight());
                rightMinInf.computeLabel();

                rightTopRightNode = rightTopRightNode.getUp();
            } else {
                if (node.getMinMaxNormal() == Node.MIN)
                    break;
            }

            if (!leftHeightReached) {
                leftMaxInf = new NonBaseNode(leftMaxInf, Node.MAX);
                node.setRight(leftMaxInf);

                node.computeLabel();

                leftStartNode = leftStartNode.getUp();
            }
        }

        List<BaseNode> leftBaseNodes = new ArrayList<>(ADS.baseNodes.subList(0, index+1));
        leftBaseNodes.add(leftMaxInfBase);

        List<BaseNode> rightBaseNodes = new ArrayList<>(ADS.baseNodes.subList(index+1, ADS.baseNodes.size()));
        rightBaseNodes.add(0, rightMinInfBase);

        leftStartNode.setUp(null);
        rightTopRightNode.setUp(null);

        SkipListADS left = new SkipListADS(leftStartNode, leftMaxInf, leftBaseNodes);
        SkipListADS right = new SkipListADS(rightMinInf, rightTopRightNode, rightBaseNodes);

        return new SkipListADS[]{left, right};
    }

    @Override
    public SLProof getProof(int index) {
        return new SLProof(this, index);
    }

    /**
     * Returns a pseudorandom boolean from a hash and a given index value.
     * Is used for the coin tossing that determines how many levels are created for a given block.
     * @param hash The block's hash
     * @param idx The number of coin tosses that have been done
     * @return A pseudorandom boolean
     */
    private boolean getOutputBit(byte[] hash, int idx) {
        int b = hash[idx/8];
        int result = (b >> (idx%8)) & 1;

        return result == 1;
    }

    @Override
    public SLAuthenticator getAuthenticator() {
        return new SLAuthenticator(startNode.getLabel());
    }

    @Override
    public boolean verify(Block block, SLProof proof, SLAuthenticator authenticator) {
        // Set the initial rootHash to the hash of the received block
        byte[] rootHash = block.hash();

        List<byte[]> Q = proof.getQ();
        List<Boolean> pos = proof.getPos();

        // Update the hash for each value in Q
        for (int i = 0 ; i < proof.getQ().size(); i++) {
            if (pos.get(i)) {
                rootHash = Utils.hash(rootHash, Q.get(i));
            } else {
                rootHash = Utils.hash(Q.get(i), rootHash);
            }
        }

        // Check if the result equals the authenticator
        return Arrays.equals(rootHash, authenticator.getHash());
    }

    /**
     * Makes sure that all labels are correct for the current data. It's up to the programmer when to do this:
     * either after every update operation, or when the authenticator is asked for.
     */
    public void refreshAllLabels() {
        startNode.computeLabel(true);
    }
}
