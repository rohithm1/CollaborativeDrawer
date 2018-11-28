import java.util.ArrayList;
import java.util.*;

import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @edits Author: Rohith Mandavilli and Sanjana Goli - for Problem set 6
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address
 
	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color
	private String msg = "";

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	private Messages m = null;
	private Point temp = null;


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		m = new Messages();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();


		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease(event.getPoint());
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		for(Integer i: sketch.getMap().keySet()) {
			sketch.getMap().get(i).draw(g);
		}

	}

	// Helpers for event handlers
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		curr = sketch.findShape(p); //if the mode is not draw, the first if statement will neve be triggered anyway
		msg = "";
		if (mode == Mode.DRAW) {
			// Start a new shape
			if(shapeType.equals("ellipse")) {
				curr = new Ellipse(p.x, p.y, color);
			}
			else if(shapeType.equals("rectangle")) {
				curr = new Rectangle(p.x, p.y, color);
			}
			else if(shapeType.equals("freehand")) {
				curr = new Polyline(p.x, p.y, color);
			}
			else if(shapeType.equals("segment")) {
				curr = new Segment(p.x, p.y, color);
			}
			drawFrom = p;
			msg += "a";	//add command
			//add it to the sketch here to see the user actually draw the shape
			sketch.addSketch(-1, curr);
			repaint();
		}
		else if (curr != null && curr.contains(p.x, p.y)) {
			if (mode == Mode.RECOLOR) {
				// Recolor the shape
				msg += "r " + color.getRGB() + " ";
				repaint();
			}
			else if (mode == Mode.MOVE) {
				// Starting to drag
				moveFrom = p;
				temp = p;
				msg += "m ";
				//if the point is not in the shape, stop moving it
				if(!curr.contains(p.x, p.y)) moveFrom = null;
			}
			else if (mode == Mode.DELETE) {
				// Get rid of the shape
				msg += "d " + sketch.getID(curr) + " " + color.getRGB();	//construct full message for delete
				//take out of the map of shapes
				sketch.getMap().remove(sketch.getID(curr));
				repaint();
			}
		}		
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		//we have to change moving id
		movingId = sketch.getID(curr);
		if (mode == Mode.DRAW) {
			if(shapeType.equals("ellipse") || shapeType.equals("rectangle"))
				curr.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			if(shapeType.equals("freehand")) {
				curr.setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
				drawFrom = p;
			}
			//need to set end rather than set corners so the point you are drawing from doesnt change
			if(shapeType.equals("segment"))
				((Segment)curr).setEnd(p.x, p.y);
			repaint();
		}
		else if (mode == Mode.MOVE && moveFrom != null && movingId != -1) {
			// Drag it
			moveFrom = p;
			repaint();
		}
	}
	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease(Point p) {
		// TODO: YOUR CODE HERE
		if(mode == mode.DRAW) {
			msg+= curr.toString(); //call the toString of the object - in same format as before
		}
		if(mode == mode.RECOLOR) {
			movingId = sketch.getID(curr);
			msg+= movingId;
		}
		if(mode == mode.MOVE && moveFrom != null) 
			msg+= (p.x - temp.x) + " " + (p.y - temp.y) + " " +color.getRGB()  + " " + movingId;
		moveFrom = null;
		movingId = -1;
		curr = null;
		//send to the server
		comm.send(msg);
	}

	/**
	 * drawer handles the parsing of new lines printed to the client
	 * @param line this is the line printed to the client from the server
	 */
	public void drawer(String line) {
		m.parseString(line);
		//handles adding - each shape uses the same functional pattern
		if(m.getCommand().equals("a")) {
			if(m.getShapeType().equals("ellipse")) {
				Shape s = new Ellipse(m.getDrawFromX(), m.getDrawFromY(), m.getDrawToX(), m.getDrawToY(), m.getId(), m.getColor());
				sketch.addSketch(m.getId(), s);
				sketch.deleteShape(-1);//delete shape added in the handle press methods
			}
			else if(m.getShapeType().equals("rectangle")) {
				Shape s = new Rectangle(m.getDrawFromX(), m.getDrawFromY(), m.getDrawToX(), m.getDrawToY(), m.getId(), m.getColor());
				sketch.addSketch(m.getId(), s);
				sketch.deleteShape(-1);
			}
			else if(m.getShapeType().equals("segment")) {
				Shape s = new Segment(m.getDrawFromX(), m.getDrawFromY(), m.getDrawToX(), m.getDrawToY(), m.getId(), m.getColor());
				sketch.addSketch(m.getId(), s);
				sketch.deleteShape(-1);
			}
			else if(m.getShapeType().equals("freehand")) {
				Polyline p = new Polyline(m.getId(), m.getColor());	//construct polyline to be added
				//loop through each segment
				for(Segment seg: m.getFreehand()) {
					//create a new segment for each arraylist index
					Shape s = new Segment(seg.getX(), seg.getY(), seg.getX2(), seg.getY2(), seg.getID(), seg.getColor());
					p.addSegment(seg);
				}
				//updates the color of the outputted shape to the user selected color
				p.setColor(color);
				sketch.addSketch(m.getId(), p);
				sketch.deleteShape(-1);
			}
		}
		//handles moving shapes
		if(m.getCommand().equals("m") && sketch.getID(curr) != m.getId()) {
			sketch.move(m.getId(), m.getDx(), m.getDy());	//makes sure only moves the shape with that specific id

		}
		//handles recoloring
		if(m.getCommand().equals("r")) {
			sketch.changeColor(m.getId(), m.getColor()); 
		}
		//handles deleting
		if(m.getCommand().equals("d") && sketch.getID(curr) != m.getId()){
			sketch.deleteShape(m.getId());
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
