package bottlecapdetection.logic;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bottlecapdetection.model.ObjectDetectionJobResult;
import lombok.Getter;

/**
 * This class represents the JobRunner and starts, processes and terminates the jobs for given mp4 file.
 * 
 * @author Alexander Buechel
 *
 */

public class ObjectDetectionRunner {
	
	private static final Logger log = LoggerFactory.getLogger(ObjectDetectionRunner.class);

	private File videoFile;
	private File outputDir;
	@Getter private ObjectDetectionJobResult result = null;
	
	public ObjectDetectionRunner(File videoFile, File outputDir) {
		this.videoFile = videoFile;
		this.outputDir = outputDir;
	}
	
	public void execute() {
		log.info("Start processing with file: {}.", videoFile);
		
		ObjectDetectionJob objDetJob = new ObjectDetectionJob(videoFile, outputDir);
		objDetJob.process();
		result = objDetJob.getResult();
		if (!result.getFailedMsg().isEmpty()) {
			log.warn(result.getFailedMsg());
		}
		
		log.info("End processing.");
	}
	
}
