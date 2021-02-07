package bottlecapdetection.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.opencv.opencv_core.CvContour;
import org.bytedeco.opencv.opencv_core.CvMemStorage;
import org.bytedeco.opencv.opencv_core.CvRect;
import org.bytedeco.opencv.opencv_core.CvSeq;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Point2f;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RotatedRect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.junit.jupiter.api.Test;
import org.opencv.core.MatOfPoint2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_videoio.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_highgui.*;

import bottlecapdetection.logic.Logic;
import bottlecapdetection.model.BinaryResult;
import bottlecapdetection.model.DetectedObject;
import bottlecapdetection.model.ObjectDetectionResult;
import bottlecapdetection.model.ROIResult;

/**
 * This class contains some unit tests for composed algorithms
 * based on opencv functionalities.
 * 
 * @author Alexander Büchel
 *
 */

@SuppressWarnings("unused")
public class ImageTests {
	
	private static final Logger log = LoggerFactory.getLogger(ImageTests.class);
	public static String testResourcesPath = "src/test/resources/testdata";
	
	/**
	 * This method shows a picture and waits.
	 */
	private void showImg(String title, Mat mat) {
		imshow(title, mat);
    	waitKey(0);
	}
	
	/**
	 *  Reads an image from a png file.
	 */
	@Test
	public void ReadImageTest() {
      File resourcesDirectory = new File(testResourcesPath + "/png/pic01.png");
      Mat mat = imread(resourcesDirectory.getAbsolutePath()); 
      Size size = mat.size();
      
      assertEquals(1080.0, size.height());
      assertEquals(1920.0, size.width());
      //showImg("Binary Image", mat);
	}
	
	/**
	 * Transforms a color image to a grayscale image.
	 */
	@Test
	public void colorToGrayImageTest() {

      File resourcesDirectory = new File(testResourcesPath + "/png/pic01.png");
      Mat matColor = imread(resourcesDirectory.getAbsolutePath());
      Mat matGray = new Mat();
      cvtColor(matColor, matGray, COLOR_BGR2GRAY);

      Size size = matGray.size();
      
      assertEquals(1080.0, size.height());
      assertEquals(1920.0, size.width());
      //showImg("Color to Gray scale:", matGray);
	}
	
	/**
	 * Finding best threshold for calculating a binary image from a color image.
	 */
	@Test
	public void findBestThresholdforROITest() {

      File resourcesDirectory = new File(testResourcesPath + "/png/pic01.png");
      Mat matColor = imread(resourcesDirectory.getAbsolutePath());
      
      Mat matGray = new Mat();
      cvtColor(matColor, matGray, COLOR_BGR2GRAY);
      GaussianBlur(matGray, matGray, new Size(11, 11), 0);
      
      int bestThreshold = Logic.findBestThresholdForROI(matGray);

      Mat binaryMat = new Mat();
      threshold(matGray, binaryMat, bestThreshold, 255, THRESH_BINARY);

      Size size = binaryMat.size();
      assertEquals(1080.0, size.height());
      assertEquals(1920.0, size.width());
      assertEquals(140, bestThreshold);
      //showImg("Binary Image", binaryMat);
	}

	
	/**
	 * Find and extract the ROI of an image.
	 */
	@Test
	public void findAndExtractROITest() {

		File resourcesDirectory = new File(testResourcesPath + "/png/pic01.png");
		Mat matColor = imread(resourcesDirectory.getAbsolutePath());
		
		ROIResult rr = Logic.extractROI(matColor);
		Mat roi = rr.getRoiSubImage();

		Size colorMatSize = matColor.size();
		Size roiSize = roi.size();
		assertEquals(653.0, roiSize.height());
		assertEquals(850.0, roiSize.width());
		assertEquals(1080.0, colorMatSize.height());
		assertEquals(1920.0, colorMatSize.width());
		assertTrue(colorMatSize.width() > roiSize.width());
		assertTrue(colorMatSize.height() > roiSize.height());
	}
	
	/**
	 * Localization of objects in ROI and calculation of feature attributes.
	 * This test checks the number of found objects and gives possibilities to
	 * create exemplaric drawn objects.
	 * 
	 * In case of drawings, activate specific commands.
	 * 
	 */
	@Test
	public void findObjectsOnROITest() {

		File resourcesDirectory = new File(testResourcesPath + "/png/pic01.png");
	    Mat matColor = imread(resourcesDirectory.getAbsolutePath());
	    
	    ROIResult roiResult = Logic.extractROI(matColor);
	    Mat roi = roiResult.getRoiSubImage();
	    
	    ObjectDetectionResult odr = Logic.localizeAndClassifyObjects(roi);
	    List<DetectedObject> objects = odr.getObjects();
		
        @SuppressWarnings("resource")
		Scalar color = new Scalar(0, 255, 0, 0);
        
        for (int i = 0; i < odr.getObjects().size(); i++) {
        	//Draw Contours
            //drawContours(roi, odr.getObjects().get(i).getContourAsMatVector(), -1, color);
        	
        	//Draw Approximation poly points
            //drawContours(roi, odr.getObjects().get(i).getPolyPointsAsMatVector(), -1, color);
        	
        	//Draw Convex Hulls
            //drawContours(roi, odr.getObjects().get(i).getConvexHullAsMatVector(), -1, color);
        	
        	//Draw Rotated Rectangles
        	int numberOfPoints = 4;
            Point2f rectPoints = new Point2f(numberOfPoints);
            odr.getObjects().get(i).getMinAreaRect().points(rectPoints);
            for (int j = 0; j < numberOfPoints; j++) {
                line(roi, new Point((int)rectPoints.getPointer(j).x(), (int)rectPoints.getPointer(j).y() ),  new Point((int)rectPoints.getPointer((j+1) % numberOfPoints).x(), (int)rectPoints.getPointer((j+1) % numberOfPoints).y()), color);
            }
        	
        	//Draw bounding box
            rectangle(roi, objects.get(i).getBoundingBox().tl(), objects.get(i).getBoundingBox().br(), color);
        	
        	//Draw minimum enclosing circle
            //circle(roi, new Point((int)objects.get(i).getCenterPoint().x(), (int)objects.get(i).getCenterPoint().y()), (int)objects.get(i).getRadius(), color);
        	
            log.info("{}", odr.getObjects().get(i));
            showImg("Result", roi);
        }
        
        //In case of drawn objects, activate this command
        showImg("Result", roi);

		assertEquals(19, odr.getContours().size());
		assertEquals(9, odr.getObjects().size());
		assertEquals(73.4, odr.getExpectedBottleCapSize(), 0.01);
	}
	
}
