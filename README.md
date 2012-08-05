# basil

Basil is a flavored templating library for Clojure, inspired by a number of
templating systems such as Enlive, JSTL and Jinja2.

**Important:** Consider this library in _alpha_ until this notice is removed.
API and implementation will change in incompatible ways. You have been warned.


## Usage

_This library is not on Clojars yet._

Leiningen dependency: `[basil "0.1.0-SNAPSHOT"]`


### Basic Templates

A Basil template can be parsed from a plain string. Every Basil template is
composed of distinct, interleaved and non-nested _static-text_ and _slots_. The
_slots_ contain limited Clojure forms to express the _filter-functions_ and
_dynamic data_. Few examples:

<table>
  <tr>
    <th>Template</th>
    <th>Clojure data</th>
    <th>Result</th>
  </tr>
  <tr>
    <td>foo bar</td>
    <td>{}</td>
    <td>foo bar</td>
  </tr>
  <tr>
    <td>foo &lt;% num %&gt; bar</td>
    <td>{:num 45}</td>
    <td>foo 45 bar</td>
  </tr>
  <tr>
    <td>foo &lt;% (inc num) %&gt; bar</td>
    <td>{:inc inc :num 45}</td>
    <td>foo 46 bar</td>
  </tr>
  <tr>
    <td><pre>foo
&lt;% (str-join "\n"
     (for-each [:a [1 2]
                :b names]
       str ":a=" :a
           ", name=" :b) %&gt;
bar</pre></td>
    <td><pre>{:names ["Tom"
         "Jane"
         "Larry"]}</pre>
    </td>
    <td><pre>foo
:a=1, name=Tom
:a=1, name=Jane
:a=1, name=Larry
:a=2, name=Tom
:a=2, name=Jane
:a=2, name=Larry
bar</pre>
    </td>
  </tr>
</table>

Use `basil.core/parse-compile` to parse and compile a template and use
`basil.core/render-template` to render a compiled template.


### Template Groups

Template groups can be created from directory or classpath. A slot in one
template can include an entire template dynamically using
`<% (include "foo.basil") %>`.

Use `basil.jvm/make-group-from-directory` and
`basil.jvm/make-group-from-classpath` to create template groups. Use
`basil.core/render-by-name` to render a template from a group.


## Getting in touch

By e-mail: kumar.shantanu(at)gmail.com

On Twitter: [@kumarshantanu](http://twitter.com/kumarshantanu)


## License

Copyright © 2012 Shantanu Kumar

Distributed under the Eclipse Public License, the same as Clojure.
