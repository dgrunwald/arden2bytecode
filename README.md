Arden2ByteCode - Compiler for Arden Syntax with Java Bytecode output
====================================================================

Copyright 2009-2010, Daniel Grunwald , 2011-2012, Hannes Flicka
Portions (arden.scc) Copyright 2004, University of British Columbia  
See LICENSE.txt for licensing information.

Building Notes
--------------

To compile Arden2ByteCode, you first need to generate the parser using
the SableCC parser generator.  
To do this, run  
`java -classpath ../tools/sablecc.jar org.sablecc.sablecc.SableCC arden.scc`  
in the arden2bytecode/src directory.  
This will create the directories `analysis`,`lexer`,`node`,`parser` within
`arden2bytecode/src/arden`.

When the input grammar is changed, you will need to re-generate the parser.
Before regenerating the parser, you should delete the old `analysis`,`lexer`,
`node`,`parser` directories to ensure there aren't any old files left behind. 

If you use Eclipse to build, compiling the parser is done automatically.
The Eclipse project has a SableCC builder in Project -> Properties -> Builders.  
The SableCC builder of the project basically starts the Ant task `sableCC` 
contained in build.xml.  
As building the parser has only to be done initially and after changes of 
the grammar, you can disable the SableCC builder if you want to save time.

If you use Ant to build, SableCC is started automatically. Again, if you want
to save time, you can disable the `sableCC` target by removing the `sableCC`
dependency from the `compile` target.


Building Howto
--------------

To build with Eclipse, import the project and choose  
Project -> Build project... from the menu.

To build with Ant, cd into the source root and type `ant` at the command 
prompt.  

This is explained in detail in the project's wiki at GitHub:  
https://github.com/hflicka/arden2bytecode/wiki/Getting-Started-with-Arden2ByteCode


Notes to the Present Implementation
-----------------------------------

Daniel:  
I believe this compiler fully implements Arden Syntax 2.5 with the following exceptions:

Languages features not implemented:

* From Arden Syntax 2.1 specification:
    * 10.2.4.6 Event Call
    * 11.2.2 Event Statement
    * 13 Evoke Slot
* From Arden Syntax 2.5 specification:
    * 11.2.5.2 Message As statement 
    * 11.2.5.6 Destination As statement
    * A6.2 "Time of" always returns null for objects. (correct would be: if all attributes of an object share a common primary time, the time of operator will return that time when applied to the object) 
    * Timezones for ArdenTime values.
    * Some string formatting specificiers are not implemented.
    * There is no way to use Arden variables within mapping clauses.
    * Citation/links slots are not syntax checked.
    * The compiler does not check that no languages features newer than the specified 'Arden Version' are used.
