(ns tarayo.mail.mime.address
  (:import javax.mail.internet.InternetAddress))

(defn ^InternetAddress
  make-address [addr charset]
  (let [^InternetAddress addr (cond-> addr (not (instance? InternetAddress addr))
                                      (InternetAddress.))]
    (InternetAddress. (.getAddress addr)
                      (.getPersonal addr)
                      charset)))

(defn ^"[Ljavax.mail.internet.InternetAddress;"
  make-addresses [addrs charset]
  (->> (cond-> addrs (string? addrs) vector)
       (map #(make-address % charset))
       (into-array InternetAddress)))
