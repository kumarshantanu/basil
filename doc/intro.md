# Introduction to Basil

TODO: write [great documentation](http://jacobian.org/writing/great-documentation/what-to-write/)

Basil is a templating library for Clojure. You may use it for generating HTML,
email content, SQL statements and so on. The dynamic parts of Basil templates
look and feel like Clojure expressions, which is by design.

Basil templates contain static and dynamic parts. As a user, you may choose
between two methods of using templates:

* **Method 1:** First compile a template. Subsequently, render a compiled
  template using _context_ (also called _environment_) data.
* **Method 2:** Create a _template group_ that returns compiled templates on
  demand. Subsequently, fetch a compiled template by name and render that using
  _context_ (also called _environment_) data.


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
|apply      | f & args  | Same as Apply clojure.core/apply |
|auto-str   | x         | Converts `x` (if collection, interposing with newline) to string. |
|default    | x         | Applied when a _slot_ has only data, e.g. <% foo %> |
|identity   | x         | Same as clojure.core/identity |
|partial    | f & args  | Same as clojure.core/partial  |
|seq        | x         | Same as clojure.core/seq      |
|str        | x & args  | Same as clojure.core/str      |
|str-join   | j coll    | Same as clojure.string/join   |
|str-br     | coll      | Same as `(partial clojure.string/join "<br/>\n")` |
|str-newline| coll      | Same as `(partial clojure.string/join "\n")`      |


**Conditionals**

|Function   |Arguments            |Description|
|-----------|---------------------|-----------|
|when       | test f & args       | When `test` is true, invoke `f` with `args` |
|when-not   | test f & args       | When `test` is false, invoke `f` with `args` |
|for-each   | k-bindings f & args | Iterate over collection(s) and invoke `f` with `args` on each pass |
|with-locals| locals f & args     | Invoke `f` with `args` in context of specified locals|


**Formatting**

|Function     | Arguments             |Description|
|-------------|-----------------------|-----------|
|format-rows  | rows decors           | Prefix/suffix every element in `rows` using `decors` |
|serial-decors| serial decor-fmt-coll | Create decoration for `format-rows` using `serial` |
|html-li      | rows                  | Same as `(format-rows rows ["<li>" "</li>\n"])` |
|html-option  | rows                  | Same as `(format-rows rows ["<option>" "</option>"])` |
|html-option-v| vals rows             | Same as `html-option` but assigns value='%s' from `vals` |
|mapseq->keys | mapseq                | Return the keys from sequence of maps |
|mapseq->rows | [mapseq] [mapseq ks]  | Return the sequence of rows values |
|html-tr      | [rows] [rows html-td] | Generate &lt;tr&gt;..&lt;/tr&gt; using `rows` data and `html-td` fn |
|html-th      | [rows]                | Generate &lt;th&gt;..&lt;/th&gt; using `rows` data |
|html-td      | [rows]                | Generate &lt;td&gt;..&lt;/td&gt; using `rows` data |


**HTML-escaping**

|Function | Arguments | Description |
|---------|-----------|-------------|
|html-safe| x         | Encode as HTML-safe string |
|html-nbsp| x         | Encode as HTML-safe string, also convert space to `&nbsp;` |


**Including other templates**

|Function | Arguments       | Description |
|---------|-----------------|-------------|
|include  | t-name & locals | fetch template from group by name and render it as string, using `locals` if specified |


## Phases of a template

So, what are the stages a templates goes through? What really happens when a
template is compiled? The sub-sections below briefly describe the inner working
of Basil.

### Parse & compile

When we say a template is compiled, it is actually parsed, then compiled.

During parsing the template string is tokenized, partitioned into static and
slot segments and stored as a sequence of tagged text. If there is a parsing
error, the process stops and the error is returned.

Once a template is parsed correctly, it is compiled by converting every text
in the sequence into an appropriate function that when executed with _context_,
would return string representation of that segment. A compiled template is thus
in object form that is usually stored in memory.

### Rendering

Rendering a template requires it to be compiled earlier. You must pass _context_
data to render a template. The _context_ is a collection of maps wherein the
'_slot_ symbols' are looked up during rendering a template. The symbols are
searched recursively in the Clojure S-expression and replaced by corresponding
values found in the _context_ data at once. There is no delayed evaluation and
no lexical scope when rendering a template _slot_.

Each segment (static or _slot_) in a compiled template is rendered independently
and concatenated into a single string in the end.

## Template groups

In many cases, you may need to work on a group of templates to serve a use-case.
For example, when using templates on the server side to render web pages you may
setup common templates for certain fragments of web pages, and a number of more
templates for every page. In such a scenario, you almost certainly need template
groups.

Basil supports creating template groups from a number of sources. At its core,
a template group is a protocol that anybody can use to implement own variety of
template groups.

### From a map

Creating a template group from a map is easy:

```clojure
(def g (basil.public/compile-template-group
         basil.public/slot-compiler
         {:tpl-1 "Hello, <% name %>!"
          :tpl-2 (slurp "tpl/activity.basil")
          :tpl-3 "<% (include :tpl-1) %>\n Welcome."}))
```

Note that you need a JVM-based '_slot_ compiler'. Rendering a group is easy:

```clojure
(basil.public/render-by-name g :tpl-1 [{:name "Morris"}])
=> "Hello, Morris!"
(basil.public/render-by-name g :tpl-3 [{:name "Morris"}])
=> "Hello, Morris!
Welcome."
```

### From directory (on the JVM)

Creating a template group from a directory is easier:

```clojure
(def g (basil.public/make-group-from-directory :prefix "templates/"))
```

The `:prefix` argument is optional and used to qualify the template filenames
with correct path. Another optional argument is `:cache-millis` with a default
value of 0 (no caching).

Rendering a template from a group is same for all kinds of template groups. See
the previous section _From a map_.

### From classpath (on the JVM)

Creating a template group from the classpath is just as easy as creating a group
from a directory. In most cases you would probably want to create a group from
classpath:

```clojure
(def g (basil.public/make-group-from-classpath :prefix "templates/"))
```

Make sure the directory 'templates' is in classpath (under 'src' or 'resources'
directory) of your Leiningen project.

Rendering a template from a group is same for all kinds of template groups. See
the previous section _From a map_.
