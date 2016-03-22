package framework;

import java.util.List;

/**
 * The Authenticated Data Structure of the original data. Defines how the ADS changes when modifications are
 * performed on the data, and defines how the authenticator value is obtained from it.
 */
public abstract class ADS<A extends ADS, P extends Proof, Auth extends Authenticator> {
    public ADS() {}

    public ADS(List<Block> blocks) {}

    /**
     * Defines how an ADS is constructed from a list of blocks.
     * @param blocks The input blocks
     * @return the created ADS
     */
    public abstract A create(List<Block> blocks);

    /**
     * Define how the values of the given ADS are set/copied to this ADS
     * @param ADS the to-be-copied ADS
     */
    public abstract void setThis(A ADS);

    /**
     * Defines how two ADSs are appended
     * @param left the left ADS
     * @param right the right ADS
     * @return the resulting ADS
     */
    public abstract A merge(A left, A right);

    /**
     * Defines how an ADS is split into two separate ADSs, at the given index
     * @param ADS the to-be-split ADS
     * @param index the location where to split
     * @return an array, containing the two resulting ADSs
     */
    public abstract A[] split(A ADS, int index);

    /**
     * * Defines how this ADS is updated when a list of blocks is appended to it
     * @param blocks the list of to-be-appended blocks
     */
    public void appendBlocks(List<Block> blocks) {
        A right = create(blocks);
        A result = merge((A) this, right);
        setThis(result);
    }

    /**
     * Defines how this ADS is updated when a list of blocks is inserted
     * @param index the location where the new blocks go
     * @param blocks the to-be-inserted blocks
     */
    public void insertBlocks(int index, List<Block> blocks) {
        A middle = create(blocks);
        A[] split = split((A) this, index);
        A left = split[0];
        A right = split[1];
        A result = merge(left, middle);
        result = merge(result, right);
        setThis(result);
    }


    /**
     * Defines how a number of blocks are deleted from the ADS
     * @param index The position of the first to-be-deleted block
     * @param length The number of to-be-deleted blocks
     */
    public void deleteBlocks(int index, int length) {
        A[] temp = split((A) this, index);
        A left = temp[0];
        A right = split(temp[1], length)[1];
        A result = merge(left, right);
        setThis(result);
    }

    /**
     * Returns a proof for a block with the given index
     * @param index index of a block we want the proof for
     * @return the proof
     */
    public abstract P getProof(int index);

    /**
     * Get the authenticator value out of the ADS
     * @return the authenticator value of this ADS
     */
    public abstract Auth getAuthenticator();

    /**
     * Uses the block, the proof, and the authenticator to decide whether the response is correct or not.
     * @param authenticator The authenticator received from the writer.
     * @return true if the block and the proof are correct. false otherwise.
     */
    public abstract boolean verify(Block block, P proof, Auth authenticator);
}
