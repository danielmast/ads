package benchmarks;

import framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert Benchmark
 */
public class InsertBenchmark extends Benchmark {

    private List<List<Block>> insertBlocks;
    private int[] insertLocs;

    private static int BLOCKSPERINSERT = 4;

    @Override
    protected String getName() {
        return "INSERT";
    }

    @Override
    protected void setRandomData() {
        data = new IntData();

        for (int i = 0; i < size; i++) {
            data.addInt(rand.nextInt(10000000));
        }
    }

    @Override
    protected void setRandomOperationInfo() {
        insertBlocks = new ArrayList<>(REPETITIONS);
        insertLocs = new int[REPETITIONS];

        for (int i = 0 ; i < REPETITIONS; i++) {
            List<Block> blocks = new ArrayList<>(BLOCKSPERINSERT);
            insertBlocks.add(blocks);

            for (int j = 0; j < BLOCKSPERINSERT; j++) {
                Block block = new IntBlock(rand.nextInt(10000000));
                blocks.add(block);
            }

            insertLocs[i] = rand.nextInt(size + i*BLOCKSPERINSERT);
        }
    }

    @Override
    protected String getFileLoc() {
        return null;
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        for (int i = 0; i < REPETITIONS; i++) {
            int loc = insertLocs[i];

            List<Block> blocks = insertBlocks.get(i);

            data.insertBlocks(loc, blocks);
            ADS.insertBlocks(loc, blocks);

            testVerify(data, ADS);
        }

        return 0;
    }

    private void testVerify(IntData data, ADS ADS) {
        for (int i = 0; i < data.getBlockCount(); i++) {
            Block block = data.getBlock(i);
            Proof proof = ADS.getProof(i);
            Authenticator auth = ADS.getAuthenticator();

            if (!ADS.verify(block, proof, auth)) {
                System.out.println("debug");
            }
        }
    }
}
