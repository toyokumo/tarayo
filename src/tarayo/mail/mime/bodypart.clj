(ns tarayo.mail.mime.bodypart
  (:require [clojure.java.io :as io])
  (:import java.net.MalformedURLException
           java.util.UUID
           javax.activation.DataHandler
           [javax.mail Message Message$RecipientType PasswordAuthentication Session]
           [javax.mail.internet InternetAddress MimeMessage]
           javax.mail.internet.MimeBodyPart))




; (declare eval-bodypart eval-multipart)
;
; (defprotocol PartEval (eval-part [part]))
;
; (extend-protocol PartEval
;   clojure.lang.IPersistentMap
;   (eval-part [part] (eval-bodypart part))
;   clojure.lang.IPersistentCollection
;   (eval-part [part]
;     (doto (javax.mail.internet.MimeBodyPart.)
;       (.setContent (eval-multipart part)))))
;
; (defn- encode-filename [filename]
;   (. javax.mail.internet.MimeUtility encodeText filename "UTF-8" nil))
;
; (defn eval-bodypart [part]
;   (condp (fn [test type] (some #(= % type) test)) (:type part)
;     [:inline :attachment]
;     (let [url (make-url (:content part))]
;       (doto (javax.mail.internet.MimeBodyPart.)
;         (.setDataHandler (DataHandler. url))
;         (.setFileName (-> (re-find #"[^/]+$" (.getPath url))
;                           encode-filename))
;         (.setDisposition (name (:type part)))
;         (cond-> (:content-type part)
;           (.setHeader "Content-Type" (:content-type part)))
;         (cond-> (:content-id part)
;           (.setContentID (str "<" (:content-id part) ">")))
;         (cond-> (:file-name part)
;           (.setFileName (encode-filename (:file-name part))))
;         (cond-> (:description part)
;           (.setDescription (:description part)))))
;     (doto (javax.mail.internet.MimeBodyPart.)
;       (.setContent (:content part) (:type part))
;       (cond-> (:file-name part)
;         (.setFileName (encode-filename (:file-name part))))
;       (cond-> (:description part)
;         (.setDescription (:description part))))))

(defmulti make-bodypart :type)

(defmethod make-bodypart :default
  [part]
  (let [content-type (str (.sym (:type part)))]
    (doto (MimeBodyPart.)
      (.setContent (:content part) content-type))))



(defn- ^java.net.URL make-url [x]
  (try
    (io/as-url x)
    (catch MalformedURLException _
      (io/as-url (io/as-file x)))))

(defn ^String message->str [msg]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (let [^javax.mail.Message jmsg (if (instance? MimeMessage msg)
                                     msg (make-jmessage msg))]
      (.writeTo jmsg out)
      (str out))))


(defn make-body-part []
  (MimeBodyPart.))


         (.setDataHandler (DataHandler. url))
