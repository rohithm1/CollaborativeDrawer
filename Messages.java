import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * 
 * Class created to parse messages between the server and the multiple clients
 * @author rohithmandavilli and Sanjana Goli for Problem Set 6
 *
 */
public class Messages {
	private String command;
	private int drawFromX;
	private int drawFromY;
	private int drawToX;
	private int drawToY;
	private Color color;
	private int id;
	private int dx;
	private int dy;
	private String shapeType;
	private List<Segment> freehand;
	
	/**
	 * Parses the line read from the server to update local sketches
	 * @param s line taken in from the server
	 */
	public void parseString(String s) {
		//create the array of all elements in the String
		String [] commands = s.split(" ");
		command = commands[0]; //very first command - either draw, recolor, move or delete
		//handles specific move functionality
		if(commands[0].equals("m")) {
			dx = Integer.parseInt(commands[1]);
			dy = Integer.parseInt(commands[2]);
			id = Integer.parseInt(commands[commands.length-1]);
			color = new Color(Integer.parseInt(commands[commands.length-2]));
		}
		//handles specific recoloring functionality
		else if(commands[0].equals("r")) {
			color = new Color(Integer.parseInt(commands[1]));
			id = Integer.parseInt(commands[commands.length-1]);
		}
		//handles specific drawing functionality, if the shape is not a freehand
		else if(commands[0].equals("a") && !(commands[commands.length-3]).equals("freehand")) {
			drawFromX = Integer.parseInt(commands[1]);
			drawFromY = Integer.parseInt(commands[2]);
			drawToX = Integer.parseInt(commands[3]);
			drawToY = Integer.parseInt(commands[4]);
			id = Integer.parseInt(commands[commands.length-1]);
			shapeType = commands[commands.length-3];
			color = new Color(Integer.parseInt(commands[commands.length-2]));
		}
		//handles specific freehand drawing functionality
		else if(commands[0].equals("a") && commands.length>8) { //length greater than 8 because freehand is much longer
			shapeType = commands[commands.length-3];
			id = Integer.parseInt(commands[commands.length-1]);
			color = new Color(Integer.parseInt(commands[commands.length-2]));
			//instantiate arrayList of segments - represents the freehand drawing
			freehand = new ArrayList<Segment>();
			for(int i = 1; i<commands.length-7;i+=7) {
				//adds each segment in the message to the list
				Segment s1 = new Segment(Integer.parseInt(commands[i]), Integer.parseInt(commands[i+1]), Integer.parseInt(commands[i+2]), Integer.parseInt(commands[i+3]), new Color(Integer.parseInt(commands[commands.length-1])));
				freehand.add(s1);
			}
		}
		//handles specific functionality for deleting
		else if(commands[0].equals("d"))
			id = Integer.parseInt(commands[1]);
	}
	//getters and setters....
	
	public String getShapeType() {
		return shapeType;	//this returns the shapeType LOL
	}
	
	public int getDrawFromX(){
		return drawFromX;
	}

	public int getDrawFromY(){
		return drawFromY;
	}
	public int getDrawToX(){
		return drawToX;
	}
	public int getDrawToY(){
		return drawToY;
	}
	public int getId(){
		return id;
	}
	public int getDx(){
		return dx;
	}
	public int getDy() {
		return dy;
	}
	
	public Color getColor() {
		return color;
	}
	public String getCommand() {
		return command;
	}
	public List<Segment> getFreehand(){
		return freehand;
	}
	public void setID(int i){
		this.id = i;
	}
}
