package bottlecapdetection.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MaxSidesObjects {

	@Getter List<Integer> maxSidesList;
	@Getter int nrOfOuterObjects; // useful for weighting bottlecap sizes
	
}
