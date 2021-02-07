package bottlecapdetection.model;

/**
 * This enum defines different ClassificationTypes:
 * 
 * BOTTLECAP_UP : Value for a bottle cap (face up)
 * BOTTLECAP_DOWN : Value for a bottle cap (face down)
 * BOTTLECAP_DEFORMED : Value for a deformed bottle cap
 * DISTRACTOR : Value for a distractor
 * INNER_OBJECT : Used for identified inner objects
 * IGNORED_OBJECT : "Objects", which are detected and covers other objects, are ignored in further steps
 * UNKNOWN : Initial value
 * 
 * @author Alexander Buechel
 *
 */

public enum ClassificationType {
	BOTTLECAP,
	BOTTLECAP_UP, 
	BOTTLECAP_DOWN, 
	BOTTLECAP_DEFORMED, 
	DISTRACTOR, 
	INNER_OBJECT, 
	IGNORED_OBJECT,
	UNKNOWN;
}
