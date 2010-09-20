package Sokoban;
import java.util.LinkedList;
import java.util.List;
import java.awt.Point;

public class State{
	
	private Box[] boxes;
	private Player player;
	
	public State(){
		
	}

	public Player getPlayer(){
		return player;
	}

	public Box getBox(int i){
		return boxes[i];
	}
	public Box[] getBoxes(){
		return boxes;
	}

	public void updateBox(int i, int x, int y){
		boxes[i].getPosition().setLocation(x, y);
	}

	public Box getBoxByPoint(Point pos){
		for(int i = 0; i < boxes.length; i++){
			if(pos.equals(boxes[i].position)){
				return boxes[i];
			}
		}
        return null;
	}

	public boolean isFree(Point pos){
		for(int i = 0; i < boxes.length; i++){
			if(pos.equals(boxes[i].position)){
				return false;
			}
		}
		return true;
	}
}
