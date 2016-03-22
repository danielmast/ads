package framework;

/**
 * A data block, containing only an integer
 */
public class IntBlock extends Block {
    private final int value;

    public IntBlock(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Uniquely represents the block with its index and value
     * @return a string representation of this integer block
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
