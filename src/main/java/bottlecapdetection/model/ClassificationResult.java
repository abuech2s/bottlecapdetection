package bottlecapdetection.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class ClassificationResult {

	@Setter @Getter private int bottleCapFaceUpCnt = 0;
	@Setter @Getter private int bottleCapFaceDownCnt = 0;
	@Setter @Getter private int bottleCapDeformedCnt = 0;
	@Setter @Getter private int DistractorCnt = 0;
	
	public int totalCnt() {
		return bottleCapDeformedCnt + bottleCapFaceDownCnt + bottleCapFaceUpCnt + DistractorCnt;
	}
	
	//We did not count and label distractors in json file by LabelMe - therefore for an analysis we have to compare the total
	// number of object without counting distractors
	public int totalCntWithoutDistractors() {
		return bottleCapDeformedCnt + bottleCapFaceDownCnt + bottleCapFaceUpCnt;
	}
	
}
