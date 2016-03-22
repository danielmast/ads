package benchmarks;

import framework.ADS;
import framework.IntData;

/**
 * Insert Benchmark
 */
public class ProofBenchmark extends Benchmark {

    private int[] proofLocs;

    @Override
    protected String getName() {
        return "PROOF";
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
        proofLocs = new int[REPETITIONS];

        for (int i = 0 ; i < REPETITIONS; i++) {
            proofLocs[i] = rand.nextInt(size);
        }
    }

    @Override
    protected String getFileLoc() {
        return "C:\\Users\\Daniel\\Dropbox\\Thesis\\Benchmarks\\proof.txt";
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        for (int i = 0; i < REPETITIONS; i++) {
            int index = proofLocs[i];
            ADS.getProof(index);
        }

        return 0;
    }
}
