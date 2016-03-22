package tests;

import framework.Block;
import framework.IntBlock;
import framework.IntData;
import org.junit.Before;
import org.junit.Test;
import skiplist.SkipListADS;
import skiplist.SLProof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class SkipListADSTest {
    private IntData data;
    private SkipListADS ADS;

    @Before
    public void initialize() {
        data = new IntData();
        data.addInts(new int[]{289, 27, 295, 28, 2, 9358, 288, 57, 928, 673, 1000, 2832987, 473});
        ADS = new SkipListADS(data.getBlocks());

        System.out.println(ADS.toString());
    }

    @Test
    public void testSplit() {
        int index = 4;

        SkipListADS[] leftAndRight = ADS.split(ADS, index);
        SkipListADS left = leftAndRight[0];
        SkipListADS right = leftAndRight[1];

        System.out.println(left);
        System.out.println(right);

        for (int i = 0; i < data.getBlockCount(); i++) {
            Block block = data.getBlock(i);

            if (i < index) {
                SLProof proof = left.getProof(i);
                assertTrue(left.verify(block, proof, left.getAuthenticator()));
            } else {
                SLProof proof = right.getProof(i-index);
                assertTrue(right.verify(block, proof, right.getAuthenticator()));
            }
        }
    }

    @Test
    public void testMerge() {

        int index = 4;

        SkipListADS[] leftAndRight = ADS.split(ADS, index);
        SkipListADS left = leftAndRight[0];
        SkipListADS right = leftAndRight[1];

        System.out.println("LEFT");
        System.out.println(left);

        System.out.println("RIGHT");
        System.out.println(right);

        System.out.println("MERGED");
        SkipListADS merged = ADS.merge(left, right);

        System.out.println(merged);

        for (int i = 0; i < data.getBlockCount(); i++) {
            Block block = data.getBlock(i);
            SLProof proof = merged.getProof(i);

            assertTrue(merged.verify(block, proof, merged.getAuthenticator()));
        }
    }

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
        SkipListADS ADSOther = new SkipListADS(dataOther.getBlocks());

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
        SkipListADS ADSOther = new SkipListADS(dataOther.getBlocks());

        System.out.println("ADS after insertion");
        System.out.println(ADS);

        System.out.println("ADSOther");
        System.out.println(ADSOther);

        assertTrue(ADS.getAuthenticator().equals(ADSOther.getAuthenticator()));

        testVerify();
    }

    @Test
    public void testDeleteBlock() {
        int index = 2;
        int length = 3;
        data.deleteBlocks(index, length);
        ADS.deleteBlocks(index, length);

        IntData dataOther = new IntData();
        dataOther.addInts(new int[]{289, 27, 9358, 288, 57, 928, 673, 1000, 2832987, 473});
        SkipListADS ADSOther = new SkipListADS(dataOther.getBlocks());

        assertTrue(ADS.getAuthenticator().equals(ADSOther.getAuthenticator()));

        testVerify();
    }

    @Test
    public void testComputeLabel() {
        byte[] startNodeLabel = ADS.getStartNode().getLabel();
        System.out.println(Arrays.toString(startNodeLabel));

        byte[] auth = ADS.getStartNode().computeLabel();
        System.out.println(Arrays.toString(auth));
    }

    @Test
    public void testToString() {
        IntData data = new IntData();
        data.addInts(new int[]{5});
        SkipListADS ADSSimple = new SkipListADS(data.getBlocks());

        System.out.println(ADSSimple);
    }

    @Test
    public void testVerify() {
        //ADS.refreshAllLabels();

        // Verify the query for each block
        for (int i = 0; i < data.getBlockCount(); i++) {
            Block block = data.getBlock(i);
            SLProof proof = ADS.getProof(i);

            assertTrue(ADS.verify(block, proof, ADS.getAuthenticator()));
        }
    }
}
