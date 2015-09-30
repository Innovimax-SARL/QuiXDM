/*
QuiXProc: efficient evaluation of XProc Pipelines.
Copyright (C) 2011-2015 Innovimax
All rights reserved.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package innovimax.quixproc.datamodel;

import java.util.EnumSet;
import java.util.Stack;

import innovimax.quixproc.datamodel.event.IQuiXEventStreamReader;
import innovimax.quixproc.datamodel.filter.AQuiXEventStreamFilter;

/**
 * <p>
 * The {@code ValidQuiXTokenStream} is a lightweight state machine that checks
 * the following rules about {@code QuiXToken}
 * </p>
 * <table>
 * <tr>
 * <th>sequence</th>
 * <td>:=</td>
 * <td>{@code START_SEQUENCE}, (<b>document</b>|<b>object</b>)*,
 * {@code END_SEQUENCE}</td>
 * </tr>
 * <tr>
 * <th>document</th>
 * <td>:=</td>
 * <td>{@code START_DOCUMENT}, ({@code PROCESSING_INSTRUCTION}|{@code COMMENT}
 * )*, <b>element</b>, ({@code PROCESSING_INSTRUCTION}|{@code COMMENT})*,
 * {@code END_DOCUMENT}</td>
 * </tr>
 * <tr>
 * <th>element</th>
 * <td>:=</td>
 * <td>{@code START_ELEMENT}, ({@code NAMESPACE}|{@code ATTRIBUTE})*, (
 * {@code TEXT}|<b>element</b>|{@code PROCESSING_INSTRUCTION}|{@code COMMENT})*,
 * {@code END_ELEMENT}</td>
 * </tr>
 * <tr>
 * <th>object</th>
 * <td>:=</td>
 * <td>{@code START_OBJECT}, ({@code KEY_NAME}, <b>value</b>)*,
 * {@code END_OBJECT}</td>
 * </tr>
 * <tr>
 * <th>value</th>
 * <td>:=</td>
 * <td><b>object</b>|<b>array</b>|{@code VALUE_FALSE}|{@code VALUE_TRUE}|
 * {@code VALUE_NUMBER}|{@code VALUE_NULL}|{@code VALUE_STRING}</td>
 * </tr>
 * <tr>
 * <th>array</th>
 * <td>:=</td>
 * <td>{@code START_ARRAY}, <b>value</b>*, {@code END_ARRAY}</td>
 * </tr>
 * </table>
 * 
 * @author innovimax
 *
 */
public class ValidQuiXTokenStream extends AQuiXEventStreamFilter {

	State state;

	public ValidQuiXTokenStream(IQuiXStream<IQuiXToken> stream) {
		this(stream, ExtraProcess.NONE);
	}

	public ValidQuiXTokenStream(IQuiXEventStreamReader stream) {
		super(stream.asIQuiXTokenStream());
		this.state = State.START;
		// ExtraProcess.NONE);
	}

	// private interface Process {
	// checkUniqueNess(QuiXCharStream )
	// }

	enum ExtraProcess {
		NONE,
	};

	protected ValidQuiXTokenStream(IQuiXStream<IQuiXToken> stream, ExtraProcess process) {
		super(stream);
		this.state = State.START;
	}

	private enum State {
		START, IN_SEQUENCE, IN_DOCUMENT, IN_DOCUMENT_AFTER_ROOT, IN_ELEMENT, IN_CONTENT_TEXT, IN_CONTENT, IN_OBJECT, IN_OBJECT_VALUE, IN_ARRAY, END
	}

	private enum Node {
		DOCUMENT, ELEMENT, OBJECT, ARRAY
	}

	private final Stack<Node> stack = new Stack<Node>();

	@Override
	public IQuiXToken process(IQuiXToken item) throws IllegalStateException {
		QuiXToken token = item.getType();
		// System.out.println(state +", "+ token);
		switch (this.state) {
		case START:
			// sequence := START_SEQUENCE, (document|json)*, END_SEQUENCE
			accept(token, QuiXToken.START_SEQUENCE);
			this.state = State.IN_SEQUENCE;
			break;
		case IN_SEQUENCE:
			accept(token, EnumSet.of(QuiXToken.START_DOCUMENT, QuiXToken.START_OBJECT, QuiXToken.END_SEQUENCE));
			switch (token) {
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
			// document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*,
			// element, (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
			accept(token, EnumSet.of(QuiXToken.PROCESSING_INSTRUCTION, QuiXToken.COMMENT, QuiXToken.START_ELEMENT));
			switch (token) {
			case PROCESSING_INSTRUCTION:
			case COMMENT:
				// stay in this state
				break;
			case START_ELEMENT:
				state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				// update
				break;
			}
			break;
		case IN_DOCUMENT_AFTER_ROOT:
			accept(token, EnumSet.of(QuiXToken.PROCESSING_INSTRUCTION, QuiXToken.COMMENT, QuiXToken.END_DOCUMENT));
			switch (token) {
			case PROCESSING_INSTRUCTION:
			case COMMENT:
				// stay in this state
				break;
			case END_DOCUMENT:
				// unpile
				acceptStackAndSetState(token, Node.DOCUMENT);
				break;
			}
			break;

		case IN_ELEMENT:
			// element := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*, TEXT?,
			// ((element|PROCESSING-INSTRUCTION|COMMENT)+, TEXT)*,
			// (element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
			accept(token,
					EnumSet.of(QuiXToken.NAMESPACE, QuiXToken.ATTRIBUTE, QuiXToken.TEXT,
							QuiXToken.PROCESSING_INSTRUCTION, QuiXToken.COMMENT, QuiXToken.START_ELEMENT,
							QuiXToken.END_ELEMENT));
			switch (token) {
			case NAMESPACE:
			case ATTRIBUTE:
				// stay in this state
				break;
			case TEXT:
				this.state = State.IN_CONTENT_TEXT;
				break;
			case PROCESSING_INSTRUCTION:
			case COMMENT:
				this.state = State.IN_CONTENT;
				break;
			case START_ELEMENT:
				// this.state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				break;
			case END_ELEMENT:
				// unpile
				acceptStackAndSetState(token, Node.ELEMENT);
			}
			break;
		case IN_CONTENT:
			accept(token, EnumSet.of(QuiXToken.TEXT, QuiXToken.PROCESSING_INSTRUCTION, QuiXToken.COMMENT,
					QuiXToken.START_ELEMENT, QuiXToken.END_ELEMENT));
			switch (token) {
			case PROCESSING_INSTRUCTION:
			case COMMENT:
				// stay in this state
				break;
			case TEXT:
				this.state = State.IN_CONTENT_TEXT;
				break;
			case START_ELEMENT:
				// this.state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				break;
			case END_ELEMENT:
				// unpile
				acceptStackAndSetState(token, Node.ELEMENT);
			}
			break;
		case IN_CONTENT_TEXT:
			accept(token, EnumSet.of(QuiXToken.PROCESSING_INSTRUCTION, QuiXToken.COMMENT, QuiXToken.START_ELEMENT,
					QuiXToken.END_ELEMENT));
			switch (token) {
			case PROCESSING_INSTRUCTION:
			case COMMENT:
				this.state = State.IN_CONTENT;
				break;
			case START_ELEMENT:
				// this.state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				break;
			case END_ELEMENT:
				// unpile
				acceptStackAndSetState(token, Node.ELEMENT);
			}
			break;
		case IN_OBJECT:
			// object := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
			accept(token, EnumSet.of(QuiXToken.KEY_NAME, QuiXToken.END_OBJECT));
			switch (token) {
			case KEY_NAME:
				this.state = State.IN_OBJECT_VALUE;
				break;
			case END_OBJECT:
				acceptStackAndSetState(token, Node.OBJECT);
			}
			break;
		case IN_OBJECT_VALUE:
			// value :=
			// object|array|VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
			accept(token, EnumSet.of(QuiXToken.VALUE_FALSE, QuiXToken.VALUE_TRUE, QuiXToken.VALUE_NULL,
					QuiXToken.VALUE_NUMBER, QuiXToken.VALUE_STRING, QuiXToken.START_ARRAY, QuiXToken.START_OBJECT));
			switch (token) {
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
			// array := START_ARRAY, value*, END_ARRAY
			accept(token,
					EnumSet.of(QuiXToken.VALUE_FALSE, QuiXToken.VALUE_TRUE, QuiXToken.VALUE_NULL,
							QuiXToken.VALUE_NUMBER, QuiXToken.VALUE_STRING, QuiXToken.START_ARRAY,
							QuiXToken.START_OBJECT, QuiXToken.END_ARRAY));
			switch (token) {
			case VALUE_FALSE:
			case VALUE_NULL:
			case VALUE_NUMBER:
			case VALUE_TRUE:
			case VALUE_STRING:
				// stay in this state
				break;
			case START_ARRAY:
				// this.state = IN_ARRAY;
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
			throw new IllegalStateException(
					"Invalid state " + token + ". Closing a node " + node + " that is not opened");
		}
		Node last = this.stack.pop();
		if (last == node) {
			// this is what is expected
			// but need to set the correct state
			if (this.stack.empty()) {
				// we are in the SEQUENCE
				this.state = State.IN_SEQUENCE;
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
		throw new IllegalStateException(
				"Invalid state " + token + ". Closing a node " + node + " while last open is a " + last);
	}

	private void accept(QuiXToken token, QuiXToken expected) {
		accept(token, EnumSet.of(expected));
	}

	private void accept(QuiXToken token, EnumSet<QuiXToken> expecteds) {
		if (expecteds.contains(token))
			return;
		//
		throw new IllegalStateException(
				"Invalid state " + token + ". One of the following state was expected: " + expecteds.toString());
	}
}
