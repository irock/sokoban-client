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
		boxes[i].xpos=x;
		boxes[i].ypos=y;
	}
	public boolean isFree(int x, int y){
		for(int i = 0; i < boxes.length; i++){
			if(boxes[i].xpos == x && boxes[i].ypos == y){
				return false;
			}
		}
		return true;
	}
}