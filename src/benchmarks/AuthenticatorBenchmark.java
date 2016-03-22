package benchmarks;

import framework.ADS;
import framework.IntData;

/**
 * Insert Benchmark
 */
public class AuthenticatorBenchmark extends Benchmark {

    @Override
    protected String getName() {
        return "AUTHENTICATOR";
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
        return null;
    }

    @Override
    protected int performOperation(IntData data, ADS ADS) {
        for (int i = 0; i < REPETITIONS; i++) {
            ADS.getAuthenticator();
        }

        return 0;
    }
}
