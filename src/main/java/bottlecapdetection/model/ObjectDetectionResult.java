package bottlecapdetection.model;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import lombok.Getter;
import lombok.Setter;

public class ObjectDetectionResult {

	@Setter @Getter private Mat roi;
	@Setter @Getter private MatVector contours;
	@Setter @Getter private List<DetectedObject> objects = new ArrayList<>();
	@Setter @Getter private double expectedBottleCapSize;
	@Setter @Getter int staticSceneIdx;
	
}
