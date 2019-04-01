(ns tarayo.mail.transport
  (:import com.sun.mail.smtp.SMTPTransport
           javax.mail.internet.MimeMessage
           javax.mail.Session))

(defn ^SMTPTransport make-transport [^Session session ^String protocol]
  (.getTransport session protocol))

(defn connect! [^SMTPTransport transport smtp-server]
  (let [{:keys [host port user password]} smtp-server]
    (.connect transport host port user password)))

(defn send! [^SMTPTransport transport ^MimeMessage message]
  (try
    (.sendMessage transport message (.getAllRecipients message))
    {:result :success
     :code (.getLastReturnCode transport)
     :message (.getLastServerResponse transport)}
    (catch Throwable ex
      {:result :failed
       :code (.getLastReturnCode transport)
       :message (.getMessage ex)
       :cause ex})))
