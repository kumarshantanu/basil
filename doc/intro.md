# Introduction to Basil

TODO: write [great documentation](http://jacobian.org/writing/great-documentation/what-to-write/)

Basil is a templating library for Clojure. You may use it for generating HTML,
email content, SQL statements and so on. The dynamic parts of Basil templates
look and feel like Clojure expressions, which is by design.

Basil templates contain static and dynamic parts. As a user, you may choose
between two methods of using templates:

* **Method 1:** First compile a template. Subsequently, render a compiled
  template using _context_ (also called _locals_) data.
* **Method 2:** Create a _template group_ that returns compiled templates on
  demand. Subsequently, fetch a compiled template by name and render that using
  _context_ (also called _locals_) data.


## Composition of a template

Every Basil template

* may be composed of _static text_ and optionally, _slots_.
* invariably begins with _static text_.
* has _static text_ and _slots_ always distinct and never nested.

_Static text_ is literally just arbitrary static text and is rendered as it is
in a context-free manner. A _slot_ is a snippet of text contained within the
`<% %>` tags, which is evaluated at runtime to produce dynamic text. A _slot_
text must be a valid Clojure expression. (Technically, the _slot_ text must
follow the Clojure grammar and should be a valid Clojure S-expression.)

Both static and slot text must escape `<%` as `\<%` and `%>` as `\%>` when using
them as literals.

### Example templates

A template that contains only static text:

```
foo bar
This is a line.
And this is another.
```

A template containing static an slot text:

```
Hello, <% name %>!
```

A template containing more static and slot text:

```
Hello, <% name %>!
<% (when-not (seq orders) identity "You have no orders.") %>
<% (when (seq orders) str "You have " (count orders) " orders:") %>
<% (for-each [:each orders] order-detail :each) %>

Have a nice day.
```

### Slots in detail

As you can notice from the examples above, _slot_ text looks like Clojure
expressions. However, a _slot_ text must conform to only a very tiny subset of
Clojure. Notably,

* Templates have the notion of _context_, which is a list of maps that is used
  to lookup dynamic values when rendering a slot.
* While rendering, symbols in a _slot_ text are searched recursively throughout
  an expression, looked up in the _context_ (by keyword) and replaced with
  corresponding values.
* Symbols in a _slot_ text that do not exist in the _context_ result in error
  during rendering.
* _Slots_ may not use 'list' as a data structure for literals; they may use
  only vectors, sets and maps as data structures for literals.
* _Slots_ do not have lexical scope and the entire expression is evaluated at
  once.
* Since value of a macro cannot be determined, it cannot be stored in the
  _context_; consequently, a _slot_ text may not make use of them.
* Due to the constraints mentioned above, symbols in _slot_ text may point to
  only data and functions. Fortunately, this helps to keep templates as
  logic-less and simple as possible.


### Filters

The _slots_ are supposed to point to _context_ variables and constants that get
operated upon by [filter](http://en.wikipedia.org/wiki/Photographic__filter)-style
functions. Since all _slot_ variables and functions are supplied as part of the
_context_, you have complete control over how a template is going to be
rendered.

Recommended approach to set up the _context_ is to have

* a set of filter functions for the application present in the context at all
  times.
* required variables populated in the _context_ on a per-template or per
  use-case basis.

#### Built-in functions

There are several built-in functions that you get as a part of the _context_.
Shadowing them with your own version is not recommended.


**Generic string conversion**

|Function   | Arguments | Description |
|-----------|-----------|-------------|
|auto-str   | x         | Converts `x` (if collection, interposing with newline) to string. |
|default    | x         | Applied when a _slot_ has only data, e.g. <% foo %> |
|identity   | x         | Same as clojure.core/identity |
|partial    | f & args  | Same as clojure.core/partial  |
|seq        | x         | Same as clojure.core/seq      |
|str        | x & args  | Same as clojure.core/str      |
|str-join   | j coll    | Same as clojure.string/join   |
|str-br     | coll      | Same as `(partial str/join "<br/>\n")` |
|str-newline| coll      | Same as `(partial str/join "\n")`      |


**Conditionals**

|Function|Arguments            |Description|
|--------|---------------------|-----------|
|when    | test f & args       | When `test` is true, invoke `f` with `args` |
|when-not| test f & args       | When `test` is false, invoke `f` with `args` |
|for-each| k-bindings f & args | Iterate over collection(s) and invoke `f` with `args` on each pass |


**Formatting**

|Function     | Arguments                |Description|
|-------------|--------------------------|-----------|
|format-rows  | rows decor & more        | Prefix/suffix every element in `rows` using `decor & more` |
|serial-decors| serial decor-fmt & more  | Create `decor(ation)` for `format-rows` using `serial` |
|html-tr      | rows                     | Same as `(format-rows rows "<tr>" "</tr>\n")` |
|html-li      | rows                     | Same as `(format-rows rows "<li>" "</li>\n")` |


**HTML-escaping**

|Function | Arguments | Description |
|---------|-----------|-------------|
|html-safe| x         | Encode as HTML-safe string |
|html-nbsp| x         | Encode as HTML-safe string converting space to `&nbsp;` |


**Including other templates**

|Function | Arguments | Description |
|---------|-----------|-------------|
|include  | t-name    | fetch template from group by name and render it as string |


## Phases of a template

### Parse & compile
### Rendering

## Template groups

### From a map
### From directory (on the JVM)
### From classpath (on the JVM)
