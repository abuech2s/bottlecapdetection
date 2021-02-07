package bottlecapdetection.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BottleCapSizeResult {

	@Getter private double avgSize;
	@Getter private int bestPosSlidingWindow;
	
}
