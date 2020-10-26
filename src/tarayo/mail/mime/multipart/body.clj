(ns tarayo.mail.mime.multipart.body
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [tarayo.mail.constant :as constant]
   [tarayo.mail.mime.multipart.data-source :as data-source])
  (:import
   jakarta.activation.DataHandler
   (jakarta.mail.internet
    MimeBodyPart
    MimeUtility)
   (java.net
    MalformedURLException
    URL
    URLDecoder)
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
      ^String last
      (URLDecoder/decode charset)
      (MimeUtility/encodeText charset nil)))

(defn- ^MimeBodyPart make-attachment-bodypart-by-url
  [part charset]
  (let [{:keys [content filename]} part
        url (ensure-url content)]
    (doto (MimeBodyPart.)
      (.setDataHandler (DataHandler. url))
      (.setFileName (or filename (extract-file-name url charset)))
      (.setHeader "Content-Type" (get part :content-type (detect-mime-type url))))))

(defn- ^MimeBodyPart make-attachment-bodypart-by-byte-array
  [part]
  (assert (contains? part :content-type) ":content-type is required for byte array content.")
  (assert (contains? part :filename) ":filename is required for byte array content.")
  (assert (not (str/blank? (:content-type part))) ":content-type must be non-empty string.")
  (assert (not (str/blank? (:filename part))) ":filename must be non-empty string.")
  (let [{:keys [^"[B" content content-type filename]} part
        ds (data-source/byte-array-data-source content content-type)]
    (doto (MimeBodyPart.)
      (.setDataHandler (DataHandler. ds))
      (.setFileName filename)
      (.setHeader "Content-Type" content-type))))

(defn- make-attachment-bodypart
  [^String disposition part charset]
  ;; NOT using `bytes?` for backward compatibility
  (let [{:keys [id content-encoding]} part
        mbp (if (= (Class/forName "[B") (class (:content part)))
              (make-attachment-bodypart-by-byte-array part)
              (make-attachment-bodypart-by-url part charset))]
    (doto mbp
      (.setDisposition disposition)
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
