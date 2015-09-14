# QuiXDM
**QuiXDM** is an open-source implementation of an [XQuery and XPath Data Model (XDM)](http://www.w3.org/TR/xpath-datamodel/) able to do Streaming Processing.

# Getting Started

To install it
*  Checkout this code. It's Java 1.6+ compliant
*  Get access to Saxon 9.6: http://saxon.sourceforge.net/ 

# Why QuiXDM?
There is SAX and StAX and DOM out there for processing XML

 Feature\API | SAX | StAX | DOM | **QuiXDM**
------|-----|------|-----|-------
in memory/streaming | streaming | streaming | in memory | **streaming**
push/pull | push | pull | -- | **pull**
data model | low level XML | low level XML | low level XML | **XPath Data Model**
handle sequence | no | no | no | **yes**

# How does it work?
It minimizes (as far as the XPath Data Model requires it) the number of XML information to manage
```ANTLR
// Here is the grammar of events
sequence := START_SEQUENCE, document*, END_SEQUENCE
document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element, (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
element := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*, (TEXT|element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
```

Mostly look at [QuiXToken.java](https://github.com/innovimax/quixdm/blob/master/main/innovimax/quixproc/datamodel/QuiXToken.java)

# Use
## With Object creation (à la [javax.xml.stream.XMLEventReader](https://docs.oracle.com/javase/8/docs/api/index.html?javax/xml/stream/XMLEventReader.html))
Simplest way to use, is to instanciate [innovimax.quixproc.datamodel.in.QuiXEventStreamReader.java](https://github.com/innovimax/quixdm/blob/master/main/innovimax/quixproc/datamodel/in/QuiXEventStreamReader.java)
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
 
Having such context, that's why [QuiXCharStream](https://github.com/innovimax/QuiXDM/blob/master/main/innovimax/quixproc/datamodel/QuiXCharStream.java) and [QuiXQName](https://github.com/innovimax/QuiXDM/blob/master/main/innovimax/quixproc/datamodel/QuiXQName.java) went live in order to be able to stream even corner cases XML:
 * huge names
 * huge string
 * huge namespace

# Contributors
[Innovimax](http://innovimax.fr) and [INRIA Lille](http://www.inria.fr/centre/lille) is contributing to this work
# Related Projects
QuiXDM can be used standalone

This is the data model of QuiXPath and QuiXProc

It is part of two bigger projects :

*  QuiXProc: https://github.com/innovimax/quixproc
*  QuiXPath: https://github.com/innovimax/quixpath

