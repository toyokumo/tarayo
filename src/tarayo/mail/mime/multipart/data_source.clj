(ns tarayo.mail.mime.multipart.data-source
  (:require
   [clojure.java.io :as io])
  (:import
   jakarta.activation.DataSource))

(defn byte-array-data-source
  "Return jakarta.activation.DataSource instance by byte array."
  (^DataSource [^"[B" buf]
   (byte-array-data-source buf "application/octet-stream"))
  (^DataSource [^"[B" buf ^String content-type]
   (proxy [DataSource] []
     (getInputStream []
       (io/input-stream buf))
     (getOutputStream []
       (throw (UnsupportedOperationException. "Read only")))
     (getContentType []
       content-type)
     (getName []
       "ByteArrayDataSource"))))
