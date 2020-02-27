(ns tarayo.mail.mime
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [tarayo.mail.constant :as constant]
            [tarayo.mail.mime.address :as address]
            [tarayo.mail.mime.message :as message]
            [tarayo.mail.mime.multipart :as multipart])
  (:import java.util.Properties
           javax.mail.internet.MimeMessage
           javax.mail.Session))

(def ^:private non-extra-headers
  #{:bcc :body :cc :content-type :date :from :message-id :multipart :reply-to :subject :to})

(def ^:private default-user-agent
  (->> (io/resource "VERSION") slurp str/trim
       (str "tarayo/")))

(defn ^MimeMessage make-message [^Session session message]
  (let [{:keys [charset content-type cc bcc body multipart]} message
        charset (or charset constant/default-charset)
        content-type (or content-type constant/default-content-type)
        multipart (or multipart constant/default-multipart)]
    (doto ^MimeMessage (message/make-message session message)
      (message/add-to (address/make-addresses (:to message) charset))
      (message/set-from (address/make-address (:from message) charset))
      (message/set-subject (:subject message) charset)
      (message/set-sent-date (:date message (java.util.Date.)))
      (message/add-headers (-> (apply dissoc message non-extra-headers)
                               (update :user-agent #(or % default-user-agent))
                               (set/rename-keys {:user-agent "User-Agent"})))
      (cond-> cc (message/add-cc (address/make-addresses cc charset)))
      (cond-> bcc (message/add-bcc (address/make-addresses bcc charset)))
      (cond-> (string? body) (message/set-content body (format "%s; charset=%s" content-type charset))
              (sequential? body) (message/set-content (multipart/make-multipart multipart body charset)))
      (.saveChanges))))
