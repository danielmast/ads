package splithash;

import framework.Block;

import java.util.Arrays;

/**
 * A LeafNode contains a reference to the next LeafNode in the leaves list.
 */
public class LeafNode extends Node {
    public LeafNode(Block block) {
        super();

        hash = block.hash();
        level = 0;
        bitcount = -1;
    }
}
