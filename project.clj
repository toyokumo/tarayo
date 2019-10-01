(defproject tarayo "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [commons-codec "1.14"]
                 [com.sun.mail/jakarta.mail "1.6.4"]
                 [jakarta.mail/jakarta.mail-api "1.6.4"]
                 [nano-id "0.10.0"]
                 [org.apache.tika/tika-core "1.23"]
                 [camel-snake-kebab "0.4.1"]]

  :profiles
  {:dev {:dependencies [[org.clojure/clojure "1.9.0"]
                        [orchestra "2019.02.06-1"]
                        [com.github.kirviq/dumbster "1.7.1"]

                        ;; for benchmark
                        [criterium "0.4.5"]
                        [com.draines/postal "2.0.3"]
                        ;; for stubbing
                        [com.gearswithingears/shrubbery "0.4.1"]]
         :source-paths ["dev/src" "src"]
         :resource-paths ["dev/resources"]
         :global-vars {*warn-on-reflection* true}}

   :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.1"]]}
   :it [:dev {:dependencies [[org.clojure/data.json "1.0.0"]
                             [camel-snake-kebab "0.4.1"]
                             [http-kit "2.3.0"]]
              :test-paths ["integration/test"]}]}
  :aliases
  {"test-all" ["with-profile" "1.9,dev:1.10,dev" "test"]
   "test-integration" ["with-profile" "1.9,it:1.10,it" "test"]
   "benchmark" ["run" "-m" "benchmark"]}

  :plugins [[lein-cloverage "1.1.2"]]
  :cloverage {:ns-exclude-regex [#"user"
                                 #"benchmark"
                                 #"tarayo\.spec"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
