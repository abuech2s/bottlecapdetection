package bottlecapdetection.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import bottlecapdetection.logic.JsonFileLoader;
import bottlecapdetection.model.json.JsonFile;

public class JsonTest {
	
	public static String testResourcesPath = "src/test/resources/testdata";

	@Test
	public void jsonTest() throws IOException {
		String filename = testResourcesPath + "/json/9013350_abuech2s_1.json";
		
		JsonFile jsonFile = JsonFileLoader.loadJsonFile(filename);
		
		assertEquals(6, jsonFile.getShapes().size());

		assertEquals(354, jsonFile.getShapes().get(0).getAvgX());
		assertEquals(350, jsonFile.getShapes().get(0).getAvgY());
		
		assertEquals(517, jsonFile.getShapes().get(1).getAvgX());
		assertEquals(284, jsonFile.getShapes().get(1).getAvgY());
		
		assertEquals(599, jsonFile.getShapes().get(2).getAvgX());
		assertEquals(321, jsonFile.getShapes().get(2).getAvgY());
		
		assertEquals(778, jsonFile.getShapes().get(3).getAvgX());
		assertEquals(344, jsonFile.getShapes().get(3).getAvgY());
		
		assertEquals(619, jsonFile.getShapes().get(4).getAvgX());
		assertEquals(636, jsonFile.getShapes().get(4).getAvgY());
		
		assertEquals(602, jsonFile.getShapes().get(5).getAvgX());
		assertEquals(714, jsonFile.getShapes().get(5).getAvgY());
	}
	
}
