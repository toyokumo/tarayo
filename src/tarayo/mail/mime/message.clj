(ns tarayo.mail.mime.message
  (:require [nano-id.custom :as nano-id])
  (:import java.util.Date
           [javax.mail Message Message$RecipientType Session]
           [javax.mail.internet InternetAddress MimeMessage]
           javax.mail.Multipart))

(def ^:private generate-id
  (nano-id/generate "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"))

(defn- default-message-id []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))]
    (format "<%s.%s@%s>" (generate-id 16) (.getTime (java.util.Date.)) (str "tarayo." hostname))))

(defn make-message [^Session session message]
  (proxy [MimeMessage] [^Session session]
    (updateMessageID []
      (.setHeader ^MimeMessage this
                  "Message-ID" ((:message-id-fn message default-message-id))))))

(def recipient-type-to Message$RecipientType/TO)
(def recipient-type-cc Message$RecipientType/CC)
(def recipient-type-bcc Message$RecipientType/BCC)

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

(defn set-text [^MimeMessage msg ^String body ^String charset]
  (.setText msg body charset))

(defn set-content [^MimeMessage msg ^Multipart multipart]
  (.setContent msg multipart))
