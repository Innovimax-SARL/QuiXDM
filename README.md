[![Build Status](https://travis-ci.org/Innovimax-SARL/QuiXDM.svg?branch=master)](https://travis-ci.org/Innovimax-SARL/QuiXDM)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/6518/badge.svg)](https://scan.coverity.com/projects/6518)
[![Code Climate](https://codeclimate.com/github/Innovimax-SARL/QuiXDM/badges/gpa.svg)](https://codeclimate.com/github/Innovimax-SARL/QuiXDM)

# QuiXDM
**QuiXDM** is an *ubiquitous* open-source datamodel to process in a Streaming fashion:
* [x] XML (via [XQuery and XPath Data Model (XDM)](https://www.w3.org/TR/xpath-datamodel)) 
* [x] JSON 
  * [x] YAML
* [x] RDF Triple 
  * [ ] Quad
* [x] CSV
  * [ ] TSV
* [ ] HTML

# Getting Started

To install it
*  Checkout this code. It's Java 1.8+ compliant
*  Get access to Saxon 9.7: http://saxon.sourceforge.net/
*  Get access to Jackson Core 2.7.4: https://github.com/FasterXML/jackson-core
*  and few other dependencies (see pom.xml)

# Why QuiXDM?
There is SAX,StAX, DOM, Jackson, Jena, CSVParser, HTMLParser out there for processing data

 Feature\API | SAX | StAX | DOM | Jackson | **QuiXDM**
------|-----|------|-----|-------|----
in memory/streaming | streaming | streaming | in memory | streaming | **streaming**
push/pull | push | pull | -- | pull | **pull**
data model | low level XML | low level XML | low level XML | low level JSON | **XPath Data Model**
handle sequence | no | no | no | no | **yes**
handle json/yaml | no | no | no | yes | **yes**
handle rdf  | no | no | no | no  | **yes**
handle csv  | no | no | no | no  | **yes**
handle html | no | no | no | no  | **yes**

# How does it work?
It uses a consistent datamodel to represent all those contents in streaming.

```ANTLR
// Here is the grammar of events
sequence       := START_SEQUENCE, (document|json_yaml|table|semantic)*, END_SEQUENCE
document       := START_DOCUMENT, (PROCESSING-INSTRUCTION|COMMENT)*, element, (PROCESSING-INSTRUCTION|COMMENT)*, END_DOCUMENT
json_yaml      := START_JSON, object, END_JSON
table          := START_TABLE, header*, array_of_array, END_TABLE
semantic       := START_RDF, statement*, END_RDF
element        := START_ELEMENT, (NAMESPACE|ATTRIBUTE)*, (TEXT|element|PROCESSING-INSTRUCTION|COMMENT)*, END_ELEMENT
object         := START_OBJECT, (KEY_NAME, value)*, END_OBJECT
value          := object|array|flat_value
flat_value     := VALUE_FALSE|VALUE_TRUE|VALUE_NUMBER|VALUE_NULL|VALUE_STRING
array          := START_ARRAY, value*, END_ARRAY
array_of_array := START_ARRAY, flat_array+, END_ARRAY
flat_array     := START_ARRAY, flat_value*, END_ARRAY
statement      := START_PREDICATE, SUBJECT, OBJECT, GRAPH?, END_PREDICATE
```
Mostly look at [QuiXToken.java](https://github.com/innovimax/QuiXDM/blob/master/src/main/java/innovimax/quixproc/datamodel/QuiXToken.java)

# Use
## With Object creation (à la [javax.xml.stream.XMLEventReader](https://docs.oracle.com/javase/8/docs/api/index.html?javax/xml/stream/XMLEventReader.html))
Simplest way to use, is to instantiate [innovimax.quixproc.datamodel.in.QuiXEventStreamReader.java](https://github.com/innovimax/QuiXDM/blob/master/src/main/java/innovimax/quixproc/datamodel/in/QuiXEventStreamReader.java)
```java
Iterable<Source> sources = 
		"/tmp/file/file_aaa.xml",	
		"/tmp/file/file_aab.json",
		"/tmp/file/file_aac.csv",
		"/tmp/file/file_aad.yml",
		"/tmp/file/file_aae.n3"	
;
QuiXEventStreamReader qesr = new QuiXEventStreamReader(sources);
while(qesr.hasNext()) {
	System.out.println(qesr.next());
}
```
## Lightweight iterator without Object creation (à la [javax.xml.stream.XMLStreamReader](https://docs.oracle.com/javase/8/docs/api/index.html?javax/xml/stream/XMLStreamReader.html))
***TODO***



# Why [QuiXCharStream](https://github.com/innovimax/QuiXDM/blob/master/src/main/java/innovimax/quixproc/datamodel/QuiXCharStream.java) and [QuiXQName](https://github.com/innovimax/QuiXDM/blob/master/src/main/java/innovimax/quixproc/datamodel/QuiXQName.java)?
Well it comes from the fact that Streaming interface in XML should really be streaming.
The truth is that there is no such character streaming interface in Java.
 * String is definitely not streamable and limited to 2^31 characters
 * CharSequence, which could have been, is neither because it has [length()](http://docs.oracle.com/javase/8/docs/api/java/lang/CharSequence.html#length--)
 * CharIterator doesn't exist in the JDK (but you can find it [here](http://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/chars/CharIterator.html))
 * CharSequence.chars() returns IntStream (instead of CharStream because Java 8 people didn't want to add it) 
 * Java 8 Stream<Char> implies that every char is boxed (which means it's highly INEFFICIENT)
 
Having such context, that's why [QuiXCharStream](https://github.com/innovimax/QuiXDM/blob/master/src/main/java/innovimax/quixproc/datamodel/QuiXCharStream.java) and [QuiXQName](https://github.com/innovimax/QuiXDM/blob/master/src/main/java/innovimax/quixproc/datamodel/QuiXQName.java) went live in order to :
 * be able to address the TEXT recombination issue (text() node in XDM cannot be contiguous)
 * be able to stream even corner cases XML:
   * huge string
   * huge names
   * huge namespace uris

# Contributors
[Innovimax](http://innovimax.fr) is contributing to this work
# Related Projects
QuiXDM can be used standalone

This is the data model of QuiXPath and QuiXProc

It is part of two bigger projects :

*  QuiXProc: https://github.com/innovimax/quixproc
*  QuiXPath: https://github.com/innovimax/quixpath

