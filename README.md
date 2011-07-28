Wingate Power Analyzer
======================

What
----
A [Wingate test](http://en.wikipedia.org/wiki/Wingate_test) is an anaerobic
test, often performed on a cycle ergometer, that is used to measure anaerobic
capacity and power.

This is a program which takes this power data (in the form of Excel
spreadsheets) and computes the peak 1-second power for each data set.

Caveats
-------
As it stands currently, if you have worksheets within your files which are NOT
valid test data, you'll need to specify the prefix of the worksheets that
should be analyzed.  The program will then look at number-suffixed variations
of that prefix (e.g., "Datasheet 1", "Datasheet 2", etc.).

Implementation
--------------
It's written in Clojure, using the hilarious and derogatorily-named
[Apache POI](http://poi.apache.org/) libraries for dealing with Excel files and
[Seesaw](https://github.com/daveray/seesaw) as the Clojure DSL for creating
the GUI.

TODO
----
The GUI layout is terrible! It could use some polish.

## License

Copyright 2011 Lyle Hanson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
