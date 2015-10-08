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

/**
 * The {@code QuiXToken} is the central enum of QuiXDM It contains all the
 * Events that a full QuiXDM implementation must support
 */
public enum QuiXToken implements IQuiXToken {
	// Here is the grammar of events
	// sequence := START_SEQUENCE, (document|json)*, END_SEQUENCE
	// document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element,
	// (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
	// json := START_JSON, object, END_JSON
	// element := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*,
	// (TEXT|element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
	// object := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
	// value :=
	// object|array|VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
	// array := START_ARRAY, value*, END_ARRAY
	// SEQUENCE
	START_SEQUENCE, END_SEQUENCE,
	// XML
	START_DOCUMENT, END_DOCUMENT, START_ELEMENT, END_ELEMENT, NAMESPACE, ATTRIBUTE, TEXT, PROCESSING_INSTRUCTION, COMMENT,
	// JSON
	START_JSON, END_JSON, START_ARRAY, END_ARRAY, START_OBJECT, END_OBJECT, KEY_NAME, VALUE_FALSE, VALUE_TRUE, VALUE_NUMBER, VALUE_NULL, VALUE_STRING;

	@Override
	public QuiXToken getType() {
		return this;
	}
}