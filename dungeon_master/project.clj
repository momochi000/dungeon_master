(defproject dungeon_master "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 ;;[clj-http "2.0.0"] ; just to test adding deps
                 [clojurewerkz/neocons "3.2.0"]
                 [net.clojars.wkok/openai-clojure "0.14.0"] ]
  :main ^:skip-aot dungeon-master.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
