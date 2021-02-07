package bottlecapdetection.model.color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Colors {

	private List<Color> colors = new ArrayList<>();
	
	/**
	 * The following methods return the difference of red/green/blue color values.
	 * The maxima (first and last entry) are ignored.
	 * @return The difference of a color channel
	 */
	
	public float getRedDiff() {
		List<Integer> reds = new ArrayList<>();
		for (Color color : colors) {
			reds.add((int)color.getRed());
		}
		Collections.sort(reds);
		
		if (reds.size() == 1) return reds.get(0);
		if (reds.size() == 2) return reds.get(1) - reds.get(0);
		if (reds.size() == 3) return reds.get(1);
		return reds.get(reds.size() - 2) - reds.get(1);
	}
	
	public float getGreenDiff() {
		List<Integer> green = new ArrayList<>();
		for (Color color : colors) {
			green.add((int)color.getGreen());
		}
		Collections.sort(green);
		
		if (green.size() == 1) return green.get(0);
		if (green.size() == 2) return green.get(1) - green.get(0);
		if (green.size() == 3) return green.get(1);
		return green.get(green.size() - 2) - green.get(1);
	}
	
	public float getBlueDiff() {
		List<Integer> blues = new ArrayList<>();
		for (Color color : colors) {
			blues.add((int)color.getBlue());
		}
		Collections.sort(blues);
		
		if (blues.size() == 1) return blues.get(0);
		if (blues.size() == 2) return blues.get(1) - blues.get(0);
		if (blues.size() == 3) return blues.get(1);
		return blues.get(blues.size() - 2) - blues.get(1);
	}
	
	public void add(Color color) {
		colors.add(color);
	}
	
}
