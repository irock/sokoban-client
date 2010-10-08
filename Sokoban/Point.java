package Sokoban;

/**
 * Represents a position in e.g\ a Sokoban puzzle map. The position is in the
 * plane and therefore has x- and y-coordinates.
 */
public class Point implements Comparable<Point> {
    /**
     * The x-coordinate of this Point.
     */
    final int x;

    /**
     * The y-coordinate of this Point.
     */
    final int y;

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
        this(p.x, p.y);
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
