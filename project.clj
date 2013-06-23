(defproject basil "0.4.1"
  :description "A general purpose template library for Clojure"
  :url "https://github.com/kumarshantanu/basil"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[quiddity "0.2.0"]]
  :profiles {:tst {:dependencies [[clip-test "0.2.0"]]}
             :jst {:source-paths ["test"]
                   ;; Enable the lein hooks for: clean, compile, test, and jar.
                   :hooks [leiningen.cljsbuild]
                   :cljsbuild {:crossovers [basil.core   basil.error  basil.group
                                            basil.lib    basil.render basil.slot
                                            basil.types  basil.util   basil.vars
                                            basil.core-test
                                            basil.group-test
                                            basil.lib-test
                                            quiddity.core quiddity.lib
                                            clip-test.internal]
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
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}}
  :aliases {"all" ["with-profile" "1.2,tst:1.3,tst:1.4,tst:1.5,tst"]
            "dev" ["with-profile" "1.5,tst,jst"]}
  :warn-on-reflection true
  :plugins [[lein-cljsbuild "0.2.8"]])
