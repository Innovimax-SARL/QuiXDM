package innovimax.quixproc.datamodel;

import java.util.EnumSet;
import java.util.Stack;

import innovimax.quixproc.datamodel.filter.AQuiXEventStreamFilter;

public class ValidQuiXTokenStream extends AQuiXEventStreamFilter<QuiXToken>{
	State state;
	public ValidQuiXTokenStream(IQuiXStream<QuiXToken> stream) {
		super(stream);
		this.state = State.START;
	}
	// sequence := START_SEQUENCE, (document|object)*, END_SEQUENCE
	// document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element,
	// (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
	// element  := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*,
	// (TEXT|element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
	// object   := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
	// value    := object|array|VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
        // array    := START_ARRAY, value*, END_ARRAY
	private enum State { START, IN_SEQUENCE, IN_DOCUMENT, IN_DOCUMENT_AFTER_ROOT, IN_ELEMENT, IN_CONTENT_TEXT, IN_CONTENT,IN_OBJECT, IN_OBJECT_VALUE, IN_ARRAY, END  }
	private enum Node { DOCUMENT, ELEMENT, OBJECT, ARRAY }
	private final Stack<Node> stack = new Stack<Node>();
	@Override
	public QuiXToken process(QuiXToken token) throws IllegalStateException {
		switch(this.state) {
		case START:
			accept(token, QuiXToken.START_SEQUENCE);
			this.state = State.IN_SEQUENCE;
			break;
		case IN_SEQUENCE:
//			sequence := START_SEQUENCE, (document|json)*, END_SEQUENCE
			switch(token) {
			case START_DOCUMENT:
				this.state = State.IN_DOCUMENT;
				this.stack.push(Node.DOCUMENT);
				break;
			case START_OBJECT:
				this.state = State.IN_OBJECT;
				this.stack.push(Node.OBJECT);
				break;
			case END_SEQUENCE:
				this.state = State.END;
				break;
			}
			break;
		case END:
			// will throw an error
			accept(token, EnumSet.noneOf(QuiXToken.class));
			break;
		case IN_DOCUMENT:	
//			document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element, (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
			accept(token, EnumSet.of(QuiXToken.PI, QuiXToken.COMMENT, QuiXToken.START_ELEMENT));
			switch (token) {
			case PI :
			case COMMENT :
				// stay in this state
				break;
			case START_ELEMENT :
				state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				break;
			}
			break;
		case IN_DOCUMENT_AFTER_ROOT:
			accept(token, EnumSet.of(QuiXToken.PI, QuiXToken.COMMENT, QuiXToken.END_DOCUMENT));
			switch (token) {
			case PI :
			case COMMENT :
				// stay in this state
				break;
			case END_DOCUMENT :
				// unpile
				acceptStackAndSetState(token, Node.DOCUMENT);
				break;
			}
			break;
			
		case IN_ELEMENT :
//			element  := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*, TEXT?, ((element|PROCESSING-INSTRUCTION|COMMENT)+, TEXT)*, (element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
			accept(token, EnumSet.of(QuiXToken.NAMESPACE, QuiXToken.ATTRIBUTE, QuiXToken.TEXT, QuiXToken.PI, QuiXToken.COMMENT, QuiXToken.START_ELEMENT, QuiXToken.END_ELEMENT));
			switch (token) {
			case NAMESPACE:
			case ATTRIBUTE:
				// stay in this state
				break;
			case TEXT:
				this.state = State.IN_CONTENT_TEXT;
				break;
			case PI:
			case COMMENT:
				this.state = State.IN_CONTENT;
				break;
			case START_ELEMENT:
				//this.state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				break;
			case END_ELEMENT:
				// unpile
				acceptStackAndSetState(token, Node.ELEMENT);
			}
			break;
		case IN_CONTENT:
			accept(token, EnumSet.of(QuiXToken.TEXT, QuiXToken.PI, QuiXToken.COMMENT, QuiXToken.START_ELEMENT, QuiXToken.END_ELEMENT));			
			switch (token) {
			case PI:
			case COMMENT:
				// stay in this state
				break;
			case TEXT:
				this.state = State.IN_CONTENT_TEXT;
				break;
			case START_ELEMENT:
				//this.state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				break;
			case END_ELEMENT:
				// unpile
				acceptStackAndSetState(token, Node.ELEMENT);
			}
			break;
		case IN_CONTENT_TEXT:	
			accept(token, EnumSet.of(QuiXToken.PI, QuiXToken.COMMENT, QuiXToken.START_ELEMENT, QuiXToken.END_ELEMENT));			
			switch (token) {
			case PI:
			case COMMENT:
				this.state = State.IN_CONTENT;
				break;
			case START_ELEMENT:
				//this.state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				break;
			case END_ELEMENT:
				// unpile
				acceptStackAndSetState(token, Node.ELEMENT);
			}
			break;
		case IN_OBJECT :	
//			object     := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
			accept(token, EnumSet.of(QuiXToken.KEY_NAME, QuiXToken.END_OBJECT));
			switch(token) {
			case KEY_NAME:
				this.state = State.IN_OBJECT_VALUE;
				break;
			case END_OBJECT:
				acceptStackAndSetState(token, Node.OBJECT);
			}
			break;
		case IN_OBJECT_VALUE:	
//			value    := object|array|VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
			accept(token, EnumSet.of(QuiXToken.VALUE_FALSE, QuiXToken.VALUE_TRUE, QuiXToken.VALUE_NULL, QuiXToken.VALUE_NUMBER, QuiXToken.VALUE_STRING, QuiXToken.START_ARRAY,QuiXToken.START_OBJECT));
			switch(token){
			case VALUE_FALSE:
			case VALUE_NULL:
			case VALUE_NUMBER:
			case VALUE_TRUE:
			case VALUE_STRING:
				this.state = State.IN_OBJECT;
				break;
			case START_OBJECT:
				this.state = State.IN_OBJECT;
				this.stack.push(Node.OBJECT);
				break;
			case START_ARRAY:
				this.state = State.IN_ARRAY;
				this.stack.push(Node.ARRAY);
				break;				
			}
			break;
		case IN_ARRAY:				
//			array    := START_ARRAY, value*, END_ARRAY
			accept(token, EnumSet.of(QuiXToken.VALUE_FALSE, QuiXToken.VALUE_TRUE, QuiXToken.VALUE_NULL, QuiXToken.VALUE_NUMBER, QuiXToken.VALUE_STRING, QuiXToken.START_ARRAY,QuiXToken.START_OBJECT, QuiXToken.END_ARRAY));
			switch(token){
			case VALUE_FALSE:
			case VALUE_NULL:
			case VALUE_NUMBER:
			case VALUE_TRUE:
			case VALUE_STRING:
				// stay in this state
				break;
			case START_ARRAY:
				//this.state = IN_ARRAY;
				this.stack.push(Node.ARRAY);
				break;
			case START_OBJECT:
				this.state = State.IN_OBJECT;
				this.stack.push(Node.OBJECT);
				break;
			case END_ARRAY:
				// unpile
				acceptStackAndSetState(token, Node.ARRAY);
			}
		}
		return token;
	}
	private void acceptStackAndSetState(QuiXToken token, Node node) {
		if (this.stack.empty()) {
			throw new IllegalStateException("Invalid state "+token+". Closing a node "+node+" that is not opened");
		}
		Node last = this.stack.pop();
		if (last == node) {
			 // this is what is expected
			// but need to set the correct state
			if (this.stack.empty()) {
				// this state should be illegal right ?
				this.state = State.END;
			} else {
				Node current = this.stack.peek();
				switch (current) {
				case DOCUMENT:
					this.state = State.IN_DOCUMENT_AFTER_ROOT;
					break;
				case ELEMENT:
					this.state = State.IN_CONTENT;
					break;
				case OBJECT:
					this.state = State.IN_OBJECT;
				case ARRAY:
				}
			}
			return;
		}
		// this is different
		throw new IllegalStateException("Invalid state "+token+". Closing a node "+node+" while last open is a "+last);
	}
	private void accept(QuiXToken token, QuiXToken expected) {
		accept(token, EnumSet.of(expected));
	}

	private void accept(QuiXToken token, EnumSet<QuiXToken> expecteds) {
		if (expecteds.contains(token)) 
			return;
		// 
		throw new IllegalStateException("Invalid state "+token+". One of the following state was expected: "+expecteds.toString());
	}
}
