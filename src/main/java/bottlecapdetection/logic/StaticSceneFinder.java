package bottlecapdetection.logic;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

import bottlecapdetection.Constants;
import bottlecapdetection.exceptions.MatException;
import bottlecapdetection.model.StaticSceneResult;

/**
 * This class determines the static scene.
 * 
 * @author Alexander Buechel
 *
 */

public class StaticSceneFinder {
	
	private static final Logger log = LoggerFactory.getLogger(StaticSceneFinder.class);

	/**
	 * This method tries to find the static scene based on a neighboring image comparison.
	 * To reduce the calculation resources, we expect, that the static scene must be inside
	 * the second or third quarter. The given list matObject has the following structure:
	 * 
	 * [null, ..., null, Mat, ..., Mat, null, ... null]
	 * 
	 * We will just iterating over the Mat Objects range.
	 * 
	 * For each neighboring images we calculate the corresponding gray scale images and then
	 * the absolute difference. Based on this difference Mat object, we store the index, with
	 * the smallest difference. This is the point, where we expect the static scene (because 
	 * neighboring images, without any motion etc. in it, do not change).
	 * 
	 * @param matObjects - A list of null entries and Mat Objects.
	 * @return - The Mat object corresponding to the static scene
	 * @throws MatException - In case of invalid data structures.
	 */
	
	public static StaticSceneResult find(List<Mat> matObjects) throws MatException {
		if (matObjects == null || matObjects.isEmpty()) {
			throw new MatException("matObjects equals null or is empty.");
		}
		if (matObjects.size() < Constants.STATICSCENE_MIN_NR_OF_IMG) {
			throw new MatException("We expect at least " + Constants.STATICSCENE_MIN_NR_OF_IMG + " Mat-Objects.");
		}
		
		//detect first non-null entry
		int IdxFirstQuarter = 0;
		for (int i = 0; i < matObjects.size(); i++) {
			if (matObjects.get(i) != null) {
				IdxFirstQuarter = i;
				break;
			}
		}
		
		//detect first null entry (backwards)
		int IdxThirdQuarter = 0;
		for (int i = matObjects.size()-1; i > 0; i--) {
			if (matObjects.get(i) != null) {
				IdxThirdQuarter = i;
				break;
			}
		}
		
		//Store best result
		int bestIdxDiff = IdxFirstQuarter;
		int bestNonZeroCnt = Integer.MAX_VALUE;
		
		List<Integer> bestIndices = new ArrayList<>();

		for (int idx = IdxFirstQuarter; idx < IdxThirdQuarter-1; idx++) {
			Mat gray1 = new Mat();
			cvtColor(matObjects.get(idx), gray1, COLOR_BGR2GRAY);
			
			Mat gray2 = new Mat();
			cvtColor(matObjects.get(idx+1), gray2, COLOR_BGR2GRAY);
			
			Mat diff = new Mat();
			absdiff(gray1, gray2, diff);
			
			int NonZeroCnt = countNonZero(diff);
			if (NonZeroCnt < bestNonZeroCnt) {
				bestIdxDiff = idx;
				bestNonZeroCnt = NonZeroCnt;
				bestIndices.add(idx);
			}
			
			gray1.deallocate();
			gray2.deallocate();
			diff.deallocate();
		}
		
		bestIdxDiff = bestIndices.get((int)(bestIndices.size() / 2)); //Take median
		
		log.info("Found best static scene at idx {}. [Iterate from idx {} to {} of total {} (staticScene at {}%)]", bestIdxDiff, IdxFirstQuarter, (IdxThirdQuarter-1), matObjects.size(), ((int)(bestIdxDiff*10000.0)/matObjects.size())/100.0);
 
		//Free memory of all other Mat-Objects
		for (int i = 0; i < matObjects.size(); i++) {
			if (matObjects.get(i) != null) {
				if (i != bestIdxDiff) {
					matObjects.get(i).deallocate();
				}
			}
		}
		
		return new StaticSceneResult(bestIdxDiff, matObjects.get(bestIdxDiff));
	}
	
}
