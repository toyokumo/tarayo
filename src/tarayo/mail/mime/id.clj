(ns tarayo.mail.mime.id
  (:require
   [nano-id.core :as nano-id]))

(def ^:private generate-id
  (nano-id/custom
   "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
   16))

(defn get-random
  ^String
  []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))]
    (format "%s.%s@%s" (generate-id) (.getTime (java.util.Date.)) (str "tarayo." hostname))))
