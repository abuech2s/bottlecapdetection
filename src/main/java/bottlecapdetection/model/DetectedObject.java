package bottlecapdetection.model;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point2f;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RotatedRect;
import bottlecapdetection.Constants;
import bottlecapdetection.logic.ObjectIdProvider;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a detected object in the ROI. It hold several feature attributes
 * and offers them by Setter/Getter methods.
 * 
 * The final allocation to one of the predefined classes is stored in the type variable.
 * 
 * @author Alexander Büchel
 *
 */

public class DetectedObject {
	
	//Id
	@Getter private int id;

	//Original contours
	@Setter @Getter private Mat contours;
	
	//Calculated Convex hull
	@Setter @Getter private Mat convexHull;
	
	//Bounding Box and centerPoint calculated based on boundingBox
	@Setter @Getter private Rect boundingBox;
	@Setter @Getter double centerX;
	@Setter @Getter double centerY;
	
	//Rotated Rectangle
	@Setter @Getter private RotatedRect minAreaRect;
	
	//Minimum Enclosing Circle
	@Setter @Getter private Point2f centerPoint;
	@Setter @Getter private double radius; 
	
	//Areasize
	@Setter @Getter private double areaSize;
	
	//Approximation curve
	@Setter @Getter private Mat polyPoints;
	
	//Detections - isOuterObject/isInnerObject is used to have a reference from inner objects to outer objects and vice versa (reference is given by foreignkey)
	@Setter @Getter private boolean isCircle;
	@Setter @Getter private boolean isDeformed;
	@Getter @Setter private boolean isOuterObject = false;
	@Getter @Setter private boolean isInnerObject = false;
	@Getter @Setter private int outerForeignId = -1;
	@Getter @Setter private int innerForeignId = -1;
	
	@Setter private float redDiff = Float.MAX_VALUE;
	@Setter private float greenDiff = Float.MAX_VALUE;
	@Setter private float blueDiff = Float.MAX_VALUE;
	
	//Max side length
	@Getter @Setter private double maxSideLength;
	
	//Classification
	@Setter @Getter ClassificationType type = ClassificationType.UNKNOWN;
	
	public DetectedObject() {
		id = ObjectIdProvider.getNewId();
	}
	
	public MatVector getContourAsMatVector() {
		MatVector mv = new MatVector(contours);
		return mv;
	}
	
	public MatVector getPolyPointsAsMatVector() {
		MatVector mv = new MatVector(polyPoints);
		return mv;
	}
	
	public MatVector getConvexHullAsMatVector() {
		MatVector mv = new MatVector(convexHull);
		return mv;
	}
	
	@Override
	public String toString() {
		return "(x=" + (int)minAreaRect.center().x() + ", y=" +(int)minAreaRect.center().y() + ") : " + getType();
	}
	
	/**
	 * We override the abstract equals method from class Object.
	 */
	
	@Override
	public boolean equals(Object o) {
        if (o == this) { 
            return true; 
        } 
  
        if (!(o instanceof DetectedObject)) { 
            return false; 
        } 
          
        DetectedObject object = (DetectedObject) o; 
        if (object.getId() == getId())  {
        	return true;        	
        } else {
        	return false;
        }
	}
	
	/**
	 * Checks, if a given rectangle is in the boundingbox of this object.
	 * 
	 * @param boundingBox - A new boundingBox
	 * @return true, if it is inside; otherwise false.
	 */
	
	public boolean rectIsInObject(Rect boundingBox, double areaSize) {
		double x = this.boundingBox.x();
		double y = this.boundingBox.y();
		double centerxBB = boundingBox.x() + (boundingBox.width() / 2);
		double centeryBB = boundingBox.y() + (boundingBox.height() / 2);
		
		int sideBuffer = 0;
		
		//log.info("x: {}-{}-{} y: {}-{}-{} A_out = {}, A_in = {}", x, centerxBB, x + this.boundingBox.width(), y, centeryBB, y + this.boundingBox.height(), this.areaSize, areaSize);
		if (centerxBB > x - sideBuffer && centerxBB < x + this.boundingBox.width() + sideBuffer) {
			if (centeryBB > y - sideBuffer && centeryBB < y + this.boundingBox.height() + sideBuffer) {
				if (areaSize < this.areaSize) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Receiving the correct label for writing csv file.
	 * As a backup, the label 'Unknown' is returned, but this case 
	 * should never happened.
	 * 
	 * @return The corresponding label to this object
	 */
	
	public String getLabel() {
		switch(type) {
		case BOTTLECAP_DOWN: return Constants.OUTPUT_LABEL_BC_FACEDOWN;
		case BOTTLECAP_UP: return Constants.OUTPUT_LABEL_BC_FACEUP;
		case BOTTLECAP_DEFORMED: return Constants.OUTPUT_LABEL_BC_DEFORMED;
		case DISTRACTOR: return Constants.OUTPUT_LABEL_DISTRACTOR;
		case UNKNOWN: return Constants.OUTPUT_LABEL_UNKNOWN;
		default: return Constants.OUTPUT_LABEL_UNKNOWN;
		}
	}
	
	public boolean hasUniStructuredSurface() {
		if (redDiff < Constants.COLOR_DIFF && greenDiff < Constants.COLOR_DIFF && blueDiff < Constants.COLOR_DIFF) {
			return true;
		}
		return false;
	}
	
	/**
	 * Static method for creating a new DetectedObject instance.
	 * 
	 * @return A new instance.
	 */
	
	public static DetectedObject createInstance() {
		DetectedObject detObj = new DetectedObject();
		return detObj;
	}
	
}
