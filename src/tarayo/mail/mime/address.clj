(ns tarayo.mail.mime.address
  (:import
   (jakarta.mail.internet
    InternetAddress)))

(defn make-address
  ^InternetAddress
  [addr charset]
  (let [^InternetAddress addr (cond-> addr
                                (not (instance? InternetAddress addr))
                                (InternetAddress.))]
    (InternetAddress. (.getAddress addr)
                      (.getPersonal addr)
                      charset)))

(defn ^"[Ljakarta.mail.internet.InternetAddress;"
  make-addresses
  [addrs charset]
  (->> (cond-> addrs (string? addrs) vector)
       (map #(make-address % charset))
       (into-array InternetAddress)))
