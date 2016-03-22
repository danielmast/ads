package framework;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of integer data blocks, stored simply as an ArrayList
 */
public class IntData extends Data<IntData> {
    private final List<Block> blocks;

    public IntData() {
        blocks = new ArrayList<>();
    }

    public List<Block> getBlocks() {
        List<Block> result = new ArrayList<>();

        for (Block block : blocks) {
            result.add(block);
        }

        return result;
    }

    @Override
    public IntBlock getBlock(int index) {
        return (IntBlock)blocks.get(index);
    }

    @Override
    public void appendBlocks(List<Block> blocks) {
        this.blocks.addAll(blocks);
    }

    @Override
    public void insertBlocks(int index, List<Block> blocks) {
        this.blocks.addAll(index, blocks);
    }

    @Override
    public void deleteBlocks(int index, int length) {
        this.blocks.subList(index, index + length).clear();
    }

    @Override
    public int getBlockCount() {
        return blocks.size();
    }

    @Override
    public IntData copy() {
        IntData copy = new IntData();

        for (int i = 0; i < getBlockCount(); i++) {
            copy.addInt(((IntBlock)blocks.get(i)).getValue());
        }

        return copy;
    }

    /**
     * Creates a single IntBlock and adds it to the list
     * @param value the to-be-added integer
     */
    public void addInt(int value) {
        blocks.add(new IntBlock(value));
    }

    /**
     * Add an array of integer values to the data
     * @param values an array of ints that have to be added to this data object
     */
    public void addInts(int[] values) {
        for (int value : values) {
            blocks.add(new IntBlock(value));
        }
    }
}
