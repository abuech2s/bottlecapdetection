package bottlecapdetection.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.Test;

import bottlecapdetection.logic.Mp4FileLoader;

public class VideoTests {
	
	/**
	 * Iterate over a directory of test mp4 files. Read them in and hold the list of Mat-Objects.
	 * We check, if the return value of each file contains at least 1 Mat ( > 0 ).
	 */
	
	@Test
	public void readMp4FileTestWithGrabber() {
		Map<Integer, Integer> results = new HashMap<>();
		for (int idx = 100; idx <= 200; idx++) {
			try {
				String filename = "C:\\test\\VideoPackage1_2\\CV20_video_" + idx + ".mp4";
				List<Mat> matList = Mp4FileLoader.loadMP4File(filename);
				int cnt = matList.size();
			    results.put(idx, cnt);
			} catch (Exception e) {
				System.err.println("Exception: " + e);
			}
		}
		
	    for (Entry<Integer, Integer> entry : results.entrySet()) {
	    	assertTrue(entry.getValue() > 0);
	    }	
	}
	
	@Test
	public void checkNrOfFramesTest() {
		int cnt = 0;
		try {
			String filename = "C:\\test\\VideoPackage1_2\\CV20_video_100.mp4";
			List<Mat> matList = Mp4FileLoader.loadMP4File(filename);
			cnt = matList.size();
		} catch (Exception e) {
			System.err.println("Exception: " + e);
		}
		System.out.println(cnt);

	}
	
}
