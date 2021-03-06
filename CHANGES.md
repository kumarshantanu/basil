#Changes and TODO


## [TODO] 2012-????-?? / 0.5.0

* [TODO] Wrap fn/evaluator invocation exceptions with Basil template info
  * originating from Quiddity (due to wrong number/type of args)
  * originating from user-supplied fns/evaluators/values
* [TODO] Nested template sections
* [TODO] Template distribution mechanism
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


## 2013-June-24 / 0.4.1

* Fix issue where missing file did not lead to a meaningful error message


## 2012-October-16 / 0.4.0

* Common cached-group feature
* Update dependencies
  * Quiddity 0.2.0
  * clip-test 0.2.0


## 2012-August-30 / 0.3.0

* ClojureScript support (via _clip-test_)
* Use _Quiddity_ to eval slots
* Update context built-ins (replace conditionals with Quiddity built-ins)


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
