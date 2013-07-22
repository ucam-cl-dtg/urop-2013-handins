package uk.ac.cam.sup.structures;

public class Quad<X, Y, Z, T> {
    public X x;
    public Y y;
    public Z z;
    public T t;

    public Quad(X x, Y y, Z z, T t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
    }
}
