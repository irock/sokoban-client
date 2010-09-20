package Sokoban;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class Player {

    Point position;

    public Player() {
        this(0, 0);
    }

    public Player(int x, int y) {
        position = new Point(x, y);
    }

    public List<Direction> getMovements(Map map, State state) {
        List<Direction> movements = new ArrayList<Direction>();
        for(Direction d : Direction.values()) {
            Point p = new Point(position.x + d.dx, position.y + d.dy);
            if(map.getMatrix()[p.y][p.x] == 0 && state.isFree(p))
                movements.add(d);
        }
        return movements;
    }

    public Set<Point> getReachablePositions(Map map, State state) {
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> positions = new HashSet<Point>();
        Set<Box> boxes = new HashSet<Box>();

        Point origPos = position;
        queue.add(position);

        while(!queue.isEmpty()) {
            position = queue.poll();

            for(Direction d : Direction.values()) {
                Point p = new Point(position.x + d.dx, position.y + d.dy);
                
                if(map.getMatrix()[p.y][p.x] == 0) {
                    if(state.isFree(p)) {
                        if(!positions.contains(p)) {
                            queue.add(p);
                            positions.add(p);
                        }
                    } else {
                        boxes.add(state.getBoxByPoint(p));
                    }
                }
            }
        }

        // TODO return boxes too.
        position = origPos;
        return positions;
    }

    public static void main(String[] args) {
        String mapString = "################\n" +
                           "#   $          #\n" +
                           "##  #          #\n" +
                           "################\n";
        
        Player player = new Player(1, 1);
        State state = new State();
        Map map = new Map(Map.parse(mapString, state));

        for(Point p : player.getReachablePositions(map, state)) {
            System.out.printf("x: %d, y: %d\n", p.x, p.y);
        }
    }
}
