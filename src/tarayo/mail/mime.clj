(ns tarayo.mail.mime
  (:require [clojure.set :as set]
            [tarayo.mail.internet.address :as address]
            [tarayo.mail.mime.message :as message])
  (:import javax.mail.internet.MimeMessage
           javax.mail.Session))

(def ^:private default-charset "utf-8")

(def ^:private non-extra-headers
  #{:bcc :body :cc :date :from :message-id :reply-to :sender :subject :to})

(defn- default-user-agent []
  (str "tarayo/" (System/getProperty "tarayo.version")))

(defn make-message [^Session session message]
  (let [charset (:charset message default-charset)
        {:keys [from sender]} message]
    (doto ^MimeMessage (message/make-message session message)
      (message/add-to (address/make-addresses (:to message) charset))
      (message/add-cc (address/make-addresses (:cc message) charset))
      (message/add-bcc (address/make-addresses (:bcc message) charset))
      (message/set-from (address/make-address (or from sender) charset))
      (message/set-subject (:subject message) charset)
      (message/set-sent-date (:date message (java.util.Date.)))
      (message/add-headers (-> (apply dissoc message non-extra-headers)
                               (update :user-agent #(or % (default-user-agent)))
                               (set/rename-keys {:user-agent "User-Agent"})))
      (message/set-body (:body message) charset)
      (.saveChanges))))
