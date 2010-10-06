package Sokoban;

/**
 * Represents a position in e.g\ a Sokoban puzzle map. The position is in the
 * plane and therefore has x- and y-coordinates.
 */
public class Point implements Comparable<Point> {
    /**
     * The x-coordinate of this Point.
     */
    int x;

    /**
     * The y-coordinate of this Point.
     */
    int y;

    /**
     * Create a new Point.
     *
     * @param x The x-coordinate of the Point.
     * @param y The y-coordinate of the Point.
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor.
     *
     * @param p The point to copy.
     */
    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * Comparison method. The points are compared first with respect to the
     * x-coordinate and then with respect to the y-coordinate.
     *
     * @param p A point to compare this point to.
     * @return a value less than 0 iff this point is compared to be less than
     * p, 0 iff they are equal and more than 0 otherwise.
     */
    public int compareTo(Point p) {
        if (x != p.x)
            return x - p.x;
        return y - p.y;
    }

    /**
     * Add the given deltas to this points coordinates.
     *
     * @param dx The delta in x-way.
     * @param dy The delta in y-way.
     */
    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }

    /**
     * Set the x- and y-coordinate of the Point.
     *
     * @param x The new x-coordinate.
     * @param y The new y-coordinate.
     */
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy the x- and y-coordinates of the given point.
     *
     * @param p The Point to copy.
     */
    public void set(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point))
            return false;
        Point p = (Point)o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return (x << 16) + y;
    }
}
