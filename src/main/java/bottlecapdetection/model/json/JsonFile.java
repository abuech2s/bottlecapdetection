package bottlecapdetection.model.json;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class represents the JsonFile section by json output of LabelMe.
 * 
 * @author Alexander Buechel
 *
 */

@Setter @Getter @ToString
public class JsonFile {

	private List<Shape> shapes = new ArrayList<>();
	private String imagePath;
	
}
