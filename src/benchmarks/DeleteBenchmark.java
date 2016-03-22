package benchmarks;

import framework.ADS;
import framework.IntData;

/**
 * Delete Benchmark
 */
public class DeleteBenchmark extends Benchmark {

    private int[] deleteLocs;

    private static int BLOCKSPERDELETE = 4;

    @Override
    protected String getName() {
        return "DELETE";
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
        deleteLocs = new int[REPETITIONS];

        for (int i = 0 ; i < REPETITIONS; i++) {
            deleteLocs[i] = rand.nextInt(size - (i+1) * BLOCKSPERDELETE);
        }
    }

    @Override
    protected String getFileLoc() {
        return null;
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        for (int i = 0; i < REPETITIONS; i++) {
            int index = deleteLocs[i];

            data.deleteBlocks(index, BLOCKSPERDELETE);
            ADS.deleteBlocks(index, BLOCKSPERDELETE);
        }

        return 0;
    }
}