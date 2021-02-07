package bottlecapdetection.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Point2f;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RotatedRect;
import org.bytedeco.opencv.opencv_core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bottlecapdetection.Constants;
import bottlecapdetection.model.BinaryResult;
import bottlecapdetection.model.BottleCapSizeResult;
import bottlecapdetection.model.ClassificationType;
import bottlecapdetection.model.DetectedObject;
import bottlecapdetection.model.ObjectDetectionResult;
import bottlecapdetection.model.ROIResult;
import bottlecapdetection.model.color.Color;
import bottlecapdetection.model.color.Colors;
import bottlecapdetection.model.MaxSidesObjects;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;

/**
 * This class contains methods for image processing logic.
 * 
 * @author Alexander Büchel
 *
 */

public class Logic {
	
	private static final Logger log = LoggerFactory.getLogger(Logic.class);
	
	/**
	 * Comparing two pictures based on their number of black pixels.
	 * We expect, that two binary pictures of same size are given. 
	 * 
	 * The return value is calculated based on the following equation:
	 *  return Math.max(nonZeros1, nonZeros2) - Math.min(nonZeros1, nonZeros2)
	 * 
	 * @param pic1 - First picture
	 * @param pic2 - Second picture
	 * @return Difference of nonZeroBased pixels
	 */
	public static long compareImages(Mat pic1, Mat pic2) {
		long nonZeros1 = countNonZero(pic1);
		long nonZeros2 = countNonZero(pic2);

		return Math.max(nonZeros1, nonZeros2) - Math.min(nonZeros1, nonZeros2);
	}
	
	/**
	 * This method calculates a threshold for a binary transformed image.
	 * This value will be in range [75, ..., 150] and is defined as the 
	 * minimum difference (number of non-zero-based pixels) to the value
	 * before.
	 * 
	 * @param mat - An opencv Mat image object (must be gray scale image)
	 * @return the best found threshold
	 */
	public static int findBestThresholdForROI(Mat matGray) {
		int bestThreshold = -1;
		long bestDiffValue = -1;

		Mat last = new Mat();
		threshold(matGray, last, 100, 255, THRESH_BINARY);

		for (int step = 105; step <= 150; step = step + 5) {
			Mat result = new Mat();
			threshold(matGray, result, step, 255, THRESH_BINARY);
			long compRes = Logic.compareImages(last, result);
			last = result;
			// Save currently best result
			if (bestThreshold == -1 || compRes < bestDiffValue) {
				bestDiffValue = compRes;
				bestThreshold = step;
			}
		}
		log.debug("Determine best threshold for ROI: {}", bestThreshold);
		return bestThreshold;
	}
	
	/**
	 * Given a list of contours, this method will return the idx in this list based
	 * on the largest contour area value.
	 * 
	 * @param contours - A list of contours
	 * @return the idx of the largest area
	 */
	public static int findLargestContour(MatVector contours) {
		int idx = -1;
		double areaValue = 0.0;
		for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
			double contourArea = contourArea(contours.get(contourIdx));
			if (contourArea > areaValue) {
				areaValue = contourArea;
				idx = contourIdx;
			}
		}
		return idx;
	}
	
	/**
	 * Transforms an image to a binary image, choosing the best threshold.
	 * 
	 * @param matColor A mat object
	 * @return A BinaryResult - containing the binary image and the threshold
	 */
	
	public static BinaryResult toBinaryImage(Mat matColor) {
	      Mat matGray = new Mat();
	      cvtColor(matColor, matGray, COLOR_BGR2GRAY);
	      GaussianBlur(matGray, matGray, new Size(11, 11), 0);
	      int bestThreshold = Logic.findBestThresholdForROI(matGray);
	      Mat binaryMat = new Mat();
	      threshold(matGray, binaryMat, bestThreshold, 255, THRESH_BINARY);
	      return new BinaryResult(binaryMat, bestThreshold);
	}
	
	public static BinaryResult toBinaryImage(Mat matColor, int threshold) {
	      Mat matGray = new Mat();
	      cvtColor(matColor, matGray, COLOR_BGR2GRAY);
	      GaussianBlur(matGray, matGray, new Size(11, 11), 0);
	      Mat binaryMat = new Mat();
	      threshold(matGray, binaryMat, threshold, 255, THRESH_BINARY);
	      return new BinaryResult(binaryMat, threshold);
	}
	
	/**
	 * This method takes a color image and returns a subimage, which represents 
	 * the ROI. This procedure is based on the following steps: Create gray 
	 * image, create a binary image, apply the canny86 algorithm, find contours 
	 * of objects and takes the largest image as the ROI.
	 * 
	 * @param matColor - A color picture.
	 * @return the subimage representing the ROI. 
	 */
	
	@SuppressWarnings("resource")
	public static ROIResult extractROI(Mat matColor) {
		Mat matGray = new Mat();
		cvtColor(matColor, matGray, COLOR_BGR2GRAY);
		
		GaussianBlur(matGray, matGray, new Size(11, 11), 0);
		int bestThreshold = Logic.findBestThresholdForROI(matGray);
		Mat binaryMat = new Mat();
		threshold(matGray, binaryMat, bestThreshold, 255, THRESH_BINARY);
		
		// Create Canny Image
		Mat cannyMat = new Mat();
		Canny(binaryMat, cannyMat, bestThreshold, bestThreshold * 2);

		Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(5, 5));
		for (int i = 0; i < 10; i++ ) {
			dilate(cannyMat, cannyMat, kernel);
			erode(cannyMat, cannyMat, kernel);
		}

		// Find contours of objects and take the largest contour as the region of interest
		MatVector contours = new MatVector();
		Mat hierarchy = new Mat();
		findContours(cannyMat, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
		int largestIdx = Logic.findLargestContour(contours);
		Mat largestROI = contours.get(largestIdx);
		
		// Crop everything around ROI, the result is an image representing the ROI
		Rect rect = boundingRect(largestROI);
		int extention = Constants.ROI_SIDE_EXTENSION;
		
		// Extract a little bit more, then the roi to prevent cut objects
		Rect largerRect = new Rect(new Point(rect.tl().x() - extention, rect.tl().y() - extention), new Point(rect.br().x() + extention, rect.br().y() + extention));
		Mat roi = new Mat();
		
		try {
			// Try to extract larger Rect
			roi = new Mat(matColor, largerRect);
		} catch (RuntimeException rte) {
			// If this does not work, take original rect
			log.info("Could not extract larger rect. Took original rectangle");
			roi = new Mat(matColor, rect);
		}
		
		boolean tookCompleteImage = false;
		// If roi is too small, take the complete picture
		if (roi.size().width() < 400 || roi.size().height() < 300) {
			roi.deallocate();
			roi = new Mat(matColor);
			tookCompleteImage = true;
		}
		
		matGray.deallocate();
		cannyMat.deallocate();
		contours.deallocate();
		
		return new ROIResult(roi, binaryMat, tookCompleteImage);
	}
	
	/**
	 * This method localizes given objects on a region of interest.
	 * Therefore it uses findContours() on the image and returns a list
	 * of DetectedObjects. For each detected object, additional information
	 * are calculated, e.g. BoundingBox, MinimumCircle, centerPoint etc.
	 * 
	 * @param roi - The Mat-Object representing ROI
	 * @return A ObjectDetectionResult - this includes the list detected objects
	 */
	
	public static ObjectDetectionResult localizeAndClassifyObjects(Mat roi) {
		
		//Finally store everything in this object
		ObjectDetectionResult odr = new ObjectDetectionResult();
		odr.setRoi(roi);
		
		// *************** LOCALIZATION ****************
		
		//Save best result - with the most objects, after filtering useless objects
		int foundMostObjects = 0;
		MatVector bestContours = null;
		int bestThreshold = 0;
		
		int filteredObjects = 0;
		
		for (int threshold = 100; threshold <= 160; threshold += 2) {
		    BinaryResult binaryResult = Logic.toBinaryImage(roi, threshold);
			MatVector contours = new MatVector();
			Mat hierarchy = new Mat();
			findContours(binaryResult.getBinaryImage(), contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);

			for (int idx = 0; idx < contours.size(); idx++) {
				double areaSize = contourArea(contours.get(idx));
	            if (areaSize < Constants.ANALYSIS_OBJECT_MINIMUM_AREA || areaSize > Constants.ANALYSIS_OBJECT_MAXIMUM_AREA) {
	            	filteredObjects++;
	            }
			}
			
			//Count useful objects - store always best solution (so far)
			if ((contours.size() - filteredObjects) >= foundMostObjects) {
				bestContours = contours;
				foundMostObjects =  (int)contours.size() - filteredObjects;
				bestThreshold = threshold;
			}
			filteredObjects = 0;
		}
		odr.setContours(bestContours);
		log.info("Found most useful objects ({}) at threshold {}", foundMostObjects, bestThreshold);
		

		// *************** CALCULATIONS FOR OBJECT ATTRIBUTES ****************
		List<DetectedObject> objects = new ArrayList<>();
        for (int i = 0; i < bestContours.size(); i++) {
        	Mat contour = bestContours.get(i);
            Mat points = new Mat();
            approxPolyDP(contour, points, arcLength(contour, true) * 0.02, true);
             
            //Calculate minEclosingCircle + radius + centerPoint
            Point2f point2f = new Point2f();
            float[] radius = new float[1];
            minEnclosingCircle(bestContours.get(i), point2f, radius);
            
            //Calculate AreaSize
            double areaSize = contourArea(contour);
           
            //Calculate convexHull
            Mat convexHull = calculateConvexHull(contour);
            
            //Calculate BoundingBox
            Rect boundingBox = boundingRect(points);
            
            //Calculate minAreaRect
            RotatedRect minAreaRect = minAreaRect(convexHull);
            
            //Calculate, if object is circle
            boolean isCircle = isCircle(minAreaRect);
            
            //Calculate, if object is deformed (possible candidate)
            boolean isDeformed = isDeformed(minAreaRect);
            
            //Calculate max side
            double maxSide = getMaxSide(minAreaRect);
            
            //Determine Colors on Surface
            //Example found at http://bytedeco.org/news/2014/12/23/third-release/
            Mat convexHullImg = new Mat(roi, boundingBox);
            UByteRawIndexer rgbaIdx = convexHullImg.createIndexer();
            int stepX = Math.max(1, (convexHullImg.size().width() / 10));
            int stepY = Math.max(1, (convexHullImg.size().height() / 10));
            Colors colors = new Colors();
            //Iterate left to right - line
            for (int x = 0; x < convexHullImg.size().width(); x = x + stepX) {
            	int y = convexHullImg.size().height() / 2;
            	float b = rgbaIdx.get(y, x, 2);
                float g = rgbaIdx.get(y, x, 1);
                float r = rgbaIdx.get(y, x, 0);
                colors.add(new Color(r, g, b));
            }
            
            //Iterate top to bottom - line 
        	for (int y = 0; y < convexHullImg.size().height(); y = y + stepY) {
        		int x = convexHullImg.size().width() / 2;
        		float b = rgbaIdx.get(y, x, 2);
                float g = rgbaIdx.get(y, x, 1);
                float r = rgbaIdx.get(y, x, 0);
                colors.add(new Color(r, g, b));
            }
        	convexHullImg.close();
        	rgbaIdx.close();
        	
            // *************** FILTERING ****************
            
            //Filter objects, which are obviously too small
            if (areaSize < Constants.ANALYSIS_OBJECT_MINIMUM_AREA || areaSize > Constants.ANALYSIS_OBJECT_MAXIMUM_AREA) {
            	filteredObjects++;
            	continue;
            }
            
            //Filter objects, which are obviously too large
            //Here we define, that each object, which has a width or height > (1/s of roi.height/width) is too large
            if (boundingBox.width() > (Constants.ANALYSIS_ROI_RELATION_SIZE * roi.cols()) || boundingBox.height() > (Constants.ANALYSIS_ROI_RELATION_SIZE * roi.rows())) {
            	filteredObjects++;
            	continue;
            }
            
            //Filter objects, which are very long rectangle, which may appear at the sides of the ROI.
            //Here we define, that each object, which has a width or height < x px and the other side must be at least x times larger
            if (boundingBox.width() < Constants.ANALYSIS_FILTER_MINSIZE && (boundingBox.height() >= Constants.ANALYSIS_FILTER_SIDEFACTOR * boundingBox.width()) || 
            		boundingBox.height() < Constants.ANALYSIS_FILTER_MINSIZE && (boundingBox.width() >= Constants.ANALYSIS_FILTER_SIDEFACTOR * boundingBox.height())) {
            	filteredObjects++;
            	continue;
            }
            
            //we eliminate all objects detected at all 4 sides.
            if (boundingBox.y() < 100 || boundingBox.y() > roi.size().height() - 100 || boundingBox.x() < 100 || boundingBox.x() > roi.size().width() - 100) {
            	filteredObjects++;
            	continue;
            }
            
            // ************ COLLECTING AND STORING RESULTS **********
            
            //If they are not filtered out, lets collect all attributes and store the detected object.
            DetectedObject detObj = DetectedObject.createInstance();
            detObj.setRedDiff(colors.getRedDiff());
            detObj.setBlueDiff(colors.getBlueDiff());
            detObj.setGreenDiff(colors.getGreenDiff());
            detObj.setContours(contour);
            detObj.setConvexHull(convexHull);
            detObj.setBoundingBox(boundingBox);
            detObj.setCenterX(boundingBox.x() + boundingBox.size().width() / 2);
            detObj.setCenterY(boundingBox.y() + boundingBox.size().height() / 2);
            detObj.setMinAreaRect(minAreaRect);
            detObj.setCenterPoint(point2f);
            detObj.setRadius(radius[0]);
            detObj.setAreaSize(areaSize);
            detObj.setPolyPoints(points);
            detObj.setCircle(isCircle);
            detObj.setDeformed(isDeformed);
            detObj.setMaxSideLength(maxSide);
            
            objects.add(detObj);
        }
        log.info("Filtered objects (based on size conditions): {}", filteredObjects);
        
		//We know, that there are maximal 15 objects: Eliminate always smallest -> we expect, that small objects are distractors
        int eliminatedCnt = 0;
        while (objects.size() > 15) {
        	double smallestAreaSize = getSmallestAreaSize(objects);
        	objects.removeIf(o -> o.getAreaSize() == smallestAreaSize);
        	
        	eliminatedCnt++;
        }
        log.info("Eliminated objects (because we found too many): {}", eliminatedCnt);

        // ************ CALCULATING FOR GENERAL FEATURES **********
        
        //Distinguish between outer and inner objects
        objects = detectRelationsOfObjects(objects);
        
        //Calculate average of expected bottle cap side length
        BottleCapSizeResult bcsr = calculateExpectedBottleCapSize(objects, Constants.ANALYSIS_SLIDING_WINDOW_SIZE);
        odr.setExpectedBottleCapSize(bcsr.getAvgSize());
        
        //Perform classification
        objects = classifyObjects(objects, Constants.ANALYSIS_SLIDING_WINDOW_SIZE, (int)bcsr.getAvgSize());

        odr.setObjects(objects);
        return odr;
	}
	
	private static double getSmallestAreaSize(List<DetectedObject> objects) {
		double areasize = Double.MAX_VALUE;
		for (DetectedObject detObj : objects) {
			if (detObj.getAreaSize() < areasize) {
				areasize = detObj.getAreaSize();
			}
		}
		return areasize;
	}
	
	/**
	 * Here, the expected bottle cap length is calculated. Therefore we iterate over all objects
	 * in the given list of objects, create a sliding window of size s in [px] and move this window along 
	 * all objects max side lengths.
	 * For each window position - defined from minPos until maxPos with center c - we determine
	 * all objects, which's max side is in this range and calculate a weighted and squared point value for 
	 * this window.
	 * 
	 * Be l_1 to l_n the list l of objects in this range, then a point value p is defined as:
	 * 
	 * c = minPos + 0.5 * windowSize
	 * p = sum_{i=1}^{n} Math.pow(Math.abs(c - l_i.maxSide), 2)
	 * 
	 * where i is in {1, ..., n} of the current list of objects in this range and which must 
	 * be a separate object.
	 * 
	 * Example: s=18px, minPos=50px and current objects (sorted by their maxSides) are 
	 * L = {51, 62, 73, 84} then we have a current sliding windows from minPos=50 until 
	 * maxPos = minPos + c = 68 and a center of c = minPos + 0.5 * windowSize = 50 + 0.5 * 18 = 59. 
	 * Then list of objects l in this range contains the maximal sides of {51, 62}.
	 * 
	 * p = sum_{i=1}^{2} (c - l_i)^2 = (59-51)^2 + (59-62)^2 = 64 + 9 = 73.
	 * 
	 * While moving this window, we store just the global minimum value p and calculate
	 * the average max side length of all objects l_i in l.
	 * 
	 * The global assumption here is, the most of all objects are bottle caps (it does not matter
	 * if they are faceUp, faceDown or Deformed).
	 * 
	 * @author Alexander Buechel
	 * @param objects - The list of objects
	 * @return the calculates expected max length of a bottle cap
	 */
	
	private static BottleCapSizeResult calculateExpectedBottleCapSize(List<DetectedObject> objects, int windowSize) {
		int minValueOfRange = (int)Collections.min(objects, Comparator.comparing(x -> x.getMaxSideLength())).getMaxSideLength();
		int maxValueOfRange = (int)Collections.max(objects, Comparator.comparing(x -> x.getMaxSideLength())).getMaxSideLength();

		//Store best optValue while iterating
		double bestOptValue = Double.MAX_VALUE; // Best value = Minimum value; therefore we begin with a large value
		int mostObjectCount = 0;
		double avgSide = 0.0;
		int bestPos = minValueOfRange;

		// Iterate over range [left...center...right] with sliding window
		for (int pos = minValueOfRange; pos <= maxValueOfRange; pos++) {
			int l = pos;
			int c = (int)(pos + 0.5 * windowSize);
			int r = pos + windowSize;

			MaxSidesObjects wo = extractObjectsBasedOnMaxSides(objects, l, r);
			List<Integer> maxSidesOfCoveredObjects = wo.getMaxSidesList();

			double optValue = 0.0;
			for (Integer i : maxSidesOfCoveredObjects) {
				optValue += Math.pow(Math.abs(c - i), 2);
			}
			optValue = optValue / maxSidesOfCoveredObjects.size();

			// Store current best solution
			// Store new best result, if (1) we found more objects or (2) the same number, but better optimization value
			// This procedure includes a weighting based of outer objects
			if ((maxSidesOfCoveredObjects.size() + wo.getNrOfOuterObjects() > mostObjectCount) || (maxSidesOfCoveredObjects.size() + wo.getNrOfOuterObjects() == mostObjectCount && optValue <= bestOptValue)) {
				bestOptValue = optValue;
				bestPos = pos;
				mostObjectCount = maxSidesOfCoveredObjects.size() + wo.getNrOfOuterObjects();
				avgSide = 0.0;
				for (Integer i : maxSidesOfCoveredObjects) {
					avgSide += i;
				}
				avgSide = avgSide / maxSidesOfCoveredObjects.size();
			}
			
		}
		log.info("Return as expected bottle cap size in [px]: {} based on optimization problem value = {} with {} objects", avgSide, bestOptValue, mostObjectCount);
		return new BottleCapSizeResult(avgSide, bestPos);
	}
	
	/**
	 * This method extracts a subgroup of objects from all objects, which are covered by the sliding window.
	 * Furthermore it determine those objects, which has inner objects, because they are a strong argument for
	 * being a bottlecap - the inner ring of the cap is often determined as well.
	 * 
	 * @param objects All objects
	 * @param minRange left value of the range
	 * @param maxRange right value of the range
	 * @return A WeightedObjects objects
	 */
	private static MaxSidesObjects extractObjectsBasedOnMaxSides(List<DetectedObject> objects, int minRange, int maxRange) {
		List<Integer> maxSides = new ArrayList<>();
		int nrOfweightedObjects = 0;
		for (DetectedObject dob : objects) {
			if (dob.getMaxSideLength() >= minRange && dob.getMaxSideLength() <= maxRange) {
				maxSides.add((int)dob.getMaxSideLength());
				if (dob.isInnerObject() && dob.isCircle()) {
					nrOfweightedObjects++;
				}
			}
		}
		return new MaxSidesObjects(maxSides, nrOfweightedObjects);
	}
	
	/**
	 * Calculates the convex hull based on contour points
	 * 
	 * @param contour - The given contour
	 * @return A Mat object containing the convex hull
	 */
	
	private static Mat calculateConvexHull(Mat contour) {
        Mat hull = new Mat();
        convexHull(contour, hull, false, true);
        return hull;
	}
	
	/**
	 * 
	 * Checks, if an object is nearly a circle. In this case, we define a circle, if the ratio of the two sides
	 * are within a specific procentual range.
	 * 
	 * @param rotatedRect - The bounding box
	 * @return true, in case that the algorithm defines a given object as a circle; otherwise false
	 */
	
	private static boolean isCircle(RotatedRect rotatedRect) {
		return isCircle(rotatedRect, 0.0);
	}
	
	//We use a punishment to make the circularity condition stronger
	private static boolean isCircle(RotatedRect rotatedRect, double punishment) {
		float largeSide = Math.max(rotatedRect.size().width(), rotatedRect.size().height());
		float shortSide = Math.min(rotatedRect.size().width(), rotatedRect.size().height());
		double ratio = shortSide / largeSide; // [0, ..., 1]
		if (ratio >= Constants.ANALYSIS_BC_COMPLETE_MIN_RATIO + punishment) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the longer side of the minimum enclosing rotated rectangular
	 * 
	 * @param The minimum area rectangle
	 * @return The length of the longer side
	 */
	private static double getMaxSide(RotatedRect minAreaRect) {
		return Math.max(minAreaRect.size().width(), minAreaRect.size().height());
	}
	
	/**
	 * Checks, if a given object has the attributes to be a possible candidate for a deformed bottle cap.
	 * a
	 * @param rotatedRect - the calculated rotated rectangle of a given object
	 * @return true, is the algorithm defines this object as a possible candidate to be a deformed bottle cap; otherwise false
	 */
	
	private static boolean isDeformed(RotatedRect rotatedRect) {
		float largeSide = Math.max(rotatedRect.size().width(), rotatedRect.size().height());
		float shortSide = Math.min(rotatedRect.size().width(), rotatedRect.size().height());
		double ratio = shortSide / largeSide; // [0, ..., 1]
		if (ratio >= Constants.ANALYSIS_BC_DEFORMED_MIN_RATIO && ratio < Constants.ANALYSIS_BC_DEFORMED_MAX_RATIO) {
			return true;
		}
		return false;
	}
	
	private static List<DetectedObject> detectRelationsOfObjects(List<DetectedObject> objects) {
		int relationCount = 0;
		for (int i = 0; i < objects.size(); i++) { // DetectedObject o : objects
			DetectedObject o = objects.get(i);
			for (int j = 0; j < objects.size(); j++) { // DetectedObject j : objects
				DetectedObject o2 = objects.get(j);
				if (!o.equals(o2)) {
					if (o2.rectIsInObject(o.getBoundingBox(), o.getAreaSize())) {
						
						objects.get(i).setInnerObject(true);
						objects.get(i).setOuterForeignId(o2.getId());	//Connect the parent and the child object.
						
						objects.get(j).setOuterObject(true);
						objects.get(j).setInnerForeignId(o.getId());	//Connect the parent and the child object.
						
						relationCount++;
					}
				}
			}
		}
		log.debug("Detected inner/outer relations: {}", relationCount);
		return objects;
	}
	
	/**
	 * Classifies the objects in a given list. It sets the classificationType
	 * 
	 * @see bottlecapdetection.model.ClassificationType
	 * @param detectedObjects - The list of objects
	 * @return The same list, containing set attributes for the Classification
	 */
	
	private static List<DetectedObject> classifyObjects(List<DetectedObject> detectedObjects, int windowSize, int avgSize) {
		for (DetectedObject detObj : detectedObjects) {
			if (isCoveredByExtendedWindow(detObj, windowSize, avgSize)) {
				if (detObj.isCircle()) {
					detObj.setType(ClassificationType.BOTTLECAP);
				} else if (detObj.isDeformed()) {
					detObj.setType(ClassificationType.BOTTLECAP_DEFORMED);
				} else {
					detObj.setType(ClassificationType.DISTRACTOR);
				}
			}
		}
		
		for (DetectedObject detObj : detectedObjects) {
			if (detObj.getType() == ClassificationType.UNKNOWN) {
				log.debug("Unknown object -> Distrator: {}", detObj);
				detObj.setType(ClassificationType.DISTRACTOR);
			}
		}
		
		//Eliminate inner objects with (deformed) BottleCap-Type if outer object is BottleCap as well
		for (int i = 0; i < detectedObjects.size(); i++) { // DetectedObject o : objects
			DetectedObject o = detectedObjects.get(i);
			for (int j = 0; j < detectedObjects.size(); j++) { // DetectedObject j : objects
				DetectedObject o2 = detectedObjects.get(j);
				if (!o.equals(o2)) {
					if (o2.rectIsInObject(o.getBoundingBox(), o.getAreaSize())) {
						if (o2.getType() == ClassificationType.BOTTLECAP || o2.getType() == ClassificationType.BOTTLECAP_DEFORMED) {
							detectedObjects.get(i).setType(ClassificationType.INNER_OBJECT);
						}
					}
				}
			}
		}
		
		//Eliminate outer objects with Distractor-Type if inner object is BottleCap
		for (int i = 0; i < detectedObjects.size(); i++) {
			DetectedObject o = detectedObjects.get(i);
			for (int j = 0; j < detectedObjects.size(); j++) {
				DetectedObject o2 = detectedObjects.get(j);
				if (!o.equals(o2)) {
					if (o2.rectIsInObject(o.getBoundingBox(), o.getAreaSize())) {
						if (o.getType() == ClassificationType.BOTTLECAP) {
							detectedObjects.get(j).setType(ClassificationType.IGNORED_OBJECT);
						}
					}
				}
			}
		}
		
		//Eliminate inner object if outer and inner are distractors
		for (int i = 0; i < detectedObjects.size(); i++) {
			DetectedObject o = detectedObjects.get(i);
			for (int j = 0; j < detectedObjects.size(); j++) {
				DetectedObject o2 = detectedObjects.get(j);
				if (!o.equals(o2)) {
					if (o2.rectIsInObject(o.getBoundingBox(), o.getAreaSize())) {
						if (o.getType() == ClassificationType.DISTRACTOR && o2.getType() == ClassificationType.DISTRACTOR) {
							detectedObjects.get(i).setType(ClassificationType.IGNORED_OBJECT);
						}
					}
				}
			}
		}
		
		//Decide between Up and Down BottleCaps
		for (DetectedObject detObj : detectedObjects) {
			if (detObj.getType() == ClassificationType.BOTTLECAP) {
				if (detObj.hasUniStructuredSurface()) {
					detObj.setType(ClassificationType.BOTTLECAP_DOWN);
				} else {
					detObj.setType(ClassificationType.BOTTLECAP_UP);
				}
			}
		}
		
		log.debug("Classification performed.");
		return detectedObjects;
	}
	
	private static boolean isCoveredByExtendedWindow(DetectedObject detObj, int windowSize, int avgSize) {
		int leftBorder = (int) (avgSize - 1 * windowSize);
		int rightBorder = (int) (avgSize + 2.5 * windowSize);
		if (detObj.getMaxSideLength() >= leftBorder && detObj.getMaxSideLength() <= rightBorder) {
			return true;
		}
		return false;
	}

}
