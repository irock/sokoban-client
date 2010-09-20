package Sokoban;
import java.util.LinkedList;
import java.util.List;
import java.awt.Point;
import java.util.ArrayList;

public class State{
	
	private ArrayList<Box> boxes;
	private Player player;
	
	public State(){
		boxes = new ArrayList<Box>();
	}

	public Player getPlayer(){
		return player;
	}

    public void addBox(Box box) {
        boxes.add(box);
    }

	public Box getBox(int i){
		return boxes.get(i);
	}
	public ArrayList<Box> getBoxes(){
		return boxes;
	}

	public void updateBox(int i, int x, int y){
		boxes.get(i).getPosition().setLocation(x, y);
	}

	public Box getBoxByPoint(Point pos){
        for(Box box : boxes) {
			if(pos.equals(box.position)){
				return box;
			}
		}
        return null;
	}

	public boolean isFree(Point pos){
        for(Box box : boxes) {
			if(pos.equals(box.position)){
				return false;
			}
		}
		return true;
	}
}
