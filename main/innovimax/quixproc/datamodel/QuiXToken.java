package innovimax.quixproc.datamodel;

public enum QuiXToken {
	// Here is the grammar of events
	// sequence := START_SEQUENCE, (document|json)*, END_SEQUENCE
	// document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element,
	// (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
	// element  := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*,
	// (TEXT|element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
	// json     := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
	// value    := array|VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
    // array    := START_ARRAY, value*, END_ARRAY
	START_SEQUENCE, END_SEQUENCE, 
	START_DOCUMENT, END_DOCUMENT, 
	START_ELEMENT, END_ELEMENT, 
	NAMESPACE,  ATTRIBUTE, TEXT, PI, COMMENT,
	// JSON
	START_ARRAY, END_ARRAY,
	START_OBJECT, END_OBJECT,
	KEY_NAME, VALUE_FALSE, VALUE_TRUE, VALUE_NUMBER, VALUE_NULL, VALUE_STRING
}
/* TO LOOK FOR EVENTS for JSON also from http://docs.oracle.com/javaee/7/api/javax/json/stream/JsonParser.Event.html
END_ARRAY
End of a JSON array.
END_OBJECT
End of a JSON object.
KEY_NAME
Name in a name/value pair of a JSON object.
START_ARRAY
Start of a JSON array.
START_OBJECT
Start of a JSON object.
VALUE_FALSE
false value in a JSON array or object.
VALUE_NULL
null value in a JSON array or object.
VALUE_NUMBER
Number value in a JSON array or object.
VALUE_STRING
String value in a JSON array or object.
VALUE_TRUE
true value in a JSON array or object.

*/