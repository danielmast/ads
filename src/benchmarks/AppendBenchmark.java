package benchmarks;

import framework.ADS;
import framework.Block;
import framework.IntBlock;
import framework.IntData;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert Benchmark
 */
public class AppendBenchmark extends Benchmark {
    private List<List<Block>> appendBlocks;

    private static final int BLOCKSPERAPPEND = 4;

    @Override
    protected String getName() {
        return "APPEND";
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
        appendBlocks = new ArrayList<>(REPETITIONS);

        for (int i = 0 ; i < REPETITIONS; i++) {
            List<Block> blocks = new ArrayList<>(4);
            appendBlocks.add(blocks);
            for (int j = 0; j < BLOCKSPERAPPEND; j++) {
                Block block = new IntBlock(rand.nextInt(10000000));
                blocks.add(block);
            }
        }
    }

    @Override
    protected String getFileLoc() {
        return "C:\\Users\\Daniel\\Dropbox\\Thesis\\Benchmarks\\append.txt";
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        for (int i = 0; i < REPETITIONS; i++) {
            data.appendBlocks(appendBlocks.get(i));
            ADS.appendBlocks(appendBlocks.get(i));
        }

        return 0;
    }
}
