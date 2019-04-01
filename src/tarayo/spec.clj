(ns tarayo.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [tarayo.core :as core]))

(s/def ::non-blank-string?
   (and string? (complement str/blank?)))

(s/def ::connection
  #(instance? tarayo.core.TarayoConnection %))

(s/def :smtp-server/host ::non-blank-string?)
(s/def :smtp-server/port pos-int?)
(s/def :smtp-server/debug boolean?)
(s/def :smtp-server/ssl boolean?)
(s/def :smtp-server/tls boolean?)
(s/def :smtp-server/protocol #{"smtp" "smtps"})
(s/def :smtp-server/user ::non-blank-string?)
(s/def :smtp-server/password ::non-blank-string?)

(s/def ::smtp-server
  (s/keys :opt-un [:smtp-server/host
                   :smtp-server/port
                   :smtp-server/ssl
                   :smtp-server/tls
                   :smtp-server/user
                   :smtp-server/password
                   :smtp-server/debug]))

(s/def :message/charset ::non-blank-string?)
(s/def :message/message-id-fn fn?)
(s/def :message/user-agent ::non-blank-string?)
(s/def :message/from ::non-blank-string?)
(s/def :message/to ::non-blank-string?)
(s/def :message/cc ::non-blank-string?)
(s/def :message/bcc ::non-blank-string?)
(s/def :message/subject ::non-blank-string?)
(s/def :message/multipart #{"alternative" "mixed" "related"})

(s/def :body-part/id ::non-blank-string?)
(s/def :body-part/type ::non-blank-string?)
(s/def :body-part/content
  (s/or :string ::non-blank-string?
        :data-handler #(instance? javax.activation.DataHandler %)))
(s/def :body-part/content-type ::non-blank-string?)
(s/def :body-part/content-encoding ::non-blank-string?)

(s/def :message-body/text ::non-blank-string?)
(s/def :message-body/part (s/keys :req-un [:body-part/type
                                           :body-part/content]
                                  :opt-un [:body-part/id
                                           :body-part/content-type
                                           :body-part/content-encoding ]))
(s/def :message-body/parts (s/+ ::body-part))

(s/def :message/body
  (s/or :text :message-body/text
        :parts :message-body/parts))

(s/def ::message
  (s/keys :req-un [:message/from
                   :message/to
                   :message/subject
                   :message/body]
          :opt-un [:message/cc
                   :message/bcc
                   :message/charset
                   :message/user-agent
                   :message/multipart
                   :message/message-id-fn]))

(s/fdef core/connect
  :args (s/cat :smtp-server (s/? ::smtp-server))
  :ret ::connection)
