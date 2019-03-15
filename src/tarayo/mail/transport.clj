(ns tarayo.mail.transport
  (:import [javax.mail Session Transport]
           javax.mail.internet.MimeMessage))

(defn make-transport [^Session session ^String protocol]
  (.getTransport session protocol))

(defn connect! [^Transport transport smtp-server]
  (let [{:keys [host port user password]} smtp-server]
    (.connect transport host port user password)))

;  {:code 0 :error :SUCCESS :message "messages sent"}
(defn send! [^Transport transport ^MimeMessage message]
  (.sendMessage transport message (.getAllRecipients message)))
