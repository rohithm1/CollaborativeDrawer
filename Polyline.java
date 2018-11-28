import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @edit Rohith Mandavilli and Sanjana Goli added more constructors
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	private List<Segment> segments;
	private Color color;
	private int id;
	
	public Polyline(int i, Color c) {
		id = i;
		color = c;
		segments = new ArrayList<Segment>();
	}
	public Polyline(int x1, int y1, Color color) {
		segments = new ArrayList<Segment>();
		Segment s = new Segment(x1, y1, x1, y1, color);
		this.color = color;
	}
	
	public Polyline(int x1, int y1, int id, Color color) {
		segments = new ArrayList<Segment>();
		Segment s = new Segment(x1, y1, x1, y1, color);
		this.color = color;
		this.id = id;
	}
	
	public Polyline(int x1, int y1, int x2, int y2, Color color) {
		segments = new ArrayList<Segment>();
		Segment s = new Segment(x1, y1, x2, y2, color);
		segments.add(s);
	}
	
	public Polyline(int x1, int y1, int x2, int y2, int id, Color color) {
		segments = new ArrayList<Segment>();
		Segment s = new Segment(x1, y1, x2, y2, color);
		segments.add(s);
		this.id = id;
	}
	
	@Override
	public void moveBy(int dx, int dy) {
		for(Segment s : segments) {
			s.moveBy(dx, dy);
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		for(Segment s: segments)
			s.setColor(color);
	}
	
	@Override
	public boolean contains(int x, int y) {
		for(Segment s : segments) {
			if(s.contains(x, y)) return true;
		} 
		return false;
	}

	public void setCorners(int x1, int y1, int x2, int y2) {
		Segment s = new Segment(x1, y1, x2, y2, color);
		segments.add(s);
	}
	
	@Override
	public void draw(Graphics g) {
		for(Segment s : segments) {
			s.draw(g);
		}
	}
	public void addSegment(Segment s) {
		segments.add(s);
	}
	//when you are making the comamnd - make sure there is no space after the a
	@Override
	public String toString() {
		String string = "";
		for(Segment s : segments) {
			string += s.toString();
		}
		string += " freehand " +  color.getRGB() + " " + id;
		return string;
	}
	public int getID() {
		return id;
	}
}
