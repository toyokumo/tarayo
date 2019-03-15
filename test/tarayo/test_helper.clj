(ns tarayo.test-helper
  (:require [tarayo.core :as core]
            [tarayo.mail.session :as session]))

(defrecord TestConnection [session transport]
  tarayo.core.ITarayo
  (send! [this message] {:fn :send! :message message})
  (close [this] nil))

(def ^:private default-test-smtp-server
  {:host "localhost" :port 1025})

(defn test-connection
  ([] (test-connection default-test-smtp-server))
  ([smtp-server]
   (->TestConnection
    (session/make-session smtp-server)
    :dummy-transport)))
