package benchmarks;

import framework.ADS;
import framework.Block;
import framework.IntData;
import hashtree.HashTreeADS;
import skiplist.SkipListADS;
import splithash.SplitHashADS;

import java.util.List;

/**
 * Runs a benchmark for each implemented ADS, on the same initialized data.
 */
public class Main {
    public static void main(String args[]) {
        IntData data = new IntData();
        data.addInt(1);
        List<Block> blocks = data.getBlocks();

        ADS[] ADSs = new ADS[]{new HashTreeADS(blocks), new SkipListADS(blocks),
                new SplitHashADS(blocks)};

        String[] ADSNames = new String[]{"Hash Tree", "Skip list", "SplitHash"};

        CreateBenchmark create = new CreateBenchmark();
        create.runBatch(ADSs, ADSNames);
////
//        MergeBenchmark merge = new MergeBenchmark();
//        merge.runBatch(ADSs, ADSNames);
//
//        SplitBenchmark split = new SplitBenchmark();
//        split.runBatch(ADSs, ADSNames);

//        ProofBenchmark proof = new ProofBenchmark();
//        proof.runBatch(ADSs, ADSNames);
//
//        VerifyBenchmark verify = new VerifyBenchmark();
//        verify.runBatch(ADSs, ADSNames);
////
//        AppendBenchmark append = new AppendBenchmark();
//        append.run(ADSs, ADSNames);
//
//        InsertBenchmark insert = new InsertBenchmark();
//        insert.run(ADSs, ADSNames);
//
//        DeleteBenchmark delete = new DeleteBenchmark();
//        delete.run(ADSs, ADSNames);
//
//
//
//        AuthenticatorBenchmark authenticator = new AuthenticatorBenchmark();
//        authenticator.run(ADSs, ADSNames);
//
//
    }
}
