package tests;

import framework.IntBlock;
import org.junit.Test;
import utils.Utils;

import java.security.MessageDigest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IntBlockTest {
    @Test
    public void testToString() {
        IntBlock block = new IntBlock(42);

        String result = block.toString();
        String expected = "42";

        assertEquals(expected, result);
    }

    @Test
    public void testHash() {
        IntBlock block = new IntBlock(42);

        byte[] result = block.hash();

        byte[] expected_input = "42".getBytes();
        MessageDigest md = Utils.getMD();
        md.update(expected_input);
        byte[] expected = md.digest();

        assertArrayEquals(expected, result);
    }
}
