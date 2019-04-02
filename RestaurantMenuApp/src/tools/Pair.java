package tools;

public class Pair<X, Y> {
    private X left;
    private Y right;

    public Pair(X left, Y right) {
        this.left = left;
        this.right = right;
    }

    public void setLeft(X left) {
        this.left = left;
    }

    public void setRight(Y right) {
        this.right = right;
    }

    public X getLeft() {
        return left;
    }

    public Y getRight() {
        return right;
    }

    @Override
    public String toString() {
        return (left.toString() + ": " + right.toString());
    }
}
