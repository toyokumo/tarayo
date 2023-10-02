(ns tarayo.test-helper
  (:require
   [tarayo.mail.session :as session])
  (:import
   (com.dumbster.smtp
    SimpleSmtpServer
    SmtpMessage)
   (org.eclipse.angus.mail.smtp
    SMTPTransport)))

(defrecord TestConnection
  [session transport]
  tarayo.core.ISMTPConnection
  (send! [_this message] {:fn :send! :message message})
  (connected? [_this] true)
  (close [_this] nil))

(def ^:private default-test-smtp-server
  {:host "localhost" :port 1025})

(defn test-connection
  ([] (test-connection default-test-smtp-server))
  ([smtp-server]
   (->TestConnection
    (session/make-session smtp-server)
    :dummy-transport)))

(defn get-received-emails
  [^SimpleSmtpServer server]
  (->> (seq (.getReceivedEmails server))
       (map (fn [^SmtpMessage msg]
              {:from (.getHeaderValue msg "From")
               :to (.getHeaderValue msg "To")
               :subject (.getHeaderValue msg "Subject")
               :body (.getBody msg)}))))

(defn get-received-email-by-from
  [^SimpleSmtpServer server ^String from-addr]
  (->> (get-received-emails server)
       (some #(when (= from-addr (:from %)) %))))

(defmacro with-test-smtp-server
  [[server-sym port-sym] & body]
  `(with-open [~server-sym (SimpleSmtpServer/start SimpleSmtpServer/AUTO_SMTP_PORT)]
     (let [~port-sym (.getPort ~server-sym)]
       ~@body)))

(defn random-address
  []
  (format "%s@example.com" (java.util.UUID/randomUUID)))

(defn tarayo-message-id?
  [x]
  (if (sequential? x)
    (tarayo-message-id?  (first x))
    (some?  (re-seq #"^<[0-9A-Za-z]+\.[0-9]+@tarayo\..+>$" x))))

(defn tarayo-user-agent?
  [x]
  (if (sequential? x)
    (tarayo-user-agent?  (first x))
    (some?  (re-seq #"^tarayo/.+$" x))))

(defn ^SMTPTransport test-transport
  []
  (proxy [SMTPTransport] [(session/make-session) (jakarta.mail.URLName. "localhost")]
    (connect
      ([] true)
      ([_ _] true)
      ([_ _ _] true)
      ([_ _ _ _] true))
    (getLastReturnCode [] 250)
    (getLastServerResponse [] "250 OK\n")
    (sendMessage [msg addrs] {:message msg :addresses addrs})))
