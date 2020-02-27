(ns tarayo.core
  (:require [tarayo.mail.mime :as mime]
            [tarayo.mail.session :as session]
            [tarayo.mail.transport :as transport])
  (:import javax.mail.Transport))

(defprotocol ISMTPConnection
  "FIXME"
  (send! [this message] "FIXME")
  (connected? [this] "FIXME")
  (close [this] "FIXME"))

(defrecord SMTPConnection [session transport]
  ISMTPConnection
  (send! [this message]
    (->> message
         (mime/make-message (:session this))
         (transport/send! (:transport this))))

  (connected? [this]
    (.isConnected ^Transport (:transport this)))

  (close [this]
    (.close ^Transport (:transport this))))

(defn- get-protocol [smtp-server]
  ;; c.f. https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary.html
  (if (contains? smtp-server :ssl.enable)
    "smtps"
    "smtp"))

(defn- get-defaults [{:keys [user] :as smtp-server}]
  {:host "localhost"
   :port (cond
           (contains? smtp-server :ssl.enable) 465
           (contains? smtp-server :starttls.enable) 587
           :else 25)
   :auth (some? user)})

(defn ^SMTPConnection
  connect
  "smtp-server
  {:host 'localhost' :port 1025}
  "
  ([] (connect {}))
  ([smtp-server]
   (let [smtp-server (merge (get-defaults smtp-server)
                            smtp-server)
         sess (session/make-session smtp-server)]
     (map->SMTPConnection
      {:session sess
       :transport (doto ^Transport (transport/make-transport sess (get-protocol smtp-server))
                    (transport/connect! smtp-server))}))))
