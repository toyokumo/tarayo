(ns tarayo.mail.mime.multipart.body
  (:require [clojure.java.io :as io])
  (:import [java.net MalformedURLException URL]
           javax.activation.DataHandler
           javax.mail.internet.MimeBodyPart
           org.apache.tika.Tika))

(def ^Tika mime-detector (Tika.))

(defn- detect-mime-type [^URL url]
  (.detect mime-detector url))

(defn- ^String ensure-string [x]
  (if (keyword? x)
    (str (.sym ^clojure.lang.Keyword x))
    x))

(defmulti ^MimeBodyPart make-bodypart
  (fn [part _charset] (:type part)))

(defmethod make-bodypart :default
  [part charset]
  (let [content-type (format "%s; charset=%s"
                             (ensure-string (:type part))
                             charset)]
    (doto (MimeBodyPart.)
      (.setContent (:content part) content-type)
      (.setHeader "Content-Type" content-type))))

(defn- ^URL ensure-url [x]
  (try
    (io/as-url x)
    (catch MalformedURLException _
      (-> x io/as-file io/as-url))))

(def ^:private separator (System/getProperty "file.separator"))

(defn- extract-file-name [^URL url]
  (last (.split (.getPath url) separator)))

(defn- make-attachment-bodypart [part _charset]
  (let [url (-> part :content ensure-url)]
    (doto (MimeBodyPart.)
      (.setDataHandler (DataHandler. url))
      (.setFileName (extract-file-name url))
      (.setDisposition (-> part :type name))
      (.setHeader "Content-Type" (get part :content-type (detect-mime-type url))))))

(defmethod make-bodypart :inline
  [part charset]
  (make-attachment-bodypart part charset))

(defmethod make-bodypart :attachment
  [part charset]
  (make-attachment-bodypart part charset))
