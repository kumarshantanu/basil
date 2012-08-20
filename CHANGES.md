#Changes and TODO


## 2012-????-?? / 0.3.0

* [TODO] ClojureScript support
* [TODO] Investigate abstracting out string concatenation
  * Auto-select efficient JVM/CLJS specific concatenation
  * Use java.lang.StringBuilder on the JVM
  * See https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
  * Use something like this for CLJS (see 'Host Interop' section at above URL)

```clojure
  (let [sb (goog.string.StringBuffer. "hello, ")]
    (.append sb "world")
    (.toString sb))
```

* [TODO] Filter fns for reversing HTML entities


## 2012-August-21 / 0.2.0

* Streamlined filter-fns for HTML generation, esp collections
* Library functions for 'locals scope'
* Error reporting must throw exception by default
  * implicitly uses JVM/CLJS error-handler
* New API basil.public[-cljs]


## 2012-August-08 / 0.1.0

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
