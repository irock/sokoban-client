import java.awt.Point;

public class Box {

	Point position;
	
	public Box(int xpos, int ypos) {
		position = new Point(xpos, ypos);
		
	}
	
	private Point getPosition() {
		return position;
	}
}
