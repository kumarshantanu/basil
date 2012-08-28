(defproject basil "0.3.0-SNAPSHOT"
  :description "A template library for Clojure"
  :url "https://github.com/kumarshantanu/basil"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [;[org.clojure/clojure "1.4.0"]
                 [quiddity "0.1.0-SNAPSHOT"]]
  :profiles {;; Clojure
             :dev {:dependencies [[mini-test "0.1.0-SNAPSHOT"]]}
             ;; CLJS testing
             :jst {:source-paths ["test"]
                   ;; Enable the lein hooks for: clean, compile, test, and jar.
                   :hooks [leiningen.cljsbuild]
                   :cljsbuild {:crossovers [basil.core   basil.error  basil.group
                                            basil.lib    basil.render basil.slot
                                            basil.types  basil.util   basil.vars
                                            basil.core-test mini-test.internal]
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
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0-alpha4"]]}}
  :aliases {"all" ["with-profile" "1.2,dev:1.3,dev:1.4,dev:1.5,dev"]
            "dev" ["with-profile" "1.4,dev,jst"]}
  :warn-on-reflection true
  :plugins [[lein-cljsbuild "0.2.6"]])