(ns tarayo.core
  (:require
   [tarayo.mail.mime :as mime]
   [tarayo.mail.session :as session]
   [tarayo.mail.transport :as transport])
  (:import
   jakarta.mail.Transport))

(defprotocol ISMTPConnection
  (send!
    [this message]
    "Send a message.

  `message` is a map containing following keys.
    * `:from`, `:to`, `:subject` and `:body` are REQUIRED.
    * `reply-to`, `:content-type`, `:multipart` and `:message-id-fn` are OPTIONAL.

  ## Content-type
  `:content-type`  is used when `:body` is a String. (Default: \"text/plain\")

  ## Multipart
  `:multipart` is a String to specify multipart type. (Default: \"mixed\")
  \"mixed\" and \"alternative\" are allowed.

  ## Body
  `:body` should be one of String or map list.
  String body will be handled as \"text message\".
  When you'd like to use multipart, you should specify body as map list.

  Map formatted body should contain following keys.
    * `:content` is REQUIRED.
    * `:content-type` and `:id` is OPTIONAL.

  String `:content` will be handled as \"text message\" while others are handled as \"attachment file\".
  If you don't specify `:content-type`, tarayo will detect it using Apache Tika automatically.

  Containing `:id` will be handled as \"inline attachment file\".

  ## Message-id-fn
  `:message-id-fn` is a function to generate custom Message-ID.
  No arguments are passed.")

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

(defn ^tarayo.core.ISMTPConnection
  connect
  "Connect to the specified SMTP server.
  If the connection is successful, an open `SMTPConnection` is returned.

  `smtp-server` is a map containing following keys. (kebab-case is allowed)
    * :host
    * :port
    * :user
    * :password
    * :ssl.enable
    * :starttls.enable

  For more information, please see https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary.html"
  ([] (connect {}))
  ([smtp-server]
   (let [smtp-server (merge (get-defaults smtp-server)
                            smtp-server)
         sess (session/make-session smtp-server)]
     (map->SMTPConnection
      {:session sess
       :transport (doto ^Transport (transport/make-transport sess)
                    (transport/connect! smtp-server))}))))
