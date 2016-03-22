package framework;

import java.util.List;

/**
 * The original framework that the writer writes to, and the reader queries from.
 */
public abstract class Data<D extends Data> {
    /**
     * Obtains one block from the data
     * @param index the index where the block is located
     * @return the block at the specified index
     */
    public abstract Block getBlock(int index);

    /**
     * Appends a list of blocks at the end of the data
     * @param blocks the list of blocks
     */
    public abstract void appendBlocks(List<Block> blocks);

    /**
     * Inserts a list of blocks at the given index.
     * @param index the index at which the blocks should be added
     */
    public abstract void insertBlocks(int index, List<Block> blocks);

    /**
     * Deletes a number of blocks currently located at range [index, index+length]
     * @param index the index of the first to-be-deleted block
     */
    public abstract void deleteBlocks(int index, int length);

    /**
     * Returns the number of blocks in the data
     * @return the number of blocks in the data
     */
    public abstract int getBlockCount();

    public abstract D copy();
}
