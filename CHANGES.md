Changes and TODO


## 2012-????-?? / 0.2.0

* [TODO] ClojureScript support
* [TODO] Auto-select/let-user-override default rendering for speed
* [TODO] Filter for reversing HTML entities


## 2012-August-?? / 0.1.0

* Template with slots
* Parse/compile and Render are separate
* Pluggable error reporting
* Model
* Filter functions
* Slot features
    * attributes   <% name %>
    * literals     <% "Number" 1 "String" "Joe Walker" "Keyword" :male %>
    * filter calls <% (second name) %>
* Template Groups and "Include"
* JVM specific API for template groups
* Collection literals - vectors, sets, maps
* Cache compiled templates (re-load only if modified)
* HTML entities (escaping HTML to avoid XSS)
* [TODO] Tests for JVM, HTML-entities and filter functions
* [TODO] Documentation