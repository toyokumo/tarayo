(ns tarayo.mail.transport
  (:import
   (com.sun.mail.smtp
    SMTPTransport)
   jakarta.mail.Session
   (jakarta.mail.internet
    MimeMessage)))

(defn- get-protocol
  ^String
  [^Session session]
  ;; c.f. https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary.html
  (if (= "true" (.getProperty session "mail.smtp.ssl.enable"))
    "smtps"
    "smtp"))

(defn make-transport
  ^SMTPTransport
  [^Session session]
  (.getTransport session (get-protocol session)))

(defn connect!
  "Make connection to the specified SMTP server.
  This connection will be closed by `tarayo.core/close`."
  [^SMTPTransport transport smtp-server]
  (let [{:keys [host port user password]} smtp-server]
    (.connect transport host port user password)))

(defn send!
  "Send a specified message via `SMTPTransport`."
  [^SMTPTransport transport ^MimeMessage message]
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
