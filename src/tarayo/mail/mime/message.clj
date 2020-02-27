(ns tarayo.mail.mime.message
  (:require [tarayo.mail.mime.id :as id])
  (:import java.util.Date
           [javax.mail Message Message$RecipientType Multipart Session]
           [javax.mail.internet InternetAddress MimeMessage]))

(defn ^MimeMessage make-message [^Session session message]
  (proxy [MimeMessage] [^Session session]
    (updateMessageID []
      (.setHeader ^MimeMessage this
                  "Message-ID" ((:message-id-fn message id/get-random))))))

(def recipient-type-to Message$RecipientType/TO)
(def recipient-type-cc Message$RecipientType/CC)
(def recipient-type-bcc Message$RecipientType/BCC)

(defn add-to [^MimeMessage msg
              ^"[Ljavax.mail.internet.InternetAddress;" addresses]
  (.addRecipients msg Message$RecipientType/TO addresses))

(defn add-cc [^MimeMessage msg
              ^"[Ljavax.mail.internet.InternetAddress;"
              addresses]
  (.addRecipients msg Message$RecipientType/CC addresses))

(defn add-bcc [^MimeMessage msg
               ^"[Ljavax.mail.internet.InternetAddress;" addresses]
  (.addRecipients msg Message$RecipientType/BCC addresses))

(defn set-from [^MimeMessage msg ^InternetAddress
                ^"[Ljavax.mail.internet.InternetAddress;" address]
  (.setFrom msg address))

(defn set-subject [^MimeMessage msg ^String subject ^String charset]
  (.setSubject msg subject charset))

(defn set-sent-date [^MimeMessage msg ^Date sent-date]
  (.setSentDate msg sent-date))

(defn add-headers [^MimeMessage msg headers]
  (doseq [[k v] headers]
    (.addHeader msg (cond-> k (keyword? k) name) v)))

(defn set-content
  ([^MimeMessage msg ^Multipart multipart]
   (.setContent msg multipart))
  ([^MimeMessage msg ^String content ^String content-type]
   (doto msg
     (.setContent content content-type)
     (.setHeader "Content-Type" content-type))))
