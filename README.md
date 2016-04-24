# PatternBasedMapGenerator

This is a generic pattern-based map generator, that receives a collection of "patterns" and "cosntraints", and uses a constraint-based search process in order to find a configuration of the input patterns that satisfy the constraints. It was designed to be part of the procedural-content generation (PCG) pipeline of a little personal project. But I decided to separate it into its own project.

(I'm still working on this, since after separating it from the res tof the project, many things need to be re-programmed; updates coming soon)

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
- _type_: ...
- _tag_: ...
- _north_, _east_, _south_, _west_: ...
- _priority_: ...
- _weight_: ...
- _canrotate_: ...

The layers define the background of the map (e.g., the walls, etc.). You can specify as many layers as desired (the first is numbered "0"), and finally, any additional objects (such as enemies, items, etc.) are defined after the layers. The types of objects and the characters used in the layers must be defined in the symbol definition files.
