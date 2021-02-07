package bottlecapdetection;

public class Constants {

	public static final String GENERAL_LINE = "**********************************************";
	public static final int GENERAL_MAX_OBJECTS = 15;
	
	public static final int STATICSCENE_MIN_NR_OF_IMG = 40;					// in #

	public static final int ROI_SIDE_EXTENSION = 20;						// in [px]
	
	public static final int ANALYSIS_SLIDING_WINDOW_SIZE = 20; 				// in [px]
	
	public static final int ANALYSIS_OBJECT_MINIMUM_AREA = 500; 			// in [px]
	public static final int ANALYSIS_OBJECT_MAXIMUM_AREA = 40000; 			// in [px]
	public static final double ANALYSIS_ROI_RELATION_SIZE = 0.5;			// in [0..1]
	public static final int ANALYSIS_FILTER_MINSIZE = 30;					// in [px]
	public static final int ANALYSIS_FILTER_SIDEFACTOR = 6;
	
	public static final int COLOR_DIFF = 100;
	
	// isCircle / isDeformed Detection
	public static final double ANALYSIS_BC_DEFORMED_MIN_RATIO = 0.3;		// in [0..1]
	public static final double ANALYSIS_BC_DEFORMED_MAX_RATIO = 0.8;		// in [0..1]
	public static final double ANALYSIS_BC_COMPLETE_MIN_RATIO = Constants.ANALYSIS_BC_DEFORMED_MAX_RATIO;		// in [0..1] - Should be set to BC_DEFORMED_MAX_RATIO
	
	// Labels for printing results into csv-file:
	public static final String OUTPUT_LABEL_BC_FACEDOWN = "BottleCap_FaceDown";
	public static final String OUTPUT_LABEL_BC_FACEUP = "BottleCap_FaceUp"; 
	public static final String OUTPUT_LABEL_BC_DEFORMED = "BottleCap_Deformed"; 
	public static final String OUTPUT_LABEL_DISTRACTOR = "Distractor"; 
	public static final String OUTPUT_LABEL_UNKNOWN = "Unknown";
	
}
