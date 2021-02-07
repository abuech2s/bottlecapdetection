package bottlecapdetection.model.json;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class represents the Shape section by json output of LabelMe.
 * The average coordinate is calculated and stored in avgX and avgY.
 * 
 * @author Alexander Buechel
 *
 */

@Setter @Getter @ToString
public class Shape {

	private String label;
	private String line_color;
	private String fill_color;
	private List<Float> points[];
	
	private int avgX;
	private int avgY;
	
	public void calcAvgPoints() {
		if (points == null) return;
		int x = 0;
		for (List<Float> point : points) {
			x += point.get(0);
		}
		avgX = x / points.length;
		
		int y = 0;
		for (List<Float> point : points) {
			y += point.get(1);
		}
		avgY = y / points.length;
	}

}
