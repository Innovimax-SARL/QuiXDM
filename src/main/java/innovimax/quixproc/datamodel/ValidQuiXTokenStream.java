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

import java.util.Arrays;
import java.util.EnumSet;

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
 * <td>{@code START_SEQUENCE}, (<b>document</b>|<b>json</b>)*,
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
 * <th>json</th>
 * <td>:=</td>
 * <td>{@code START_JSON}, <b>object</b>, {@code END_JSON}</td>
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
	}

	protected ValidQuiXTokenStream(IQuiXStream<IQuiXToken> stream, ExtraProcess process) {
		super(stream);
		this.state = State.START;
	}

	private enum State {
		START, IN_SEQUENCE, IN_DOCUMENT, IN_DOCUMENT_AFTER_ROOT, IN_ELEMENT, IN_CONTENT_TEXT, IN_CONTENT, IN_JSON, IN_JSON_AFTER_ROOT, IN_OBJECT, IN_OBJECT_VALUE, IN_ARRAY, END
	}

	private enum Node {
		DOCUMENT, ELEMENT, JSON, OBJECT, ARRAY
	}

	private static class NodeStack {
		// this is a compact implementation using the fact that most of 
		// the time the element are of the same type
		int[] data;
		private static final int START_SIZE = 8;
		int size, pos;
		final int MASK, UNIT, MAX_ALLOWED, UPPER_MASK;

		NodeStack() {
			int max = Node.values()[Node.values().length-1].ordinal();
			int mask = 1;
			while (mask<=max) {
				mask<<=1;
			}
			this.MAX_ALLOWED = Integer.MAX_VALUE >> 1;
			this.MASK = mask - 1;
			this.UPPER_MASK = this.MAX_ALLOWED ^this.MASK;
			this.UNIT = mask;
			this.data = new int[START_SIZE];
			this.size = 8;
			this.pos = -1;
		}

		public void push(Node node) {
			int value = node.ordinal();
			if (this.pos >= 0 && value == (this.data[this.pos] & this.MASK)) {
				if (value <= this.MAX_ALLOWED) {
					this.data[this.pos] += this.UNIT;
					return;
				}
				// this is greater than maxallowed
			}
			this.pos++;
			if (this.pos >= this.size) {
				this.size = (this.size * 3) / 2 + 1;
			    System.out.println(this.size);
				this.data = Arrays.copyOf(this.data, this.size);
			}
			this.data[this.pos] = (byte) node.ordinal();
		}

		public boolean empty() {
			return this.pos < 0;
		}

		public Node pop() {
			// simple case first
			if ((this.data[this.pos] & this.UPPER_MASK) == 0)
			return Node.values()[this.data[this.pos--]];
			// now it means there is at least one
			this.data[this.pos] -= this.UNIT;
			return Node.values()[this.data[this.pos] & this.MASK];
		}

		public Node peek() {
			return Node.values()[this.data[this.pos] & this.MASK];
		}

	}

	private final NodeStack stack = new NodeStack();

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
			accept(token, EnumSet.of(QuiXToken.START_DOCUMENT, QuiXToken.START_JSON, QuiXToken.END_SEQUENCE));
			switch (token) {
			case START_DOCUMENT:
				this.state = State.IN_DOCUMENT;
				this.stack.push(Node.DOCUMENT);
				break;
			case START_JSON:
				this.state = State.IN_JSON;
				this.stack.push(Node.JSON);
				break;
			case END_SEQUENCE:
				this.state = State.END;
				break;
			default:
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
				this.state = State.IN_ELEMENT;
				this.stack.push(Node.ELEMENT);
				// update
				break;
			default:
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
			default:
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
				break;
			default:
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
				break;
			default:
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
				break;
			default:
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
				break;
			default:
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
			default:
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
				break;
			default:
			}
			break;
		case IN_JSON:
			// json := START_JSON, object, END_JSON
			accept(token, EnumSet.of(QuiXToken.START_OBJECT));
			this.stack.push(Node.OBJECT);
			this.state = State.IN_OBJECT;
			break;
		case IN_JSON_AFTER_ROOT:
			accept(token, EnumSet.of(QuiXToken.END_JSON));
			acceptStackAndSetState(token, Node.JSON);
			break;
		default:
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
				case JSON:
					this.state = State.IN_JSON_AFTER_ROOT;
					break;
				case ELEMENT:
					this.state = State.IN_CONTENT;
					break;
				case OBJECT:
					this.state = State.IN_OBJECT;
					break;
				case ARRAY:
					this.state = State.IN_ARRAY;
					break;
				default:
				}
			}
			return;
		}
		// this is different
		throw new IllegalStateException(
				"Invalid state " + token + ". Closing a node " + node + " while last open is a " + last);
	}

	private static void accept(QuiXToken token, QuiXToken expected) {
		accept(token, EnumSet.of(expected));
	}

	private static void accept(QuiXToken token, EnumSet<QuiXToken> expecteds) {
		if (expecteds.contains(token))
			return;
		//
		throw new IllegalStateException(
				"Invalid state " + token + ". One of the following state was expected: " + expecteds.toString());
	}
}
