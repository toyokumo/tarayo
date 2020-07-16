(defproject toyokumo/tarayo
  #=(clojure.string/trim #=(slurp "resources/VERSION"))
  :description "SMTP client library for Clojure. That’s it."
  :url "https://github.com/toyokumo/tarayo"
  :license {:name "Apache, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[camel-snake-kebab "0.4.1"]
                 [com.sun.mail/jakarta.mail "1.6.5"]
                 [commons-codec "1.14"]
                 [jakarta.mail/jakarta.mail-api "1.6.5"]
                 [nano-id "1.0.0"]
                 [org.apache.tika/tika-core "1.24.1"]]

  :profiles
  {:1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.1"]]}

   :dev {:dependencies [[org.clojure/clojure "1.10.1"]
                        [com.github.kirviq/dumbster "1.7.1"]
                        [testdoc "1.4.0"]
                        ;; for stubbing
                        [com.gearswithingears/shrubbery "0.4.1"]]
         :source-paths ["dev/src" "src"]
         :resource-paths ["dev/resources"]
         :global-vars {*warn-on-reflection* true}}

   :it {:dependencies [[org.clojure/data.json "1.0.0"]
                       [camel-snake-kebab "0.4.1"]
                       [http-kit "2.3.0"]]
        :test-paths ["integration/test"]}

   :benchmark {:dependencies [[criterium "0.4.5"]
                              [com.draines/postal "2.0.3"]]}

   :antq {:dependencies [[antq "RELEASE"]]}}
  :aliases
  {"test-all" ["with-profile" "1.8,dev:1.9,dev:1.10,dev" "test"]
   "test-integration" ["with-profile" "1.9,dev,it:1.10,dev,it" "test"]
   "benchmark" ["with-profile" "+benchmark" "run" "-m" "benchmark"]
   "antq" ["with-profile" "+antq" "run" "-m" "antq.core"]}

  :plugins [[lein-cloverage "1.1.2"]]
  :cloverage {:ns-exclude-regex [#"benchmark"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
