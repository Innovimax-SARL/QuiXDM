# quixdm
**QuiXDM** is an open-source implementation of an [XQuery and XPath Data Model (XDM)](http://www.w3.org/TR/xpath-datamodel/) able to do Streaming Processing.

# Getting Started

To install, it
*  Checkout this code. It's Java 1.6+ compliant: http://code.google.com/p/quixpath/source/checkout
*  Get access to Saxon 9.6: http://saxon.sourceforge.net/ 

# Why QuiXDM?
There is SAX and StAX and DOM out there for processing XML

 API | SAX | StAX | DOM | **QuixDM**
------|-----|------|-----|-------
in memory/streaming | streaming | streaming | in memory | **streaming**
push/pull | push | pull | -- | **pull**
data model | low level XML | low level XML | low level XML | **XPath Data Model**
handle sequence | no | no | no | **yes**

# How does it works?
We minimize (as the XPath Data Model requires it) the number of XML information we manage
```ANTLR
  // Here is the grammar of events
sequence := START_SEQUENCE, document*, END_SEQUENCE
document := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element, (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
element := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*, (TEXT|element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
```

Mainly look at [QuixToken.java](https://github.com/innovimax/quixdm/blob/master/main/innovimax/quixproc/datamodel/QuixToken.java)

# Use
Simplest way to use, is to instanciate [innovimax.quixproc.datamodel.in.QuixEventStreamReader.java](https://github.com/innovimax/quixdm/blob/master/main/innovimax/quixproc/datamodel/in/QuixEventStreamReader.java)
```java
Iterable<Source> sources = Arrays.asList(new Source[] {
		new javax.xml.transform.stream.StreamSource("/tmp/file/file_aaa.xml"),	
		new javax.xml.transform.stream.StreamSource("/tmp/file/file_aab.xml"),	
	});
	QuixEventStreamReader qesr = new QuixEventStreamReader(sources);
	while(qesr.hasNext()) {
		System.out.println(qesr.next());
	}
```
# Contributors

[Innovimax](http://innovimax.fr) and [INRIA Lille](http://www.inria.fr/centre/lille) is contributing to this work
Related Projects

QuiXDM can be used standalone

This is the data model of QuiXPath and QuiXProc

It is part of two bigger projects :

*  QuiXProc: https://github.com/innovimax/quixproc
*  QuiXPath: https://github.com/innovimax/quixpath

