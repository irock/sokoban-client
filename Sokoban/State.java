package Sokoban;
import java.util.LinkedList;
import java.util.List;
import java.awt.Point;
import java.util.List;
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
	/*
	 * Returns an arraylist of the boxes in this state
	 */
	public ArrayList<Box> getBoxes(){
		return boxes;
	}
	public void updateBox(int i, int x, int y){
		boxes.get(i).getPosition().setLocation(x, y);
	}
	public void updatePlayer(Point pos){
		//TODO
	}
	/*
	 * Get box if it is at the position and else returns null
	 */
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
