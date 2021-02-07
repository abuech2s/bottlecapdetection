package bottlecapdetection.logic;

/**
 * This class is a provider for atomic job ids.
 * It started by job id 1 and increments them by 1.
 * 
 * @author Alexander Büchel
 *
 */

public class ObjectIdProvider {

	private static int lastId = 0;
	
	/**
	 * A static method for returning a new job id.
	 * 
	 * @return The new job id.
	 */
	
	public static int getNewId() {
		lastId++;
		return lastId;
	}
	
}
