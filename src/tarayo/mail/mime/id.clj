(ns tarayo.mail.mime.id
  (:require
   [nano-id.custom :as nano-id]))

(def ^:private generate-id
  (nano-id/generate "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"))

(defn ^String get-random
  []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))]
    (format "<%s.%s@%s>" (generate-id 16) (.getTime (java.util.Date.)) (str "tarayo." hostname))))
