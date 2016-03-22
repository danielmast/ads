package seqhash;

import framework.Block;

/**
 * A LeafNode contains a reference to the next LeafNode in the leaves list.
 */
public class LeafNode extends Node {
    public LeafNode(Block block) {
        super(block);
    }
}
