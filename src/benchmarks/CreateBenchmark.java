package benchmarks;

import framework.*;

/**
 * Insert Benchmark
 */
public class CreateBenchmark extends Benchmark {

    @Override
    protected String getName() {
        return "CREATE";
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
    }

    @Override
    protected String getFileLoc() {
        return "C:\\Users\\Daniel\\Dropbox\\Thesis\\Benchmarks\\create.txt";
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        for (int i = 0; i < REPETITIONS; i++) {
            ADS = ADS.create(data.getBlocks());

            //testVerify(data, ADS);
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
