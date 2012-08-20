(defproject basil "0.2.0"
  :description "Basil, a template library for Clojure"
  :url "https://github.com/kumarshantanu/basil"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;; :dependencies [[org.clojure/clojure "1.4.0"]]
  :source-paths ["src"]
  :test-paths   ["test"]
  :profiles {;; Clojure
             :dev {:dependencies []
                   :source-paths ["src-jvm"]
                   ;;:test-paths   ["test"]
                   }
             ;; CLJS
             :js  {;;:source-paths []
                   ;;:test-paths   []
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
                                             :pretty-print false}}}}}
             ;; CLJS testing
             :jst {:source-paths ["src-cljs" "test" "test-cljs-macro"]
                   ;; :test-paths   []
                   ;; Enable the lein hooks for: clean, compile, test, and jar.
                   :hooks [leiningen.cljsbuild]
                   :cljsbuild {:crossovers [basil.core   basil.error  basil.group
                                            basil.lib    basil.render basil.slot
                                            basil.types  basil.util   basil.vars
                                            basil.core-test]
                               ;; Test command for running the unit tests
                               ;;     $ lein cljsbuild test
                               :test-commands {"unit" ["phantomjs"
                                                       "run-tests.js"]}
                               :builds {:test {:source-path "test-cljs"
                                               :compiler
                                               {:output-to "target/basil-test.js"
                                                ;; :optimizations nil
                                                :pretty-print true}}}}}
             :1.2 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-alpha3"]]}}
  :aliases {"all" ["with-profile" "1.2,dev:1.3,dev:1.4,dev:1.5,dev"]
            "dev" ["with-profile" "1.4,dev"]
            "js"  ["with-profile" "1.4,js"]
            "jst" ["with-profile" "1.4,jst"]}
  :warn-on-reflection true
  :plugins [[lein-cljsbuild "0.2.5"]])