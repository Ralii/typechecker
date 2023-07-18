(defproject refactor-kit "0.1.0-SNAPSHOT"
  :description "Refactor kit and test bench for refactoring tools"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [metosin/malli "0.11.0"]]
  :main ^:skip-aot refactor-kit.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
