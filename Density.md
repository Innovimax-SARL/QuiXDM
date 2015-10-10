# Density
The goal is to talk about density of information in data format like XML, JSON, YAML, HTML, CSV
## Node Density
The node density is the number of node that we can create with any given amount of bytes
### XML Node Density
For example, the maximum node density is reached via this simple pattern
```
a<b/>
```
which makes 5 bytes long and generates 2 nodes :
 * a text node with the string "a"
 * a element with the name "b"
 
So in the end
```
<r>(a<b/>)+</r>
```
is the densiest XML available in Node density : 40%
## Event Density
The event density is the number of event that we can create with any given amount of bytes. Their is a direct relationship with Node density :
* Node density <= Event density (always)

It comes from the fact that every node that is always a leaf (attribute, processing-instruction) generates 1 events, while node that can contains other node always generate 2 events (start_element, end_element)

### XML Event Density
For example, the maximum event density is reached via this simple pattern
```
a<b/>
```
which makes 5 bytes long and generates 3 events :
 * a text event with the string "a"
 * a start_element event with the name "b"
 * a end_element event witht the name "b"
 
So in the end
```
<r>(a<b/>)+</r>
```
is the densiest XML available in event density : 60%


