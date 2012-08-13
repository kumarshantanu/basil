(defproject basil "0.2.0-SNAPSHOT"
  :description "Basil, a template library for Clojure"
  :url "https://github.com/kumarshantanu/basil"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;; :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies []}
             ;; aux is for CLJS testing
             :aux {:source-paths ["src" "test"]
                   :test-paths ["test"]
                   :cljsbuild {:crossovers [basil.core-test]
                               ;; Test command for running the unit tests
                               ;;     $ lein cljsbuild test
                               :test-commands {"unit" ["phantomjs"
                                                       "run-tests.js"]}
                               :builds {:test {:source-path "test-cljs"
                                               :compiler
                                               {:output-to "target/basil-test.js"
                                                ;; :optimizations nil
                                                :pretty-print true}}}}}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-alpha2"]]}}
  :aliases {"all" ["with-profile" "1.3,dev:1.4,dev:1.5,dev"]
            "dev" ["with-profile" "1.4,dev"]
            "aux" ["with-profile" "1.4,dev,aux"]}
  :warn-on-reflection true
  :plugins [[lein-cljsbuild "0.2.5"]]
  ;; Enable the lein hooks for: clean, compile, test, and jar.
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:crossovers [basil.core   basil.error  basil.group
                           basil.lib    basil.render basil.slot
                           basil.types  basil.util   basil.vars]
              :crossover-jar true
              :builds {:main {:source-path "src-cljs"
                              :compiler    {:output-to "target/basil.js"
                                            :optimizations :whitespace ;; :simple
                                            :pretty-print true}}
                       :mini {:source-path "src-cljs"
                              :compiler    {:output-to "target/basil-min.js"
                                            :optimizations :advanced
                                            :pretty-print false}}}})
