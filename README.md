# basil

Basil is a general purpose template library for Clojure and ClojureScript.

**Important:** Consider this library in _ALPHA_ until this notice is removed.
API and implementation will change in incompatible ways. You have been warned.


## Usage

On Clojars: https://clojars.org/basil

Leiningen dependency: `[basil "0.4.0"]`

Supported Clojure versions: 1.2, 1.3, 1.4, 1.5

Tested with lein-cljsbuild version: 0.2.8

This page has only elementary documentation.
[Intro page](https://github.com/kumarshantanu/basil/blob/master/doc/intro.md)
has more details.

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
    <td>foo &lt;% (inc (inc num)) %&gt; bar</td>
    <td>{:inc inc :num 45}</td>
    <td>foo 47 bar</td>
  </tr>
  <tr>
    <td><pre>foo
&lt;% (when-not (seq names) "No names!") %&gt;
bar</pre></td>
    <td>{:names []}</td>
    <td>foo<br/>
No names!<br/>
bar</td>
  </tr>
  <tr>
    <td><pre>foo
&lt;% (when (seq names) (str-br names)) %&gt;
bar</pre></td>
    <td>{:names ["Lucy" "Brian"]}</td>
    <td>foo<br/>
Lucy&lt;br/&gt;<br/>
Brian&lt;br/&gt;<br/>
bar</td>
  </tr>
  <tr>
    <td><pre>foo
&lt;% (for-each [b names] (str "name=" b)) %&gt;
bar</pre></td>
    <td>{:names ["Tom" "Jane" "Larry"]}</td>
    <td>foo<br/>
name=Tom<br/>
name=Jane<br/>
name=Larry<br/>
bar</td>
  </tr>
  <tr>
    <td><pre>foo
&lt;% (for-each [a [1 2]
              b names]
     (str ":a=" a
          ", name=" b)) %&gt;
bar</pre></td>
    <td>{:names ["Tom" "Jane" "Larry"]}</td>
    <td>foo<br/>
:a=1, name=Tom<br/>
:a=1, name=Jane<br/>
:a=1, name=Larry<br/>
:a=2, name=Tom<br/>
:a=2, name=Jane<br/>
:a=2, name=Larry<br/>
bar</td>
  </tr>
</table>

Use `basil.public/parse-compile` to parse and compile a template and use
`basil.public/render-template` to render a compiled template.


### Template Groups

Template groups can be created from directory or classpath. A slot in one
template can include an entire template dynamically using
`<% (include "foo.basil") %>`. You may also create additional context for
included template using `<% (include "foo.basil" {:a 10 :b 20} {:items xs}) %>`.

Use `basil.public/make-group-from-directory` and
`basil.public/make-group-from-classpath` to create template groups. Use
`basil.public/render-by-name` to render a template from a group.


## Getting in touch

By e-mail: kumar.shantanu(at)gmail.com

On Twitter: [@kumarshantanu](http://twitter.com/kumarshantanu)


## License

Copyright © 2012 Shantanu Kumar

Distributed under the Eclipse Public License, the same as Clojure.
