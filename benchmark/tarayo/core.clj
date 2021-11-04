(ns core
  (:require
   [criterium.core :as criterium]
   [helper :as h]
   [tarayo.core :as tarayo]))

(defn -main
  []
  (h/with-test-smtp-server [_ port]
    (println "TARAYO ----")
    (criterium/bench
     (with-open [conn (tarayo/connect {:port port})]
       (tarayo/send! conn h/test-message)))))
