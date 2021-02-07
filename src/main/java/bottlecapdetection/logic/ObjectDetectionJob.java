package bottlecapdetection.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bottlecapdetection.exceptions.MatException;
import bottlecapdetection.model.ObjectDetectionJobResult;
import bottlecapdetection.model.ObjectDetectionResult;
import bottlecapdetection.model.ROIResult;
import bottlecapdetection.model.StaticSceneResult;
import lombok.Getter;

/**
 * This class represents an object detection job. The input is a path to a file.
 * While processing, it will create a result object, which contain several information
 * about this detection.
 * 
 * @author Alexander Buechel
 *
 */

public class ObjectDetectionJob {

	private static final Logger log = LoggerFactory.getLogger(ObjectDetectionJob.class);
	
	@Getter private ObjectDetectionJobResult result = null;
	
	public ObjectDetectionJob(File videoFile, File outputDir) {
		result = new ObjectDetectionJobResult(videoFile, outputDir);
	}
	
	/**
	 * This method performs the execution of object detection job
	 */
	
	public void process() {
		log.info("Process started for {}", result.getVideoFile().getName());
		result.startProcessingTime();
		
		try {
			//Read file
			List<Mat> matList = Mp4FileLoader.loadMP4File(result.getVideoFile().getAbsolutePath());
			
			//Get static scene
			StaticSceneResult ssR = StaticSceneFinder.find(matList);
			Mat staticScene = ssR.getStaticScene();
			
			//Find ROI
			ROIResult rr = Logic.extractROI(staticScene);
			Mat roi = rr.getRoiSubImage();
	
			//Find & Classify objects
		    ObjectDetectionResult odr = Logic.localizeAndClassifyObjects(roi);
		    odr.setStaticSceneIdx(ssR.getStaticSceneIdx());
		    
		    //Set job results
		    result.setObjectDetectionResult(odr);
			result.getObjectDetectionResult().setRoi(roi);
			result.countObjects();
			result.analyseJsonFile();
			
		} catch (FileNotFoundException e) {
			result.setFailedMsg("FileNotFound: " + result.getVideoFile().getName());
		} catch (MatException e) {
			result.setFailedMsg("MatException: " + e.getMessage());
		} catch (Exception e) {
			result.setFailedMsg("Exception: " + e.getMessage());
		}
		result.endProcessingTime();
	}
}
