package bottlecapdetection;

import java.io.File;

import org.bytedeco.ffmpeg.global.avutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bottlecapdetection.exceptions.IOException;
import bottlecapdetection.logic.ObjectDetectionRunner;

/**
 * This is the main class. Taking the start parameters and instantiates and runs the ObjectDetectionRunner.
 * It includes a help description as well.
 * 
 * 
 * @author Alexander Büchel
 *
 */

public class BottleCapDetector {
	
	private static final Logger log = LoggerFactory.getLogger(BottleCapDetector.class);
	
	/**
	 * This is the main procedure
	 * 
	 * @param args - Default args from commandline
	 * @throws IOException - In case of any IO failure
	 */
	
	public static void main(String[] args) throws IOException {
		
		// Deactivate javacv logger
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		
		log.info(Constants.GENERAL_LINE);
		
		// Check general settings:
		if (args == null || args.length != 2) {
			printHelp();
			System.exit(-1);
		}
		
		// Check, if file exists:
		File videoFile = new File(args[0]);
		if (!videoFile.exists()) {
			log.info("Could not find file. Abort.");
		}
		
		File outputDir = new File(args[1]);
		if (!outputDir.mkdir()) {
			log.debug("Could not create outputDir: {}. Maybe it already exists.", outputDir);
		}
		
		// Setting up object detector and run identification process
		ObjectDetectionRunner odr = new ObjectDetectionRunner(videoFile, outputDir);		
		odr.execute();
		odr.getResult().writeResultsToCsvFile();
		log.info("");
		odr.getResult().printResults();
		odr.getResult().storeResultAsImage();
		log.info(Constants.GENERAL_LINE);
	}
	
	/**
	 * Prints out this help information
	 */
	
	private static void printHelp() {
		log.info(Constants.GENERAL_LINE);
		log.info("");
		log.info("The BottleCapDetector (Alexander Buechel, HBRS, CV20):");
		log.info("");
		log.info("Usage:");
		log.info("");
		log.info("For WINDOWS : detect.bat <path-to-mp4-file> <ResultDirectory>");
		log.info("For LINUX : detect.sh <path-to-mp4-file> <ResultDirectory>");
		log.info("");
		log.info("    with <path-to-mp4-file> : Path to a single mp4 file.");
		log.info("    with <ResultDirectory> : Result Directory.");
		log.info("");
		log.info(Constants.GENERAL_LINE);
	}
	
}
