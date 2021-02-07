package bottlecapdetection.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class offers a method to load a mp4 file.
 * 
 * @author Alexander Buechel
 *
 */

public class Mp4FileLoader {
	
	private static final Logger log = LoggerFactory.getLogger(Mp4FileLoader.class);

	/**
	 * Loads a mp4 file given by an absolute path. It uses FFmpegFrameGrabber
	 * from Mvn OpenCv-Dependency.
	 * 
	 * To prevent memory leak problems, we expect the static scene inside the second or third quarter.
	 * Therefore - based on the total number of frame - we calculate the indices, we are just interested in
	 * and just extract those images.
	 * 
	 * @param filePath - The absolute file path to a mp4 file
	 * @return A list of null entries and a specific range of Mat objects of this mp4 file
	 * @throws Exception - in case of grabFrameExceptions, type: org.bytedeco.javacv.FrameGrabber.Exception
	 * @throws FileNotFoundException - in case of file was not found
	 */
	public static List<Mat> loadMP4File(String filePath) throws Exception, FileNotFoundException {
		
		List<Mat> matObjects = new ArrayList<>();
		
		File file = new File(filePath);
		if (!file.exists()) {
			throw new FileNotFoundException("File not found: " + filePath);
		}
		
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filePath);
		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
		grabber.start();
		
		int totalFrameCnt = grabber.getLengthInFrames();
		int IdxFirstQuarter = (int)(totalFrameCnt / 4);
		int IdxThirdQuarter = (int)(totalFrameCnt / 4 * 3);
		
		Frame frame;
		int cnt = 1;
		
		log.debug("Going to extract images idx {} to {} from video file (total frames: {}).", IdxFirstQuarter, IdxThirdQuarter, grabber.getLengthInFrames());
		
	    while ((frame = grabber.grabImage()) != null) {
	    	if (cnt >= IdxFirstQuarter && cnt <= IdxThirdQuarter) {
	    		Mat mat = converterToMat.convert(frame);
		    	matObjects.add(mat.clone());
	    	} else {
	    		matObjects.add(null); // Using null values for reducing storage and avoiding memory problems
	    	}
	    	cnt++;
	    }
	    
	    grabber.stop();
	    grabber.close();

	    return matObjects;
	}
	
}
