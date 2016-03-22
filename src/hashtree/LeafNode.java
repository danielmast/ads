package hashtree;

import framework.Block;

/**
 * A leaf node keeps a reference to its block. This is convenient for rehashing.
 */
public class LeafNode extends Node {
    public LeafNode(Block block) {
        hash = block.hash();
        level = 0;
    }
}
