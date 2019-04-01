(ns tarayo.mail.mime.multipart.body
  (:require [clojure.java.io :as io])
  (:import [java.net MalformedURLException URL URLDecoder]
           javax.activation.DataHandler
           [javax.mail.internet MimeBodyPart MimeUtility]
           org.apache.tika.Tika))

(def ^Tika mime-detector (Tika.))

(defn- detect-mime-type [^URL url]
  (.detect mime-detector url))

(defn- ^String ensure-string [x]
  (if (keyword? x)
    (str (.sym ^clojure.lang.Keyword x))
    x))

(defmulti ^MimeBodyPart make-bodypart
  (fn [part _charset] (keyword (:type part))))

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

(defn- extract-file-name [^URL url ^String charset]
  (-> (.getPath url)
      (.split separator)
      last
      (URLDecoder/decode charset)
      (MimeUtility/encodeText charset nil)))

(defn- make-attachment-bodypart [part charset]
  (let [url (-> part :content ensure-url)
        {:keys [id content-encoding]} part]
    (doto (MimeBodyPart.)
      (.setDataHandler (DataHandler. url))
      (.setFileName (extract-file-name url charset))
      (.setDisposition (-> part :type name))
      (.setHeader "Content-Type" (get part :content-type (detect-mime-type url)))
      (cond-> id (.setContentID (str "<" id ">")))
      (cond-> content-encoding (.setHeader "Content-Transfer-Encoding" content-encoding)))))

(defmethod make-bodypart :inline
  [part charset]
  (make-attachment-bodypart part charset))

(defmethod make-bodypart :attachment
  [part charset]
  (make-attachment-bodypart part charset))
