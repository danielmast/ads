package benchmarks;

import framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert Benchmark
 */
public class MergeBenchmark extends Benchmark {

    private List<List<Block>> leftBlocks, rightBlocks;

    private List<ADS> leftADSs, rightADSs;

    @Override
    protected String getName() {
        return "MERGE";
    }

    @Override
    protected void setRandomData() {
        // This is not used
        data = new IntData();
    }

    @Override
    protected void setRandomOperationInfo() {
        leftBlocks = new ArrayList<>(REPETITIONS);
        rightBlocks = new ArrayList<>(REPETITIONS);

        for (int i = 0 ; i < REPETITIONS; i++) {
            List<Block> leftList = new ArrayList<>();
            List<Block> rightList = new ArrayList<>();
            leftBlocks.add(leftList);
            rightBlocks.add(rightList);


            // size / 2, so that left and right together merge to an ADS with size size
            for (int j = 0; j < size / 2; j++) {
                Block block = new IntBlock(rand.nextInt(10000000));
                leftList.add(block);

            }

            for (int j = 0; j < size / 2; j++) {
                Block block = new IntBlock(rand.nextInt(10000000));
                rightList.add(block);
            }
        }
    }

    @Override
    protected void setADSSpecificInfo(ADS ADS) {
        leftADSs = new ArrayList<>();
        rightADSs = new ArrayList<>();

        for (List<Block> blocks : leftBlocks) {
            leftADSs.add(ADS.create(blocks));
        }

        for (List<Block> blocks : rightBlocks) {
            rightADSs.add(ADS.create(blocks));
        }
    }

    @Override
    protected String getFileLoc() {
        return "C:\\Users\\Daniel\\Dropbox\\Thesis\\Benchmarks\\merge.txt";
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        int dummy = 5;

        for (int i = 0; i < REPETITIONS; i++) {
            ADS left = leftADSs.get(i);
            ADS right = rightADSs.get(i);
            ADS = ADS.merge(left, right);

            dummy |= ADS.hashCode();

            //testVerify(i, ADS);
        }

        return dummy;
    }

    private void testVerify(int rep, ADS ADS) {
        for (int i = 0; i < leftBlocks.get(rep).size() + rightBlocks.get(rep).size(); i++) {
            Block block;
            if (i < leftBlocks.get(rep).size())
                block = leftBlocks.get(rep).get(i);
            else
                block = rightBlocks.get(rep).get(i - leftBlocks.get(rep).size());

            Proof proof = ADS.getProof(i);
            Authenticator auth = ADS.getAuthenticator();
            if (!ADS.verify(block, proof, auth)) {
                System.out.println("debug");
            }
        }
    }
}
