package bottlecapdetection.model;

import org.bytedeco.opencv.opencv_core.Mat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ROIResult {

	@Getter @Setter Mat roiSubImage;
	@Getter @Setter Mat binaryImage;
	@Getter @Setter boolean tookCompleteImage = false;
	
}
