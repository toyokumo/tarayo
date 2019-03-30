(ns tarayo.mail.mime
  (:require [clojure.set :as set]
            [tarayo.mail.mime.address :as address]
            [tarayo.mail.mime.message :as message]
            [tarayo.mail.mime.multipart :as multipart])
  (:import javax.mail.internet.MimeMessage
           javax.mail.Session))

(def ^:private defaults
  {:charset "utf-8"
   :content-type "text/plain"
   :multipart "mixed"})

(def ^:private non-extra-headers
  #{:bcc :body :cc :content-type :date :from :message-id :multipart :reply-to :subject :to})

(defn- default-user-agent []
  (str "tarayo/" (System/getProperty "tarayo.version")))

(defn ^MimeMessage make-message [^Session session message]
  (let [{:keys [charset content-type cc bcc body multipart]} (merge defaults message)]
    (doto ^MimeMessage (message/make-message session message)
      (message/add-to (address/make-addresses (:to message) charset))
      (message/set-from (address/make-address (:from message) charset))
      (message/set-subject (:subject message) charset)
      (message/set-sent-date (:date message (java.util.Date.)))
      (message/add-headers (-> (apply dissoc message non-extra-headers)
                               (update :user-agent #(or % (default-user-agent)))
                               (set/rename-keys {:user-agent "User-Agent"})))
      (cond-> cc (message/add-cc (address/make-addresses cc charset))
              bcc (message/add-bcc (address/make-addresses bcc charset))
              (string? body) (message/set-content body (format "%s; charset=%s" content-type charset))
              (sequential? body) (message/set-content (multipart/make-multipart multipart body charset)))
      (.saveChanges))))
