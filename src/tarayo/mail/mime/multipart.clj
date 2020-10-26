(ns tarayo.mail.mime.multipart
  (:require
   [tarayo.mail.mime.multipart.body :as body])
  (:import
   (jakarta.mail.internet
    MimeMultipart)))

(defn- add-body-parts
  [^MimeMultipart multipart parts ^String charset]
  (doseq [part parts]
    (.addBodyPart multipart (body/make-bodypart part charset))))

(defn ^MimeMultipart make-multipart
  [^String multipart-type parts charset]
  (doto (MimeMultipart. multipart-type)
    (add-body-parts parts charset)))
