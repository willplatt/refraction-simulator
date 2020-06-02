package RefractionSim;

/**
 * Class for edges (line segments) in two dimensions
 * @author William Platt
 *
 */
public class Edge2D {
	
	private double x0; // x co-ordinate of the lower point
	private double y0; // y co-ordinate of the lower point
	private double x1; // x co-ordinate of the higher point
	private double y1; // y co-ordinate of the higher point
	private double height; // Positive difference between the y co-ordinates of the two points
	
	/**
	 * Constructor for the Edge2D class
	 * @param x0 the x co-ordinate of the first end of the edge
	 * @param y0 the y co-ordinate of the first end of the edge
	 * @param x1 the x co-ordinate of the second end of the edge
	 * @param y1 the y co-ordinate of the second end of the edge
	 */
	public Edge2D(double x0, double y0, double x1, double y1) {
		if (y0 < y1) {
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
			this.height = y1 - y0; // Height is positive
		} else { // Reverse the points so that (x0, y0) is lower than (x1, y1)
			this.x0 = x1;
			this.y0 = y1;
			this.x1 = x0;
			this.y1 = y0;
			this.height = y0 - y1; // Height is positive
		}
	}
	
	/**
	 * Returns the x co-ordinate of the lower end of the edge
	 * @return the x co-ordinate of the lower of the two end points of the edge
	 */
	public double getX0() {
		return x0;
	}
	
	/**
	 * Returns the x co-ordinate of the higher end of the edge
	 * @return the x co-ordinate of the higher of the two end points of the edge
	 */
	public double getX1() {
		return x1;
	}
	
	/**
	 * Returns the y co-ordinate of the lower end of the edge
	 * @return the y co-ordinate of the lower of the two end points of the edge
	 */
	public double getY0() {
		return y0;
	}
	
	/**
	 * Returns the y co-ordinate of the higher end of the edge
	 * @return the y co-ordinate of the higher of the two end points of the edge
	 */
	public double getY1() {
		return y1;
	}
	
	/**
	 * Returns the vertical height of the edge
	 * @return the y component of the edge's length
	 */
	public double getHeight() {
		return height;
	}
	
}
