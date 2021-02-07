package bottlecapdetection.model;

import org.bytedeco.opencv.opencv_core.Mat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StaticSceneResult {

	@Getter private int staticSceneIdx = 0;
	@Getter private Mat staticScene = null;
	
}
