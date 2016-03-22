package tests;

import hashtree.HashTreeADS;
import framework.IntBlock;
import framework.IntData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IntHTDataTest {
    private IntData data;

    @Before
    public void initialize() {
        data = new IntData();
    }

    /**
     * Tests the addition of integer arrays to the data
     */
    @Test
    public void testAddInts() {
        int[] input = new int[]{4, 9, 3, 5};
        data.addInts(input);

        assertEquals(4, data.getBlockCount());

        IntBlock block = data.getBlock(2);
        assertEquals(3, block.getValue());

        input = new int[]{18, 3, 105, 87, 2938};
        data.addInts(input);

        assertEquals(4 + 5, data.getBlockCount());

        block = data.getBlock(6);
        assertEquals(105, block.getValue());
    }

    @Test
    public void testEncode() {
        HashTreeADS ADS = new HashTreeADS(data.getBlocks());

        assertNotNull(ADS);
    }
}
