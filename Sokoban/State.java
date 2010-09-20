package Sokoban;

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
	public void updateBox(int i, int x, int y){
		boxes[i].getPosition().setLocation(x, y);
	}
	public boolean isFree(int x, int y){
		for(int i = 0; i < boxes.length; i++){
			if(boxes[i].getPosition().x == x && boxes[i].getPosition().y == y){
				return false;
			}
		}
		return true;
	}
}
