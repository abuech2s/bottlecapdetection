package bottlecapdetection.tests;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import org.junit.jupiter.api.Test;

import bottlecapdetection.logic.Mp4FileLoader;
import bottlecapdetection.logic.StaticSceneFinder;
import bottlecapdetection.model.StaticSceneResult;

public class StaticSceneTests {

	@Test
	public void findStaticSceneTest() throws Exception {
		String filename = "C:\\test\\VideoPackage1_2\\CV20_video_100.mp4";
		List<Mat> matList = Mp4FileLoader.loadMP4File(filename);
		StaticSceneResult ssR = StaticSceneFinder.find(matList);
		Mat staticScene = ssR.getStaticScene();
		imwrite("C:\\test\\input.png", staticScene);
		
	}
	
}
