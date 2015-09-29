# QuiXDM
**QuiXDM** is an open-source datamodel to process XML (via [XQuery and XPath Data Model (XDM)](http://www.w3.org/TR/xpath-datamodel/)) or JSON or both in a Streaming fashion.

[![Build Status](https://travis-ci.org/innovimax/QuiXDM.svg?branch=master)](https://travis-ci.org/innovimax/QuiXDM)
# Getting Started

To install it
*  Checkout this code. It's Java 1.8+ compliant
*  Get access to Saxon 9.6: http://saxon.sourceforge.net/
*  Get access to Jackson Core 2.6.2: https://github.com/FasterXML/jackson-core 

# Why QuiXDM?
There is SAX and StAX and DOM out there for processing XML

 Feature\API | SAX | StAX | DOM | **QuiXDM**
------|-----|------|-----|-------
in memory/streaming | streaming | streaming | in memory | **streaming**
push/pull | push | pull | -- | **pull**
data model | low level XML | low level XML | low level XML | **XPath Data Model**
handle sequence | no | no | no | **yes**
handle json | no | no | no | **yes**

# How does it work?
It minimizes (as far as the XPath Data Model requires it) the number of information to manage to allow processing
```ANTLR
// Here is the grammar of events
sequence := START_SEQUENCE, (document|object)*, END_SEQUENCE
document := START_DOCUMENT, (PROCESSING_INSTRUCTION|COMMENT)*, element, (PROCESSING_INSTRUCTION|COMMENT)*, END_DOCUMENT
element  := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*, TEXT?, ((element|PROCESSING_INSTRUCTION|COMMENT)+, TEXT)*, (element|PROCESSING_INSTRUCTION|COMMENT)*, END_ELEMENT
object   := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
value    := object|array|VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
array    := START_ARRAY, value*, END_ARRAY
```
Mostly look at [QuiXToken.java](https://github.com/innovimax/quixdm/blob/master/main/innovimax/quixproc/datamodel/QuiXToken.java)

# Use
## With Object creation (à la [javax.xml.stream.XMLEventReader](https://docs.oracle.com/javase/8/docs/api/index.html?javax/xml/stream/XMLEventReader.html))
Simplest way to use, is to instantiate [innovimax.quixproc.datamodel.in.QuiXEventStreamReader.java](https://github.com/innovimax/quixdm/blob/master/main/innovimax/quixproc/datamodel/in/QuiXEventStreamReader.java)
```java
Iterable<Source> sources = Arrays.asList(new Source[] {
		new javax.xml.transform.stream.StreamSource("/tmp/file/file_aaa.xml"),	
		new javax.xml.transform.stream.StreamSource("/tmp/file/file_aab.xml")	
});
QuiXEventStreamReader qesr = new QuiXEventStreamReader(sources);
while(qesr.hasNext()) {
	System.out.println(qesr.next());
}
```
## Lightweight iterator without Object creation (à la [javax.xml.stream.XMLStreamReader](https://docs.oracle.com/javase/8/docs/api/index.html?javax/xml/stream/XMLStreamReader.html))
***TODO***



# Why [QuiXCharStream](https://github.com/innovimax/QuiXDM/blob/master/main/innovimax/quixproc/datamodel/QuiXCharStream.java) and [QuiXQName](https://github.com/innovimax/QuiXDM/blob/master/main/innovimax/quixproc/datamodel/QuiXQName.java)?
Well it comes from the fact that Streaming interface in XML should really be streaming.
The truth is that there is no such character streaming interface in Java.
 * String is definitely not streamable and limited to 2^31 characters
 * CharSequence, which could have been, is neither because it has [length()](http://docs.oracle.com/javase/8/docs/api/java/lang/CharSequence.html#length--)
 * CharIterator doesn't exist in the JDK (but you can find it [here](http://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/chars/CharIterator.html))
 * CharSequence.chars() returns IntStream (instead of CharStream because Java 8 people didn't want to add it) which 
 * Java 8 Stream<Char> implies that every char is boxed (which means it's highly INEFFICIENT)
 
Having such context, that's why [QuiXCharStream](https://github.com/innovimax/QuiXDM/blob/master/main/innovimax/quixproc/datamodel/QuiXCharStream.java) and [QuiXQName](https://github.com/innovimax/QuiXDM/blob/master/main/innovimax/quixproc/datamodel/QuiXQName.java) went live in order to :
 * be able to address the TEXT recombination issue (text() node in XDM cannot be contiguous)
 * be able to stream even corner cases XML:
   * huge string
   * huge names
   * huge namespace uris

# Contributors
[Innovimax](http://innovimax.fr) and [INRIA Lille](http://www.inria.fr/centre/lille) is contributing to this work
# Related Projects
QuiXDM can be used standalone

This is the data model of QuiXPath and QuiXProc

It is part of two bigger projects :

*  QuiXProc: https://github.com/innovimax/quixproc
*  QuiXPath: https://github.com/innovimax/quixpath

