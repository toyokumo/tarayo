(ns tarayo.mail.session
  (:require [clojure.set :as set])
  (:import java.util.Properties
           javax.mail.Session))

(def ^:private protocols #{"smtp" "smtps"})

(defn- transform [m]
  (mapcat
   (fn [[k v]]
     (let [v (cond-> v (boolean? v) str)]
       (if (keyword? k)
         (map #(vector (str "mail." % "." (name k)) v) protocols)
         [[k v]])))
   m))

(defn- extract-smtp-server-kvs [smtp-server]
   (-> smtp-server
       (dissoc :debug :password :protocol :ssl)
       (set/rename-keys {:tls :starttls.enable})
       transform))

(defn- session-properties [smtp-server]
  (let [props (Properties.)]
    (doseq [[k v] (extract-smtp-server-kvs smtp-server)]
      (.put props k v))
    props))

(defn ^Session make-session [smtp-server]
  (let [props (session-properties smtp-server)]
    (doto (Session/getInstance props)
      (.setDebug (get smtp-server :debug false)))))
