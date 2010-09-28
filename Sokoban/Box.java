package Sokoban;
import java.awt.Point;

public class Box {

    Point position;

    public Box(int xpos, int ypos) {
        position = new Point(xpos, ypos);

    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point p) {
        position.x = p.x;
        position.y = p.y;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Box))
            return false;
        return this == o || position.equals(((Box)o).position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}
