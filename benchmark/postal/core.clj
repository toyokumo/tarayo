(ns core
  (:require
   [criterium.core :as criterium]
   [helper :as h]
   [postal.core :as postal]))

(defn -main
  []
  (h/with-test-smtp-server [_ port]
    (println "POSTAL ----")
    (criterium/bench
     (postal/send-message {:host "localhost" :port port}
                          h/test-message))))
