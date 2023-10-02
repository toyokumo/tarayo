(ns tarayo.mail.session
  (:require
   [camel-snake-kebab.core :as csk]
   [tarayo.mail.constant :as constant])
  (:import
   jakarta.mail.Session
   java.util.Properties))

(defn- transform
  [m]
  (mapcat
   (fn [[k v]]
     ;; c.f. https://jakarta.ee/specifications/mail/2.0/apidocs/jakarta.mail/jakarta/mail/package-summary.html
     ;; > The properties are always set as strings; the Type column describes how the string is interpreted.
     ;; But `mail.event.executor` requires to be set an instance of `java.util.concurrent.Executor`,
     ;; so we should convert only boolean and integer to string.
     (let [v (cond-> v
               (or (boolean? v) (integer? v)) str)]
       (if (keyword? k)
         (map #(vector (str "mail." % "." (csk/->camelCaseString k)) v)
              constant/supported-protocols)
         [[k v]])))
   m))

(defn- extract-smtp-server-kvs
  [smtp-server]
  (-> smtp-server
      (dissoc :debug :password)
      transform))

(defn- session-properties
  [smtp-server]
  (let [props (Properties.)]
    (doseq [[k v] (extract-smtp-server-kvs smtp-server)]
      (.put props k v))
    props))

(defn make-session
  "Create `jakarta.mail.Session` instance and return it."
  (^Session [] (make-session {}))
  (^Session [smtp-server]
   (let [props (session-properties smtp-server)]
     (doto (Session/getInstance props)
       (.setDebug (get smtp-server :debug false))))))
