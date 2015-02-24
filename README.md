RMLProcessor
============

Processor for RML (R2RML extension) in Java based on [DB2Triples](https://github.com/antidot/db2triples/)

Class diagram
-------------
![alt text](https://raw.github.com/mmlab/RMLProcessor/master/docs/class-diagram.jpg)

Installation
------------

The processor can be installed using Maven, so make sure you have installed it first: http://maven.apache.org/download.cgi and java 1.7

    mvn clean install

Usage
-----
You see the commandline options execute the following command.
    
    java -jar RMLMapper-0.1.jar -H

To execute a mapping on the commandline run it like this:

    java -jar RMLMapper-0.1.jar -M mapping.ttl -O output.nt

If you want to start a service execute:

    java -jar RMLMapper-0.1.jar -S -P 8150 -C

Where `-P` and `-C` are optional. Default port is 8150. The transfomer service
is used by issuing an HTTP POST request posting the mapping file as body. 

You can for example use cURL to run transformation. 

    curl -X POST --d @<rml-napping-file> "http://localhost:8150/?mapping=<uri-of-rml-mapping>"


Remark
-----

On OSX, it might be needed to export JAVA_HOME=$(/usr/libexec/java_home)

More Information
----------------

More information about the solution can be found at http://rml.io

This application is developed by Multimedia Lab http://www.mmlab.be

Copyright 2014, Multimedia Lab - Ghent University - iMinds

License
-------

The RMLProcessor is released under the terms of the [MIT license](http://opensource.org/licenses/mit-license.html).
