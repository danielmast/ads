package benchmarks;

import framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert Benchmark
 */
public class SplitBenchmark extends Benchmark {

    private List<List<Block>> blocksList;
    private List<ADS> ADSs;
    private int[] splitLocs;

    @Override
    protected String getName() {
        return "SPLIT";
    }

    @Override
    protected void setRandomData() {
        // This is not used
        data = new IntData();
    }

    @Override
    protected void setRandomOperationInfo() {
        blocksList = new ArrayList<>(REPETITIONS);
        splitLocs = new int[REPETITIONS];

        for (int i = 0 ; i < REPETITIONS; i++) {
            List<Block> blocks = new ArrayList<>();
            blocksList.add(blocks);

            for (int j = 0; j < size; j++) {
                Block block = new IntBlock(rand.nextInt(10000000));
                blocks.add(block);
            }

            splitLocs[i] = rand.nextInt(size - 1) + 1; // 1 <= result <= size - 1
        }
    }

    @Override
    protected void setADSSpecificInfo(ADS ADS) {
        ADSs = new ArrayList<>();

        for (List<Block> blocks : blocksList) {
            ADSs.add(ADS.create(blocks));
        }
    }

    @Override
    protected String getFileLoc() {
        return "C:\\Users\\Daniel\\Dropbox\\Thesis\\Benchmarks\\split.txt";
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        int dummy = 5;

        for (int i = 0; i < REPETITIONS; i++) {

            ADS toSplit = ADSs.get(i);
            int index = splitLocs[i];
            ADS[] split = ADS.split(toSplit, size/2);

            dummy |= split[0].hashCode();
            dummy |= split[1].hashCode();

//            List<Block> leftBlocks = new ArrayList<>(blocksList.get(i).subList(0, index));
//            testVerify(leftBlocks, split[0]);
//
//            List<Block> rightBlocks = new ArrayList<>(blocksList.get(i).subList(index, blocksList.get(i).size()));
//            testVerify(rightBlocks, split[1]);
        }

        return dummy;
    }

    private void testVerify(List<Block> blocks, ADS ADS) {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            Proof proof = ADS.getProof(i);
            Authenticator auth = ADS.getAuthenticator();

            if (!ADS.verify(block, proof, auth)) {
                System.out.println("debug");
            }
        }
    }
}
