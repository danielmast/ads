package framework;

import utils.Utils;

/**
 * A hashable data block.
 */
public abstract class Block {
    /**
     * Returns the hash of the block, by hashing its unique string representation
     * @return the hash of the block
     */
    public byte[] hash() {
        return Utils.hash(toString());
    }

    /**
     * This method should be implemented, as it is used in the hash method
     * @return The unique string representation of the block
     */
    public abstract String toString();
}
