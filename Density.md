# Density
The goal is to talk about density of information in data format like XML, JSON, YAML, HTML, CSV
## Node Density
The node density is the number of node that we can create with any given amount of bytes
### XML Node Density : max 40%
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
is the densiest XML available in Node density : 40%.
### JSON Node Density : max 50%
For example, the maximum node density is reached via this pattern
```
[1,1, ... ,1]
```
which provides 1 node (number_value) for each 2 bytes : "," and "1".

So in the end
```
{"A":[1,1, ... ,1]}
```
is the densiest JSON available in Node density : 50%. 
## Event Density
The event density is the number of event that we can create with any given amount of bytes. Their is a direct relationship with Node density :
* Node density <= Event density (always)

It comes from the fact that every node that is always a leaf (attribute, processing-instruction) generates 1 events, while node that can contains other node always generate 2 events (start_element, end_element)

### XML Event Density : max 60%
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
### JSON Event Density : max 99,99%
#### zero depth : max 66,67%
For example, the maximum node density is reached via this pattern
```
[{},{}, ... ,{}]
```
which provides 2 events (start_object and end_object) for each 3 bytes : "," and "{" and "}".

So in the end
```
{"A":[{},{}, ... ,{}]}
```
is the densiest JSON available in Event density without depth : 66,67%.
#### increasing depth
For example, the maximum node density is reached via this pattern
```
[[[.....[[......]].....]]]
```
which provides 2 events (start_array and end_array) for each 2 bytes :  "[" and "]".

So in the end
```
{"A":[[[.....[[......]].....]]]}
```
is the densiest JSON available in Event density with depth :99,99%.


