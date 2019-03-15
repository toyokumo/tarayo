(ns tarayo.mail.mime.message
  (:require [nano-id.custom :as nano-id])
  (:import java.util.Date
           [javax.mail Message Message$RecipientType Session]
           [javax.mail.internet InternetAddress MimeMessage]))

(def ^:private generate-id
  (nano-id/generate "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"))

(defn- default-message-id []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))]
    (format "<%s.%s@%s>" (generate-id 16) (.getTime (java.util.Date.)) (str "tarayo." hostname))))

(defn make-message [^Session session message]
  (proxy [MimeMessage] [^Session session]
    (updateMessageId []
      (.setHeader ^MimeMessage this
                  "Message-ID" ((:message-id-fn message default-message-id))))))

(defn add-to [^MimeMessage msg addresses]
  (.addRecipients msg Message$RecipientType/TO addresses))

(defn add-cc [^MimeMessage msg addresses]
  (.addRecipients msg Message$RecipientType/CC addresses))

(defn add-bcc [^MimeMessage msg addresses]
  (.addRecipients msg Message$RecipientType/BCC addresses))

(defn set-from [^MimeMessage msg ^InternetAddress address]
  (.setFrom msg address))

(defn set-subject [^MimeMessage msg ^String subject ^String charset]
  (.setSubject msg subject charset))

(defn set-sent-date [^MimeMessage msg ^Date sent-date]
  (.setSentDate msg sent-date))

(defn add-headers [^MimeMessage msg headers]
  (doseq [[k v] headers]
    (.addHeader msg (cond-> k (keyword? k) name) v)))

(defn set-body [^MimeMessage msg ^String body ^String charset]
  ;; FIXME
  ; (if (string? body)
  ;   (if (instance? MimeMessage jmsg)
  ;     (doto ^MimeMessage jmsg (.setText body charset))
  ;     (doto jmsg (.setText body)))
  ;   (doto jmsg (add-multipart! body)))
  (.setText msg body charset))

