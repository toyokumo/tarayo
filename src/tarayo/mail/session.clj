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
     (let [v (cond-> v (boolean? v) str)]
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

(defn ^Session make-session
  "Create `jakarta.mail.Session` instance and return it."
  ([] (make-session {}))
  ([smtp-server]
   (let [props (session-properties smtp-server)]
     (doto (Session/getInstance props)
       (.setDebug (get smtp-server :debug false))))))
