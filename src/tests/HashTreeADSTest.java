package tests;

import framework.Block;
import framework.IntBlock;
import framework.IntData;
import hashtree.HTProof;
import hashtree.HashTreeADS;
import hashtree.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class HashTreeADSTest {
    private IntData data;
    private HashTreeADS ADS;

    @Before
    public void initialize() {
        data = new IntData();
        data.addInts(new int[]{289, 27, 295, 28, 2, 9358, 288, 57, 928, 673, 1000, 2832987, 473});
        ADS = new HashTreeADS(data.getBlocks());
    }

    @Test
    public void testGetAuthenticator() {
        assertNotNull(ADS.getAuthenticator());
    }

    @Test
    public void testGetLeaves() {
        assertEquals(13, ADS.getLeaves().size());
    }

    @Test
    public void testGetRoot() {
        Node root = ADS.getRoot();
        assertNotNull(root);
    }

    @Test
    public void testIsRoot() {
        Node root = ADS.getRoot();
        assertTrue(ADS.isRoot(root));
    }

    /**
     * Tests the appendBlock operation. Appends a block using the append operation, and verifies if the result is
     * the same as when the block was already added before.
     */
    @Test
    public void testAppendBlocks() {
        int value1 = 43, value2 = 129, value3 = 567;
        List<Block> blocks = new ArrayList<>();
        blocks.add(new IntBlock(value1));
        blocks.add(new IntBlock(value2));
        blocks.add(new IntBlock(value3));

        data.appendBlocks(blocks);
        ADS.appendBlocks(blocks);

        IntData dataOther = new IntData();
        dataOther.addInts(new int[]{289, 27, 295, 28, 2, 9358, 288, 57, 928, 673, 1000, 2832987, 473, value1, value2, value3});
        HashTreeADS ADSOther = new HashTreeADS(dataOther.getBlocks());

        assertTrue(ADS.getAuthenticator().equals(ADSOther.getAuthenticator()));

        testVerify();
    }

    @Test
    public void testInsertBlocks() {
        int index = 4;
        int value1 = 43, value2 = 129, value3 = 567;
        List<Block> blocks = new ArrayList<>();
        blocks.add(new IntBlock(value1));
        blocks.add(new IntBlock(value2));
        blocks.add(new IntBlock(value3));

        data.insertBlocks(index, blocks);
        ADS.insertBlocks(index, blocks);

        IntData dataOther = new IntData();
        dataOther.addInts(new int[]{289, 27, 295, 28, value1, value2, value3, 2, 9358, 288, 57, 928, 673, 1000, 2832987, 473});
        HashTreeADS ADSOther = new HashTreeADS(dataOther.getBlocks());

        assertTrue(ADS.getAuthenticator().equals(ADSOther.getAuthenticator()));

        testVerify();
    }

    @Test
    public void testDeleteBlocks() {
        int index = 2;
        int length = 3;
        data.deleteBlocks(index, length);
        ADS.deleteBlocks(index, length);

        IntData dataOther = new IntData();
        dataOther.addInts(new int[]{289, 27, 9358, 288, 57, 928, 673, 1000, 2832987, 473});
        HashTreeADS ADSOther = new HashTreeADS(dataOther.getBlocks());

        assertTrue(ADS.getAuthenticator().equals(ADSOther.getAuthenticator()));

        testVerify();
    }

    /**
     * Queries each block separately and tests whether they verify correctly.
     */
    @Test
    public void testVerify() {
        // Verify the query for each block
        for (int i = 0; i < ADS.getLeaves().size(); i++) {
            Block block = data.getBlock(i);
            HTProof proof = ADS.getProof(i);

            assertTrue(ADS.verify(block, proof, ADS.getAuthenticator()));
        }
    }
}
