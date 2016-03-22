package benchmarks;

import framework.*;
import skiplist.SkipListADS;
import splithash.SplitHashADS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

/**
 * Performs a benchmark on an ADS
 */
public abstract class Benchmark {
    protected int size;
    protected int times;
    protected IntData data;
    protected Random rand;

    protected static final int SIZE_DEFAULT = 73; // not used in practice
    protected static final int TIMES_DEFAULT = 100;

    // Number of times that a benchmark is repeated within the same data input.
    // Without repetitions, duration of one performance is too short to be accurately measured with milliseconds.
    protected static final int REPETITIONS = 10;

    private static final DecimalFormat df = new DecimalFormat("#.000000");

    public Benchmark() {
        this(SIZE_DEFAULT, TIMES_DEFAULT);
    }

    public Benchmark(int size, int times) {
        this.size = size;
        this.times = times;

        rand = new Random();
        //rand.setSeed(123456);
    }

    protected abstract String getName();

    /**
     * Returns a copy of the data, created by this benchmark,
     * to prevent errors when executing this benchmark multiple times in a row.
     * @return A copy of the data
     */
    public Data getData() {
        return data.copy();
    }

    protected abstract void setRandomData();

    protected abstract void setRandomOperationInfo();

    protected void setADSSpecificInfo(ADS ADS) {}

    public void run(ADS[] ADSs, String[] ADSNames) {
        int ADSCount = ADSs.length;
        long[] durations = new long[ADSCount];

        System.out.println("Benchmark " + getName() + " (size " + size + ", times " + times + ", repetitions " + REPETITIONS + "):");

        int dummy = 5;

        for (int t = 1; t <= times; t++) {
            // Initialize new data and operation info for this repetition
            setRandomData();
            setRandomOperationInfo();

            IntData[] datas = new IntData[ADSs.length];

            // Set new data for each ADS
            for (int i = 0; i < ADSs.length; i++) {
                datas[i] = data.copy();
                ADS ADS = ADSs[i];
                List<Block> blocks = datas[i].getBlocks();
                ADS.setThis(ADS.create(blocks));
            }

            // Perform operation for each ADS
            for (int i = 0; i < ADSCount; i++) {
                IntData data = datas[i];
                ADS ADS = ADSs[i];

                setADSSpecificInfo(ADS);

                long startTime = getTime();

                //if (ADS instanceof SkipListADS || ADS instanceof SplitHashADS)
                    dummy |= performOperation(data, ADS);

                durations[i] += getTime() - startTime;
            }

            //System.out.print(t + "/" + times + " ");
        }

        //System.out.println("");

        String line = "" + size;

        // Print results
        for (int i = 0; i < ADSCount; i++) {
            double singleDuration = (double)durations[i] / (double) times / (double) REPETITIONS/ 1000000;
            System.out.println(ADSNames[i] + ": " + df.format(singleDuration) + " ms");
            System.out.println("dummy = " + dummy);

            //System.out.print(df.format(singleDuration) + " ; ");

            line += " " + df.format(singleDuration);
        }

        // Write results to benchmark's file
        line = line.replaceAll(",", ".");
        line += '\n';

        if (getFileLoc() != null) {

            try {
                Files.write(Paths.get(getFileLoc()), line.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
                e.printStackTrace();
            }

        }

        System.out.println();
        System.out.println();
    }

    public void runBatch(ADS[] ADSs, String[] ADSNames) {
        // Warm up
        for (size = 100; size <= 300; size += 100) {
            run(ADSs, ADSNames);
        }

        for (size = 400; size <= 4800; size += 400) {
            run(ADSs, ADSNames);
        }
    }

    protected abstract String getFileLoc();

    protected abstract int performOperation(IntData data, ADS ADS);

    private long getTime() {
        return System.nanoTime();
    }
}
