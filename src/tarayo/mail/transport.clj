(ns tarayo.mail.transport
  (:import [javax.mail Session Transport]
           javax.mail.internet.MimeMessage))

(defn make-transport [^Session session ^String protocol]
  (.getTransport session protocol))

(defn connect! [^Transport transport smtp-server]
  (let [{:keys [host port user password]} smtp-server]
    (.connect transport host port user password)))

(defn send! [^Transport transport ^MimeMessage message]
  (try
    (.sendMessage transport message (.getAllRecipients message))
    {:result :success}
    (catch Throwable ex
      {:result :fail :message (.getMessage ex) :cause ex})))
