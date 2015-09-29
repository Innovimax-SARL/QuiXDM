package innovimax.quixproc.datamodel;

/**
 * The {@code QuiXToken} is the central enum of QuiXDM
 * It contains all the Events that a full QuiXDM implementation must support 
 */
public enum QuiXToken {
	// Here is the grammar of events
	// sequence := START_SEQUENCE, (document|object)*, END_SEQUENCE
	// document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element,
	// (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
	// element  := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*,
	// (TEXT|element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
	// object     := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
	// value    := object|array|VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
    // array    := START_ARRAY, value*, END_ARRAY
	START_SEQUENCE, END_SEQUENCE, 
	START_DOCUMENT, END_DOCUMENT, 
	START_ELEMENT, END_ELEMENT, 
	NAMESPACE,  ATTRIBUTE, TEXT, PROCESSING_INSTRUCTION, COMMENT,
	// JSON
	START_ARRAY, END_ARRAY,
	START_OBJECT, END_OBJECT,
	KEY_NAME, VALUE_FALSE, VALUE_TRUE, VALUE_NUMBER, VALUE_NULL, VALUE_STRING
}