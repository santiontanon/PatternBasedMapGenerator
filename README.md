# PatternBasedMapGenerator

This is a generic pattern-based map generator, that receives a collection of "patterns" and "cosntraints", and uses a constraint-based search process in order to find a configuration of the input patterns that satisfy the constraints. It was designed to be part of the procedural-content generation (PCG) pipeline of a little personal project. But I decided to separate it into its own project.


## Pattern File format

Patterns are specified via creating two types of files: _symbol definition files_ and _pattern files_:

- _Symbol definition files_: these files define a mapping from individual characters (used to define the patterns) and concepts in your game (e.g., enemies, trees, etc.). The character `' '` is by default associated with the concept `empty`. An example symbol definition file can be found in the _data_ folder, but a short example would look like this:

`<Symbols>`  
` 	<tile symbol="," type="grass"/>`  
` 	<tile symbol="~" type="water"/>`
` 	<tile symbol="i" type="item"/>`  
`  	<tile symbol="E" type="enemy"/>`  
`</Symbols>`  

- _Pattern files_: these define the collection of patterns to be used by the map generator. Each pattern has a set of attributes, a set of layers and a list of objects. An example pattern file with a single pattern would look like this (full examples can be found in the _data_ folder):

`<patterns`  
`  <pattern width="5" height="5" type="forest" tag="path,enemy,path-w-n,path-w-e,path-w-s,path-n-e,path-n-s,path-e-s" north="none" east="path" south="none" west="path" canrotate="true" priority="2" weight="0.1">`  
`    <layer layer="0">`  
`      <row>,,,,,</row>`  
`      <row>,,,,,</row>`  
`      <row>,,,,,</row>`  
`      <row>,,,,,</row>`  
`      <row>,,,,,</row>`  
`    </layer>`  
`    <layer layer="1">`  
`      <row>     </row>`  
`      <row>     </row>`  
`      <row>=====</row>`  
`      <row>     </row>`  
`      <row>     </row>`  
`    </layer>`  
`    <object x="2" y="2" type="enemy"/>`  
`  </pattern>`  

The attributes of a pattern define are:
- _width_ and _height_: the width and height of a pattern in tiles
- _type_: a comma-separated list of tags used to filter which patterns to use to generate a given map. For example, you can assign different tags to patterns used for generating indoor from those used to generate outdoor maps.
- _tag_: a comma-separated list of tags used to indicate the content of this patter (e.g., if it has an enemy, or a certain other structure). These tags can be used for defining constraints. There is a set of predefined tags that the generator will understand ( `path-w-n`, `path-w-e`, `path-w-s`, `path-n-e`, `path-n-s`, `path-e-s`) and represent wether there is a path connecting each of the four sides of the pattern. These are used in order to check whether some types of constraints are satisfied or not.
- _north_, _east_, _south_, _west_: a comma-separated list of tags representing the type of borders of the pattern. When putting two patterns side by side, the map generator will use these to ensure patterns match.
- _priority_: among all the patterns that could be used in a given situation, only those with lowest priority will be considered.
- _weight_: use to calculate the probability od selecting this pattern (the higher, the more probability).
- _canrotate_: if this is set to true, four copies of this pattern will be created, each of them rotated 90 degrees.

The layers define the background of the map (e.g., the walls, etc.). You can specify as many layers as desired (the first is numbered "0"), and finally, any additional objects (such as enemies, items, etc.) are defined after the layers. The types of objects and the characters used in the layers must be defined in the symbol definition files.
