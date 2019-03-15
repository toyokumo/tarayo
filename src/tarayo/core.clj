(ns tarayo.core
  (:import [javax.mail Session Transport]
           [java.util Properties Random]
           java.net.MalformedURLException
           java.util.UUID
           javax.activation.DataHandler
           [javax.mail Message Message$RecipientType PasswordAuthentication Session]
           [javax.mail.internet InternetAddress MimeMessage
            MimeMultipart MimeBodyPart]
           [org.apache.commons.codec.binary Base64])

  (:require [clojure.spec.alpha :as s]
            [clojure.set :as set]
            [tarayo.mail.session :as session]
            [tarayo.mail.transport :as transport]
            [tarayo.mail.mime :as mime]
            [nano-id.custom :as nano-id]
            [clojure.java.io :as io]
            ))

(defprotocol ITarayo
  (send! [this message])
  (close [this]))

(defrecord TarayoConnection [session transport]
  ITarayo
  (send! [this message]
    (->> message
         (mime/make-message (:session this))
         (transport/send! (:transport this))))

  (close [this] (.close (:transport this))))

(defn- get-defaults [{:keys [ssl user]}]
  {:port (if ssl 465 25)
   :auth (some? user)
   :protocol "smtp"})

(defn connect
  "smtp-server
  {:host 'localhost' :port 1025}
  "
  [smtp-server]
  (let [smtp-server (merge (get-defaults smtp-server)
                           smtp-server)
        sess (session/make-session smtp-server)]
    (map->TarayoConnection
     {:session sess
      :transport (doto ^Transport (transport/make-transport sess (:protocol smtp-server))
                   (transport/connect! smtp-server))})))

(comment
  (with-open [conn (connect {:host "localhost" :port 1025})]
    (send! conn {:from "liquidz.uo@gmail.com"
                 :to "iizuka@cstap.com"
                 :subject "hello"
                 :body "world" })
    )
  )

;;; (defn eval-multipart [parts]
;;;   (let [;; multiparts can have a number of different types: mixed,
;;;         ;; alternative, encrypted...
;;;         ;; The caller can use the first two entries to specify a type.
;;;         ;; If no type is given, we default to "mixed" (for attachments etc.)
;;;         [^String multiPartType, parts] (if (keyword? (first parts))
;;;                                          [(name (first parts)) (rest parts)]
;;;                                          ["mixed" parts])
;;;         mp (javax.mail.internet.MimeMultipart. multiPartType)]
;;;     (doseq [part parts]
;;;       (.addBodyPart mp (eval-part part)))
;;;     mp))
;;
;;(def attachment-body-part-types #{:inline :attachment})
;;
;;(defn ^java.net.URL as-url [x]
;;  (try
;;    (io/as-url x)
;;    (catch MalformedURLException _
;;      (io/as-url (io/as-file x)))))
;;
;;(defn- body-part [part]
;;  (if-let [body-part-type (attachment-body-part-types (:type part))]
;;    (let [url (as-url (:content part))]
;;      (doto (MimeBodyPart.)
;;        (.setDataHandler (DataHandler. url))
;;        )
;;      )
;;
;;    )
;;  )
;;
;;(defn multipart [parts]
;;  (let [[^String multipart-type parts] (if (keyword? (first parts))
;;                                         [(name (first parts)) (rest parts)]
;;                                         ["mixed" parts])
;;        mmp (MimeMultipart. multipart-type)
;;        ]
;;    (doseq [part parts]
;;      (.addBodyPart mmp )
;;      )
;;    ))
;;
;;(defn- set-text! [^MimeMessage mmsg body ^String charset]
;;  (if (string? body)
;;    (doto ^MimeMessage mmsg (.setText body charset))
;;    (doto ^MimeMessage mmsg (.setContent ))
;;    (.setContent jmsg (eval-multipart parts))
;;    )
;;  )
;;
;;; (defn add-multipart! [^javax.mail.Message jmsg parts]
;;;   (.setContent jmsg (eval-multipart parts)))
;;
;;
;;; (defn add-body! [^javax.mail.Message jmsg body charset]
;;;   (if (string? body)
;;;     (if (instance? MimeMessage jmsg)
;;;       (doto ^MimeMessage jmsg (.setText body charset))
;;;       (doto jmsg (.setText body)))
;;;     (doto jmsg (add-multipart! body))))
;;
;;
;;     ;
;;     ; (doto ^MimeMessage jmsg
;;     ;   (add-body! (:body msg) charset)
;;     ;   (.saveChanges)))))
