package benchmarks;

import framework.*;

/**
 * Insert Benchmark
 */
public class VerifyBenchmark extends Benchmark {
    private int[] verifyLocs;
    private Proof[] proofs;
    private Authenticator authenticator;

    @Override
    protected String getName() {
        return "VERIFY";
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
        verifyLocs = new int[REPETITIONS];

        for (int i = 0; i < REPETITIONS; i++) {
            verifyLocs[i] = rand.nextInt(size);
        }
    }

    @Override
    protected void setADSSpecificInfo(ADS ADS) {
        proofs = new Proof[REPETITIONS];
        authenticator = ADS.getAuthenticator();

        for (int i = 0; i < REPETITIONS; i++) {
            int index = verifyLocs[i];
            proofs[i] = ADS.getProof(index);
        }
    }

    @Override
    protected String getFileLoc() {
        return "C:\\Users\\Daniel\\Dropbox\\Thesis\\Benchmarks\\verify.txt";
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        for (int i = 0; i < REPETITIONS; i++) {
            Block block = data.getBlock(verifyLocs[i]);
            Proof proof = proofs[i];
            ADS.verify(block, proof, authenticator);
        }

        return 0;
    }
}
