(ns tarayo.mail.mime.multipart
  (:import [javax.mail.internet MimeMultipart ])
  )




;
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
;
; (defn eval-multipart [parts]
;   (let [;; multiparts can have a number of different types: mixed,
;         ;; alternative, encrypted...
;         ;; The caller can use the first two entries to specify a type.
;         ;; If no type is given, we default to "mixed" (for attachments etc.)
;         [^String multiPartType, parts] (if (keyword? (first parts))
;                                          [(name (first parts)) (rest parts)]
;                                          ["mixed" parts])
;         mp (javax.mail.internet.MimeMultipart. multiPartType)]
;     (doseq [part parts]
;       (.addBodyPart mp (eval-part part)))
;     mp))


(defn- multipart-type [parts]
  (if (keyword? (first parts))
    [(name (first parts)) (rest parts)]
    ["mixed" parts]))

(defn make-multipart [parts]
  (let [[multipart-type parts] (multipart-type parts)]
    (MimeMultipart. multipart-type)
    ))


