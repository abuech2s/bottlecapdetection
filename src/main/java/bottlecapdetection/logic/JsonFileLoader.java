package bottlecapdetection.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

import bottlecapdetection.model.json.JsonFile;
import bottlecapdetection.model.json.Shape;

/**
 * This class loads the json content from a jsonFile created by LabelMe.
 * Spaces and linebreaks are removed.
 * 
 * The json content is parsed and mapped to objectoriented structure by Gson.
 * An object of type JsonFile is returned.
 * 
 * @author Alexander Buechel
 *
 */

public class JsonFileLoader {

	/**
	 * 
	 * Reads in a json file, parses it to a JsonFile-Object.
	 * 
	 * @param filepath - Filepath to a json file
	 * @return An object of type JsonFile
	 * @throws IOException - In case of IO exceptions
	 */
	
	public static JsonFile loadJsonFile(String filepath) throws IOException {
		String jsonContent = new String(Files.readAllBytes(Paths.get(filepath))).replaceAll("\r\n", "").replaceAll(" ", "");
		Gson gson = new Gson();
		JsonFile jsonFile = gson.fromJson(jsonContent, JsonFile.class);
		
		for (Shape s : jsonFile.getShapes()) {
			s.calcAvgPoints();
		}
		
		return jsonFile;
	}

}
