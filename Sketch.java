import java.awt.Color;
import java.awt.Point;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
/**
 * Sketch class created to update local and master drawings
 * @author rohithmandavilli and Sanjana Goli
 *
 */
public class Sketch {
	private TreeMap<Integer, Shape> sketchMap;
	int id;												//holds the global id counter - represents the amount of shapes in the sketch
	
	
	public Sketch() {
		sketchMap = new TreeMap<Integer, Shape>();
		id = 0;
	}
	
	/**
	 * @param i id of the shape
	 * @param s shaoe to be added
	 */
	public synchronized void addSketch(Integer i, Shape s) {
		sketchMap.put(i, s);
	}
	
	/**
	 * removes a shape with the given id
	 * @param id to be found
	 */
	public synchronized void deleteShape(Integer id) {
		sketchMap.remove(id);
	}
	
	/**
	 * updates the color of an id
	 * @param id
	 * @param color
	 */
	public synchronized void changeColor(Integer id, Color color) {
		sketchMap.get(id).setColor(color);
	}
	
	/**
	 * handles movement for a shape
	 * @param id shape to be moved
	 * @param dx change in x
	 * @param dy change in y
	 */
	public synchronized void move(Integer id, int dx, int dy) {
		Shape s = sketchMap.get(id);
		s.moveBy(dx, dy);
		sketchMap.put(id, s);
	}
	/**
	 * 
	 * @param s shape to find
	 * @return returns the id of the given shape
	 */
	public synchronized int getID(Shape s) {
		Set <Integer> keys = sketchMap.keySet(); //to get the topmost shape
		for(Integer i: keys)
			if(sketchMap.get(i).equals(s))
				return i;
		return -1;
	}
	/**
	 * 
	 * @return returns the map of ids to shapes
	 */
	public synchronized TreeMap<Integer, Shape> getMap() {
		return sketchMap;
	}
	
	/**
	 * @param p 
	 * @return returns the shape that the selected point is in
	 */
	public synchronized Shape findShape(Point p) {
		Set <Integer> keys = sketchMap.descendingKeySet(); //to get the topmost shape
		for(Integer i: keys)
			if(sketchMap.get(i).contains(p.x, p.y))
				return sketchMap.get(i);
		return null;
	}
	/**
	 * overrides the toString, and returns a string with the toString of each shape in the sketch
	 */
	public String toString() {
		String sketchState = "";
		for(Integer i : sketchMap.keySet()) { //go to each shape integer points to the id
			sketchState += "a" + sketchMap.get(i).toString() + "\n";
		}
		return sketchState;
	}
	/**
	 * returns a generated id for a new shape
	 * @return the new generate id for the shape
	 */
	public int changeID() {
		return id++;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
