(ns tarayo.mail.mime.multipart.body
  (:require
   [clojure.java.io :as io]
   [tarayo.mail.constant :as constant])
  (:import
   (java.net
    MalformedURLException
    URL
    URLDecoder)
   javax.activation.DataHandler
   (javax.mail.internet
    MimeBodyPart
    MimeUtility)
   org.apache.tika.Tika))

(def ^Tika mime-detector (Tika.))

(def ^:private separator (System/getProperty "file.separator"))

(defn- detect-mime-type
  [^URL url]
  (.detect mime-detector url))

(defn- ^String ensure-string
  [x]
  (if (keyword? x)
    (str (.sym ^clojure.lang.Keyword x))
    x))

(defn- ^URL ensure-url
  [x]
  (try
    (io/as-url x)
    (catch MalformedURLException _
      (-> x io/as-file io/as-url))))

(defn- make-text-bodypart
  [part charset]
  (let [content-type (or (:content-type part) constant/default-content-type)
        content-type (format "%s; charset=%s"
                             (ensure-string content-type)
                             charset)]
    (doto (MimeBodyPart.)
      (.setContent (:content part) content-type)
      (.setHeader "Content-Type" content-type))))

(defn- extract-file-name
  [^URL url ^String charset]
  (-> (.getPath url)
      (.split separator)
      last
      (URLDecoder/decode charset)
      (MimeUtility/encodeText charset nil)))

(defn- make-attachment-bodypart
  [disposition part charset]
  (let [url (-> part :content ensure-url)
        filename (or (:filename part) (extract-file-name url charset))
        {:keys [id content-encoding]} part]
    (doto (MimeBodyPart.)
      (.setDataHandler (DataHandler. url))
      (.setFileName filename)
      (.setDisposition disposition)
      (.setHeader "Content-Type" (get part :content-type (detect-mime-type url)))
      (cond-> id (.setContentID (str "<" id ">")))
      (cond-> content-encoding (.setHeader "Content-Transfer-Encoding" content-encoding)))))

(defn ^MimeBodyPart make-bodypart
  [part charset]
  (let [string-content? (-> part :content string?)
        has-id? (contains? part :id)]
    (cond
      string-content? (make-text-bodypart part charset)
      has-id? (make-attachment-bodypart "inline" part charset)
      :else (make-attachment-bodypart "attachment" part charset))))
