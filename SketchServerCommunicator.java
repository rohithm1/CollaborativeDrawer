import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @edits Rohith Mandavilli and Sanjana Goli for Problem Set 6
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for
	private Messages m;

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
		m = new Messages();
	}
	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");

			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			//server --> client
			server.broadcast(server.getSketch().toString());

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line;
			//hold id to add at the end
			int id;
			while((line = in.readLine()) != null) {//multiple clients
				m.parseString(line);
				id = m.getId();
				//handles for adding shapes to the sketch
				if(m.getCommand().equals("a")) {
					//only way to update the global id for sketch - represents amount of shapes in the world
					id = server.getSketch().changeID();
					//changes the id of the message to the new generated id
					m.setID(id);
					if(m.getShapeType().equals("ellipse")) {
						Shape s = new Ellipse(m.getDrawFromX(), m.getDrawFromY(), m.getDrawToX(), m.getDrawToY(), id, m.getColor());
						//adds shape with specific id and characteristics
						server.getSketch().addSketch(m.getId(), s);
					}
					else if(m.getShapeType().equals("rectangle")) {
						Shape s = new Rectangle(m.getDrawFromX(), m.getDrawFromY(), m.getDrawToX(), m.getDrawToY(), m.getId(), m.getColor());
						server.getSketch().addSketch(m.getId(), s);
					}
					else if(m.getShapeType().equals("segment")) {
						Shape s = new Segment(m.getDrawFromX(), m.getDrawFromY(), m.getDrawToX(), m.getDrawToY(), m.getId(), m.getColor());
						server.getSketch().addSketch(m.getId(), s);
					}
					else if(m.getShapeType().equals("freehand")) {
						//construct a new polyline to be added to the server
						Polyline p = new Polyline(m.getId(), m.getColor());
						for(Segment seg: m.getFreehand()) {	//for each segment in the polyline message - we add it to the polyline
							p.addSegment(seg);
						}
						server.getSketch().addSketch(m.getId(), p);
					}
					//need to cut the default id out of the message, then replace the default id generated in editor with the new updated one
					line = line.substring(0, line.length()-3) + " " + id;
				}
				//handles moving shapes
				if(m.getCommand().equals("m")) {
					server.getSketch().move(m.getId(), m.getDx(), m.getDy());
				}
				//handles recoloring shapes
				if(m.getCommand().equals("r")) {
					System.out.println("id recolor: " + id);
					server.getSketch().changeColor(m.getId(), m.getColor()); 
				}
				//handles deleting shapes
				if(m.getCommand().equals("d")){
					server.getSketch().deleteShape(m.getId());
				}
				
				server.broadcast("broadcast: ");
				//send the line to everyone
				server.broadcast(line);
			}


			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
