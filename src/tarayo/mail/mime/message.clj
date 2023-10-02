(ns tarayo.mail.mime.message
  (:require
   [tarayo.mail.mime.id :as id])
  (:import
   (jakarta.mail
    Message$RecipientType
    Multipart
    Session)
   (jakarta.mail.internet
    InternetAddress
    MimeMessage)
   java.util.Date))

(defn make-message
  ^MimeMessage
  [^Session session message]
  (proxy [MimeMessage] [^Session session]
    (updateMessageID
      []
      (.setHeader ^MimeMessage this
                  "Message-ID" ((:message-id-fn message id/get-random))))))

(def recipient-type-to Message$RecipientType/TO)
(def recipient-type-cc Message$RecipientType/CC)
(def recipient-type-bcc Message$RecipientType/BCC)

(defn add-to
  [^MimeMessage msg
   ^"[Ljakarta.mail.internet.InternetAddress;" addresses]
  (.addRecipients msg Message$RecipientType/TO addresses))

(defn add-cc
  [^MimeMessage msg
   ^"[Ljakarta.mail.internet.InternetAddress;"
   addresses]
  (.addRecipients msg Message$RecipientType/CC addresses))

(defn add-bcc
  [^MimeMessage msg
   ^"[Ljakarta.mail.internet.InternetAddress;" addresses]
  (.addRecipients msg Message$RecipientType/BCC addresses))

(defn set-from
  [^MimeMessage msg
   ^InternetAddress address]
  (.setFrom msg address))

(defn set-reply-to
  [^MimeMessage msg
   ^"[Ljakarta.mail.internet.InternetAddress;" addresses]
  (.setReplyTo msg addresses))

(defn set-subject
  [^MimeMessage msg ^String subject ^String charset]
  (.setSubject msg subject charset))

(defn set-sent-date
  [^MimeMessage msg ^Date sent-date]
  (.setSentDate msg sent-date))

(defn add-headers
  [^MimeMessage msg headers]
  (doseq [[k v] headers]
    (when (string? v)
      (.addHeader msg (cond-> k (keyword? k) name) v))))

(defn set-content
  ([^MimeMessage msg ^Multipart multipart]
   (.setContent msg multipart))
  ([^MimeMessage msg ^String content ^String content-type]
   (doto msg
     (.setContent content content-type)
     (.setHeader "Content-Type" content-type))))
