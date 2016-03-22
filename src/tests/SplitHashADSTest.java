package tests;

import framework.Block;
import framework.IntBlock;
import framework.IntData;
import org.junit.Before;
import org.junit.Test;
import splithash.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class SplitHashADSTest {
    private IntData data;
    private SplitHashADS ADS;
    private final int randomRuns = 1000;
    private final int randomLength = 100;
    private Random rand;

    @Before
    public void initialize() {
        data = new IntData();
        data.addInts(new int[]{289, 27, 295, 28, 2, 9358, 288, 57, 928, 673, 1000, 2832987, 473});
        ADS = new SplitHashADS(data.getBlocks());
        rand = new Random();
    }

    public SplitHashADS randomMerge(List<Block> sequence) {
        List<SplitHashADS> res = new ArrayList<>(sequence.size());

        for (Block block : sequence) {
            LeafNode node = new LeafNode(block);
            res.add(new SplitHashADS(node));
        }

        while (res.size() > 1) {
            // Pick a random index
            int idx = rand.nextInt(res.size() - 1);
            // Merge the SeqHashes at idx and idx+1 and put the result on index idx
            res.set(idx, (new SplitHashADS()).merge(res.get(idx), res.get(idx + 1)));
            // Remove the residue
            res.remove(idx+1);
        }

        return res.get(0);

    }

    /**
     * This test uses randomMerge to create 2 SeqHashes with the same set of nodes, that are
     * both merged, but in a (randomly selected) different order. If the resulting SeqHashes
     * are equal, then this proves that the associativity property holds.
     */
    @Test
    public void testRandomMerge() {

        for (int run = 0; run < randomRuns; run++) {
            List<Block> sequence = new ArrayList<>();

            for (int i = 0; i < randomLength; i++) {
                IntBlock block = new IntBlock(getRandomInt());
                sequence.add(block);
            }

            SplitHashADS a = randomMerge(sequence);
            SplitHashADS b = randomMerge(sequence);

            assertTrue(a.equals(b));

            Node aTop = a.finish();
            Node bTop = b.finish();

            assertTrue(aTop.equals(bTop));
        }
    }

    private int getRandomInt() {
        return rand.nextInt(2147483647);
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
        SplitHashADS ADSOther = new SplitHashADS(dataOther.getBlocks());

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
        SplitHashADS ADSOther = new SplitHashADS(dataOther.getBlocks());

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
        SplitHashADS ADSOther = new SplitHashADS(dataOther.getBlocks());

        assertTrue(ADS.getAuthenticator().equals(ADSOther.getAuthenticator()));

        testVerify();
    }

    @Test
    public void testGetAuthenticator() {
        byte[] auth0 = ADS.getAuthenticator().getRoots().get(0);

        System.out.println(Arrays.toString(auth0));
    }

    /**
     * Queries each block separately and tests whether they verify correctly.
     */
    @Test
    public void testVerify() {
        // Verify the query for each block
        for (int i = 0; i < ADS.getLeaves().size(); i++) {
            Block block = data.getBlock(i);
            SHProof proof = ADS.getProof(i);

            assertTrue(ADS.verify(block, proof, ADS.getAuthenticator()));
        }
    }

    /**
     * Test split() by building input data of varying sizes, and testing on all split indices
     */
    @Test
    public void testSplitOnStaticSeqHash() {
        int[] in;

        for (int i = 1; i <= 100; i++) { // i = length of in
            System.out.println("in.length = " + i);

            in = new int[i];
            for (int k = 0; k < i; k++) // k = setting in values
                in[k] = k;

            for (int j = 0; j <= i; j++) { // j = split location
                IntData data = new IntData();
                data.addInts(in);

                IntData dataA = new IntData();
                dataA.addInts(Arrays.copyOfRange(in, 0, j));
                IntData dataB = new IntData();
                dataB.addInts(Arrays.copyOfRange(in, j, in.length));

                SplitHashADS ADSA = new SplitHashADS(dataA.getBlocks());

                SplitHashADS ADSB = new SplitHashADS(dataB.getBlocks());

                SplitHashADS ADS = new SplitHashADS(data.getBlocks());
                SplitHashADS[] leftRight = new SplitHashADS().split(ADS, j);

                System.out.println("Testing split " + j);
                assertTrue(ADSA.equals(leftRight[0]));
                System.out.println("LEFT PASS");
                assertTrue(ADSB.equals(leftRight[1]));
                System.out.println("RIGHT PASS");
            }
        }
    }

    @Test
    public void testCutSequence() {
        //Random rand = new Random(12352);

        int i = 3000;

        int[] in = new int[i];
        for (int k = 0; k < i; k++) // k = setting in values
            in[k] = rand.nextInt(1000000);

        IntData data = new IntData();
        data.addInts(in);

        SplitHashADS ADS = new SplitHashADS(data.getBlocks());
        SplitHashADS[] seqHash = new SplitHashADS[]{ADS, null};

        int j = rand.nextInt(i);
        while (j > 0) {
            IntData dataA = new IntData();
            dataA.addInts(Arrays.copyOfRange(in, 0, j));

            SplitHashADS ADSA = new SplitHashADS(dataA.getBlocks());

            IntData dataB = new IntData();
            dataB.addInts(Arrays.copyOfRange(in, j, seqHash[0].getLeaves().size()));

            SplitHashADS ADSB = new SplitHashADS(dataB.getBlocks());

            seqHash = new SplitHashADS().split(seqHash[0], j);

            if (!ADSA.equals(seqHash[0]))
                System.out.println("debug");
            if (!ADSB.equals(seqHash[1]))
                System.out.println("debug");

            assertTrue(ADSA.equals(seqHash[0]));
            assertTrue(ADSB.equals(seqHash[1]));
            System.out.println("LEFT AND RIGHT PASS " + j);

            j = rand.nextInt(j);
        }
    }
}
