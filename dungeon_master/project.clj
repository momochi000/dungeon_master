(defproject dungeon_master "0.1.0-SNAPSHOT"
  :description "An experimental attempt to create a LLM-powered dungeon master who can run a paper and pencil tabletop role playing campaign"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [org.neo4j.driver/neo4j-java-driver "5.15.0"]
                 [net.clojars.wkok/openai-clojure "0.14.0"]
                 [cheshire "5.12.0"]
                 ;;[clj-http "2.0.0"] ; just to test adding deps
                 ]
  :main ^:skip-aot dungeon-master.core
  :source-paths ["src"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             ;; I moved these to ~/.lein/profiles.clj
             ;; but im not really a fan of that, i think it should go here
             ;; so wherever i pull this repo i have access to these
             ;;:dev {:plugins [
             ;;                [lein-pprint "1.3.2"]
             ;;                [io.aviso/pretty "1.4.4"]
             ;;                ]}
             ;;:repl {:plugins [
             ;;                 [lein-pprint "1.3.2"]
             ;;                 [io.aviso/pretty "1.4.4"]
             ;;                 ]}

             }
  :repl-options {:init-ns dungeon-master.repl-ns}
  )
