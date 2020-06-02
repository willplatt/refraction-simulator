package RefractionSim;
import java.awt.Color;

/**
 * Class for 3-D objects which act as a transmission medium for light
 * @author William Platt
 *
 */
public class Target extends Object3D {
	
	private int materialID;
	private String shape;
	
	/**
	 * Constructor for the Target class
	 * @param shape the shape of the target object from the selection of primitive geometries
	 * @param color the overall colour of the object including transparency
	 * @param materialID the index of the material of the object in the viewport's materials list
	 */
	public Target(Mesh.Primitive shape, Color color, int materialID) {
		super(new Mesh(shape), color); // Call the Object3D constructor
		this.shape = shape.toString(); // Store the name of the material
		this.materialID = materialID; // Store the index that identifies the material of the target
	}
	
	/**
	 * Returns the index of the target's material in the viewport's list of materials
	 * @return the index of the target's material
	 */
	public int getMaterial() {
		return materialID;
	}
	
	/**
	 * Sets the material to that referenced by the materialID parameter
	 * @param materialID the index of the material the target is to be changed to
	 */
	public void setMaterial(int materialID) {
		this.materialID = materialID;
	}
	
	/**
	 * Returns the name of the shape of the target
	 * @return the name of the target's shape
	 */
	public String getShape() {
		return shape;
	}
	
}
