# Density
The goal is to talk about density of information in data format like XML, JSON, YAML, HTML, CSV
## Node Density
The node density is the number of node that we can create with any given amount of bytes
### XML
For example, the maximum node density is reached via this simple pattern
```
a<b/>
```
which makes 5 bytes long and generates two nodes :
 * a text node with the string "a"
 * a element with the name "b"
 
## Event Density
