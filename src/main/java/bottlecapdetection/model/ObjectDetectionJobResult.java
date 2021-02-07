package bottlecapdetection.model;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Point2f;
import org.bytedeco.opencv.opencv_core.Scalar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bottlecapdetection.logic.JsonFileLoader;
import bottlecapdetection.model.json.JsonFile;
import bottlecapdetection.model.json.Shape;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains the result of an executed object detection job.
 * Finally it holds the results of a detection in the objectsDetectionResult,
 * the cumulated and counted objects for each class.
 * 
 * @author Alexander Büchel
 *
 */

@ToString
public class ObjectDetectionJobResult {
	
	private static final Logger log = LoggerFactory.getLogger(ObjectDetectionJobResult.class);
	
	@ToString.Exclude @Getter private long startProcessing = 0L;	// in [ms]
	@ToString.Exclude @Getter private long endProcessing = 0L;		// in [ms]
	@ToString.Exclude private long duration = 0L; 					// in [ms]
	@Setter @Getter private String failedMsg = "";
	@Getter private File videoFile;
	@Getter private File outputDir;
	
	// Result objects
	@Setter @Getter private ObjectDetectionResult objectDetectionResult = null;	
	@Getter private ClassificationResult classificationResult = null;
	@Getter private ClassificationResult jsonResult = null;
	
	public ObjectDetectionJobResult(File videoFile, File outputDir) {
		objectDetectionResult = new ObjectDetectionResult();
		this.videoFile = videoFile;
		this.outputDir = outputDir;
	}
	
	public void startProcessingTime() {
		this.startProcessing = System.currentTimeMillis();
	}
	
	public void endProcessingTime() {
		this.endProcessing = System.currentTimeMillis();
		duration = (int)(this.endProcessing - this.startProcessing);
	}
	
	public void countObjects() {
		classificationResult = new ClassificationResult();
		classificationResult.setDistractorCnt(countCategory(ClassificationType.DISTRACTOR));
		classificationResult.setBottleCapDeformedCnt(countCategory(ClassificationType.BOTTLECAP_DEFORMED));
		classificationResult.setBottleCapFaceDownCnt(countCategory(ClassificationType.BOTTLECAP_DOWN));
		classificationResult.setBottleCapFaceUpCnt(countCategory(ClassificationType.BOTTLECAP_UP));
	}
	
	private int countCategory(ClassificationType type) {
		int cnt = 0;
		for (DetectedObject detObj : objectDetectionResult.getObjects()) {
			if (detObj.getType() == type) {
				cnt++;
			}
		}
		return cnt;
	}
	
	public void storeResultAsImage() {
		Mat roi = objectDetectionResult.getRoi();
		Scalar color = new Scalar(0, 255, 0, 0);
        for (int i = 0; i < objectDetectionResult.getObjects().size(); i++) {
        	//rectangle(roi, objectDetectionResult.getObjects().get(i).getBoundingBox().tl(), objectDetectionResult.getObjects().get(i).getBoundingBox().br(), color);
        	int numberOfPoints = 4;
            Point2f rectPoints = new Point2f(numberOfPoints);
            objectDetectionResult.getObjects().get(i).getMinAreaRect().points(rectPoints);
            for (int j = 0; j < numberOfPoints; j++) {
                line(roi, new Point((int)rectPoints.getPointer(j).x(), (int)rectPoints.getPointer(j).y() ),  new Point((int)rectPoints.getPointer((j+1) % numberOfPoints).x(), (int)rectPoints.getPointer((j+1) % numberOfPoints).y()), color);
            }
        }
        imwrite(outputDir + "/" + videoFile.getName().replaceAll(".mp4", ".png"), roi);
	}
	
	/**
	 * This method prints out the collected results.
	 */
	public void printResults() {
		log.info("DETECTED OBJECTS: ");
		for (DetectedObject detObj : objectDetectionResult.getObjects()) {
			if (detObj.getType() != ClassificationType.IGNORED_OBJECT && detObj.getType() != ClassificationType.INNER_OBJECT) {
				log.info("   {}", detObj);
			}
		}
		log.info("");
		if (classificationResult != null && jsonResult != null) {
			log.info("BC_UP(detected/json): {}/{} BC_DOWN(detected/json): {}/{} DEFORMED(detected/json): {}/{} DISTRACTORS(detected): {} TOTAL(detected/json): {}/{}   TOTAL TIME [ms]: {}", 
					classificationResult.getBottleCapFaceUpCnt(), 
					jsonResult.getBottleCapFaceUpCnt(),
					classificationResult.getBottleCapFaceDownCnt(), 
					jsonResult.getBottleCapFaceDownCnt(),
					classificationResult.getBottleCapDeformedCnt(), 
					jsonResult.getBottleCapDeformedCnt(),
					classificationResult.getDistractorCnt(),
					classificationResult.totalCnt(),
					jsonResult.totalCnt(),
					duration);
			
			@SuppressWarnings("unused")
			String latexLine = 
					videoFile.getName() + 
					"&" + classificationResult.getBottleCapFaceDownCnt() + 
					"&" + classificationResult.getBottleCapFaceUpCnt() + 
					"&" + classificationResult.getBottleCapDeformedCnt() + 
					"&" + classificationResult.getDistractorCnt() + 
					"&" + classificationResult.totalCntWithoutDistractors() + 
					"&" + jsonResult.getBottleCapFaceDownCnt() + 
					"&" + jsonResult.getBottleCapFaceUpCnt() + 
					"&" + jsonResult.getBottleCapDeformedCnt() + 
					"&" + jsonResult.totalCntWithoutDistractors() + 
					"&" + (classificationResult.totalCntWithoutDistractors() - jsonResult.totalCntWithoutDistractors()) + 
					"\\\\" + 
					"\r\n";
			//Use this line to create a latex tabular row
//			try (PrintWriter output = new PrintWriter(new FileWriter("c:\\latex\\latexTabular", true))) {
//			    output.printf(latexLine);
//			} catch (Exception e) {
//				log.warn("Exception: {}", e);
//			}
			
		} else if (classificationResult != null) {
			log.info("UP: {} DOWN: {} DEFORMED: {} DISTRACTORS: {} TOTAL: {} TOTAL TIME [ms]: {}", 
					classificationResult.getBottleCapFaceUpCnt(), 
					classificationResult.getBottleCapFaceDownCnt(), 
					classificationResult.getBottleCapDeformedCnt(), 
					classificationResult.getDistractorCnt(),
					classificationResult.totalCnt(),
					duration);
		}
		
		log.info("");
	}
	
	/**
	 * Writes results into csv text file.
	 */
	
	public void writeResultsToCsvFile() {
		String csvFilename = videoFile.getName().replaceAll(".mp4", ".csv");
		String csvFilepath = outputDir + "/" + csvFilename;
		try {
			FileWriter fileWriter = new FileWriter(csvFilepath);
			for (DetectedObject detObj : objectDetectionResult.getObjects()) {
				if (detObj.getType() != ClassificationType.IGNORED_OBJECT && detObj.getType() != ClassificationType.INNER_OBJECT) {
					fileWriter.write(objectDetectionResult.getStaticSceneIdx() + "," + (int)detObj.centerX + "," + (int)detObj.centerY + ",'" + detObj.getLabel()+"'\r\n");
				}
			}
			fileWriter.flush();
			log.info("Results are written to output file: {}", csvFilepath);
			fileWriter.close();
		} catch (IOException e) {
			log.warn("Could not write file:", e);
		}
	}
	
	public void analyseJsonFile() {
		String jsonFileName = null;
		
		//Try to find json file based on name convention
		if (videoFile.getName().startsWith("CV20")) {
			//We expect here a file name: "CV20_video_x.mp4"
			jsonFileName = videoFile.getName().replace("_video_", "_label_renamed_").replace(".mp4", ".json");
			
		} else {
			//We try to find a jsonFile by replacing the file extension
			 jsonFileName = videoFile.getName().replace(".mp4", ".json");
		}
		String jsonPath = videoFile.getParent() + "\\" + jsonFileName;
		log.info("Try to find json file: {}", jsonPath);
		File jsonFile = new File(jsonPath);
		if (!jsonFile.exists()) {
			log.warn("Corresponding json file does not exist: {}", jsonFile.getAbsoluteFile());
			return;
		}
		countObjectsInJsonFile(jsonFile.getAbsolutePath());
	}
	private void countObjectsInJsonFile(String jsonFileName) {
		int BC_FaceUp_Cnt = 0;
		int BC_FaceDown_Cnt = 0;
		int BC_Deformed = 0;
		try {
			JsonFile jsonFile = JsonFileLoader.loadJsonFile(jsonFileName);

			
			for (Shape shape : jsonFile.getShapes()) {
				String label = shape.getLabel();
				if (label.contains("BottleCapFaceDown") || label.contains("BottleCap_FaceDown")) {
					BC_FaceDown_Cnt++;
				} else if (label.contains("BottleCapFaceUp") || label.contains("BottleCap_FaceUp")) {
					BC_FaceUp_Cnt++;
				} else if (label.contains("BottleCapDeformed") || label.contains("BottleCap_Deformed")) {
					BC_Deformed++;
				}
			}
		} catch (Exception e) {
			log.warn("Exception: {}", e);
		}
		jsonResult = new ClassificationResult();
		jsonResult.setBottleCapDeformedCnt(BC_Deformed);
		jsonResult.setBottleCapFaceDownCnt(BC_FaceDown_Cnt);
		jsonResult.setBottleCapFaceUpCnt(BC_FaceUp_Cnt);
	}

}
