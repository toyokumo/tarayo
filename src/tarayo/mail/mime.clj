(ns tarayo.mail.mime
  (:require [clojure.set :as set]
            [tarayo.mail.mime.address :as address]
            [tarayo.mail.mime.message :as message]
            [tarayo.mail.mime.multipart :as multipart])
  (:import [javax.mail Message Session]))

(def ^:private default-charset "utf-8")

(def ^:private non-extra-headers
  #{:bcc :body :cc :date :from :message-id :reply-to :sender :subject :to})

(defn- default-user-agent []
  (str "tarayo/" (System/getProperty "tarayo.version")))

(defn make-message [^Session session message]
  (let [charset (:charset message default-charset)
        {:keys [from sender body]} message]
    (doto ^Message (message/make-message session message)
      (message/add-to (address/make-addresses (:to message) charset))
      (message/add-cc (address/make-addresses (:cc message) charset))
      (message/add-bcc (address/make-addresses (:bcc message) charset))
      (message/set-from (address/make-address (or from sender) charset))
      (message/set-subject (:subject message) charset)
      (message/set-sent-date (:date message (java.util.Date.)))
      (message/add-headers (-> (apply dissoc message non-extra-headers)
                               (update :user-agent #(or % (default-user-agent)))
                               (set/rename-keys {:user-agent "User-Agent"})))
      (cond-> (string? body) (message/set-text body charset)
              (sequential? body) (message/set-content (multipart/make-multipart body charset)))
      (.saveChanges))))
