We use the google gson library to parse the json file into custom classes. The root json is parsed to object of type <Scratch> which includes a List of <Target>. The <Target> type has a Map of the blocks of type <Block>. The <Block> type holds everying inside a block: opcode,parent,next,key,index,inputs,fields,x and y. We then constructed a method blockToString that takes a block as an input and returns a pesudo code representation of the block through formatting the opcode and inputs of the said block. We also made a method called generateStory which takes the current block as an input and an indentation boolean. This method calls blockToString recursively until there are no blocks left. We also check if the next line should be indented by calling the method shouldIndent on a block which checks if this block's key is the SUBSTACK or SUBSTACK2 value of any other block and indents accordingly. The output is then written to a file every recursive call of generateStory.

Usage: please import the gson and simple json libraries.
Please open the result.txt with notepad++ or a file editor that is not the classic windows notepad or line breaks won't be shown.

Sample output:
=====START=====
When green flag is clicked:
Think: MESSAGE: Hmm... for: 2 SECS
Say: MESSAGE: Hello!
Repeat: 10 TIMES
 Turn Right: 15 DEGREES
If: x less than: 30
then: 
 Change X by: 10 DX
Wait Until: x greater than: 50
Repeat Until: y equals: 40
 Go to X: 40 X
 Go to Y: 40 Y
When key is pressed: space
Wait for: DURATION: 2
Say: MESSAGE: Hello! for: 2 SECS
Think: MESSAGE: Hmm...
Change Y by: 50 DY
If: x equals: 50
then: 
 Set: X to -100 
else:
 Set: Y to 40
Forever: 
 Move: 2 STEPS
=====END=====