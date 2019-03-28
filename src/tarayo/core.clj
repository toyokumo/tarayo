(ns tarayo.core
  (:require [tarayo.mail.mime :as mime]
            [tarayo.mail.session :as session]
            [tarayo.mail.transport :as transport])
  (:import javax.mail.Transport))

(defprotocol ITarayo
  (send! [this message])
  (close [this]))

(defrecord TarayoConnection [session transport]
  ITarayo
  (send! [this message]
    (->> message
         (mime/make-message (:session this))
         (transport/send! (:transport this))))

  (close [this] (.close (:transport this))))

(defn- get-defaults [{:keys [ssl user]}]
  {:port (if ssl 465 25)
   :auth (some? user)
   :protocol (if ssl "smtps" "smtp")})

(defn connect
  "smtp-server
  {:host 'localhost' :port 1025}
  "
  [smtp-server]
  (let [smtp-server (merge (get-defaults smtp-server)
                           smtp-server)
        sess (session/make-session smtp-server)]
    (map->TarayoConnection
     {:session sess
      :transport (doto ^Transport (transport/make-transport sess (:protocol smtp-server))
                   (transport/connect! smtp-server))})))
