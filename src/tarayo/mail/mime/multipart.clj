(ns tarayo.mail.mime.multipart
  (:require [tarayo.mail.mime.multipart.body :as body])
  (:import [javax.mail.internet MimeMultipart]
           javax.mail.BodyPart))

(defn- add-body-parts [^MimeMultipart multipart parts ^String charset]
  (doseq [part parts]
    (.addBodyPart multipart ^BodyPart (body/make-bodypart part charset))))

(defn- multipart-type [parts]
  (if (keyword? (first parts))
    [(name (first parts)) (rest parts)]
    ["mixed" parts]))

(defn make-multipart [parts charset]
  (let [[multipart-type parts] (multipart-type parts)]
    (doto (MimeMultipart. multipart-type)
      (add-body-parts parts charset))))


