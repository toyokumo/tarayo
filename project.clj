(defproject toyokumo/tarayo
  #=(clojure.string/trim #=(slurp "resources/VERSION"))
  :description "SMTP client library for Clojure. That’s it."
  :url "https://github.com/toyokumo/tarayo"
  :license {:name "Apache, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[camel-snake-kebab "0.4.3"]
                 [org.eclipse.angus/angus-mail "2.0.3"]
                 [commons-codec "1.16.1"]
                 [jakarta.mail/jakarta.mail-api "2.1.3"]
                 [nano-id "1.1.0"]
                 [org.apache.tika/tika-core "2.9.1"]]

  :profiles
  {:1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}

   :dev {:dependencies [[org.clojure/clojure "1.11.1"]
                        [com.github.kirviq/dumbster "1.7.1"]
                        [testdoc "1.4.1"]
                        ;; for stubbing
                        [com.gearswithingears/shrubbery "0.4.1"]]
         :source-paths ["dev/src" "src"]
         :resource-paths ["dev/resources"]
         :global-vars {*warn-on-reflection* true}}

   :it {:dependencies [[org.clojure/data.json "2.5.0"]
                       [http-kit "2.7.0"]]
        :test-paths ["integration/test"]}

   :antq {:dependencies [[com.github.liquidz/antq "RELEASE"]]}}
  :aliases
  {"test-all" ["with-profile" "1.8,dev:1.9,dev:1.10,dev:dev" "test"]
   "test-integration" ["with-profile" "1.9,dev,it:1.10,dev,it" "test"]
   "antq" ["with-profile" "+antq" "run" "-m" "antq.core"]}

  :plugins [[lein-cloverage "1.2.4"]]
  :cloverage {:ns-exclude-regex [#"benchmark"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
