(ns tarayo.mail.internet.address
  (:import javax.mail.internet.InternetAddress))

(defn make-address [addr charset]
  (let [addr (cond-> addr (not (instance? InternetAddress addr))
                     (InternetAddress.))]
    (InternetAddress. (.getAddress addr)
                      (.getPersonal addr)
                      charset)))

(defn make-addresses [addrs charset]
  (->> (cond-> addrs (string? addrs) vector)
       (map #(make-address % charset))
       (into-array InternetAddress)))
