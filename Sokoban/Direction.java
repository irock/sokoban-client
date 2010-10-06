package Sokoban;

/**
 * Represents a direction of movement in the Sokoban game. Defined directions
 * are UP, RIGHT, DOWN and LEFT.
 */
public enum Direction {
    /* Define the available directions. Order matters! The available number of
     * directions matters! */
    UP(0, -1, "U"),
    RIGHT(1, 0, "R"),
    DOWN(0, 1, "D"),
    LEFT(-1, 0, "L");

    /**
     * The delta movement x-ways.
     */
    final int dx;

    /**
     * The delta movement y-ways.
     */
    final int dy;

    /**
     * The string representation of this direction.
     */
    final String rep;

    /**
     * Create a new Direction.
     *
     * @param dx The movement in x-ways this direction corresponds to.
     * @param dy The movement in y-ways this direction corresponds to.
     * @param rep The string representation of this direction/movement.
     */
    Direction(int dx, int dy, String rep) {
        this.dx = dx;
        this.dy = dy;
        this.rep = rep;
    }

    @Override
    public String toString() {
        return rep;
    }

    /**
     * Static method for finding a direction given its delta movements.
     *
     * @param dx The delta movement in x-ways of the direction to find.
     * @param dy The delta movement in y-ways of the direction to find.
     * @return the direction found, or null if no such direction was found.
     */
    public static Direction getDirection(int dx, int dy) {
        for (Direction d : values())
            if (d.dx == dx && d.dy == dy)
                return d;
        return null;
    }
}
