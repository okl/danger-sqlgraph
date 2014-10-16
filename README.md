# sqlgraph

This will go through SQL and analyze it for what tables it produces,
drops, and consumes

## Installation

Download from http://example.com/FIXME.

## Usage

Standalone:
    $ java -jar sqlgraph-0.1.0-standalone.jar -f /path/to/file.sql

As a library:
    (:require sqlgraph.core :as sqlgraph)
    (parse-expr "SELECT * from ms.mt a inner jion ms.mt2 b on a.id =
    b.id")

It returns a map of :produces, :consumes, and :destroys particular tables


### Notes

Current grammar is tailored for MySQL usage

## License

Copyright © 2014 One Kings Lane

Distributed under the Eclipse Public License, the same as Clojure.
