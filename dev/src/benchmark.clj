(ns benchmark
  (:require [criterium.core :as criterium]
            [postal.core :as postal]
            [tarayo.core :as tarayo]
            [tarayo.test-helper :as h]))

(def ^:private message
  {:from "alice@example.com"
   :to "bob@example.com"
   :subject "hello"
   :body "world"})

(defn -main []
  (h/with-test-smtp-server [_ port]
    (println "POSTAL ----")
    (criterium/bench
     (postal/send-message {:host "localhost" :port port}
                          message)))

  (println "")

  (h/with-test-smtp-server [_ port]
    (println "TARAYO ----")
    (criterium/bench
     (with-open [conn (tarayo/connect {:port port})]
       (tarayo/send! conn message)))))

(comment (-main))
