(ns tarayo.core
  (:require [clojure.java.io :as io]
            [tarayo.mail.mime :as mime]
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

; (comment
;   (def server {:host "smtp.gmail.com"
;                :port 587
;                :tls true
;                :user "liquidz.uo@gmail.com"
;                :password "gaywuphmomvwvivq"
;                })
;   ; (def server {:host "localhost" :port 1025})
;
;   (with-open [conn (connect server)]
;     (send! conn {:from "liquidz.uo@gmail.com"
;                  :to "iizuka@cstap.com"
;                  :subject "hello"
;                  :body [
;                         {:type "text/html"
;                          :content "<h1>わーるど</h1>"}
;                         {:type :attachment
;                          :content (io/file "/home/uochan/tmp/uochan.png")}
;                         ]
;                  :charset "utf-8"
;                  })
;     )
;   )
;
