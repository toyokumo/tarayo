(ns tarayo.core
  (:require
   [tarayo.mail.mime :as mime]
   [tarayo.mail.session :as session]
   [tarayo.mail.transport :as transport])
  (:import
   javax.mail.Transport))

(defprotocol ISMTPConnection
  "TODO"
  (send!
    [this message]
    "Send a message.

  message => Map
    `:from`, `:to`, `:subject` and `:body` are required.
    `:content-type`, `:multipart` and `:message-id-fn` are optional.

  :message-id-fn => Function to generate custom Message-ID. No arguments are passed.

  body => Content string or list of multipart maps
  multipart map => map
    :id           A Content-ID within multiparts. (OPTIONAL)
                  `tarayo.mail.mime.id/get-random` is useful.
    :content-type TODO
    :content      TODO")

  (connected?
    [this]
    "Return true if this connection is open.")

  (close
    [this]
    "Close this connection."))

(defrecord SMTPConnection
  [session transport]

  ISMTPConnection
  (send!
    [this message]
    (->> message
         (mime/make-message (:session this))
         (transport/send! (:transport this))))

  (connected?
    [this]
    (.isConnected ^Transport (:transport this)))

  (close
    [this]
    (.close ^Transport (:transport this))))

(defn- get-defaults
  [{:keys [user] :as smtp-server}]
  {:host "localhost"
   :port (cond
           (contains? smtp-server :ssl.enable) 465
           (contains? smtp-server :starttls.enable) 587
           :else 25)
   :auth (some? user)})

(defn ^SMTPConnection
  connect
  "Connect to the specified SMTP server.
  If the connection is successful, an open `SMTPConnection` is returned.

  smtp-server: A map (kebab-case is allowed)
    :host
    :port
    :user
    :password
    :ssl.enable
    :starttls.enable

  https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary.html"
  ([] (connect {}))
  ([smtp-server]
   (let [smtp-server (merge (get-defaults smtp-server)
                            smtp-server)
         sess (session/make-session smtp-server)]
     (map->SMTPConnection
      {:session sess
       :transport (doto ^Transport (transport/make-transport sess)
                    (transport/connect! smtp-server))}))))
