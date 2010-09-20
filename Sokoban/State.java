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
}