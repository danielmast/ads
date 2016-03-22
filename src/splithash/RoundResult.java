package splithash;

import java.util.ArrayList;

/**
 * Result object of a round. Consists of 3 node lists.
 */
public class RoundResult {
    private final ArrayList<Node> center;
    private final ArrayList<Node> leftFringe;
    private final ArrayList<Node> rightFringe;

    public RoundResult() {
        this.center = new ArrayList<>();
        this.leftFringe = new ArrayList<>();
        this.rightFringe = new ArrayList<>();
    }

    public ArrayList<Node> getCenter() {
        return center;
    }

    public ArrayList<Node> getLeftFringe() {
        return leftFringe;
    }

    public ArrayList<Node> getRightFringe() {
        return rightFringe;
    }

    public void addCenter(Node node) {
        center.add(node);
    }

    public void addLeftFringe(Node node) {
        leftFringe.add(node);
    }

    public void addRightFringe(Node node) {
        rightFringe.add(node);
    }
}
