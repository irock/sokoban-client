package Sokoban;

public enum Direction {
    UP(0, 1, "U"),
    RIGHT(1, 0, "R"),
    DOWN(0, -1, "D"),
    LEFT(-1, 0, "L");

    final int dx;
    final int dy;
    final String rep;

    Direction(int dx, int dy, String rep) {
        this.dx = dx;
        this.dy = dy;
        this.rep = rep;
    }

    @Override
    public String toString() {
        return rep;
    }
};
