(ns tarayo.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))



(s/def ::host (and string? (complement str/blank?)))
(s/def ::port pos-int?)
(s/def ::debug boolean?)
(s/def ::ssl boolean?)
(s/def ::tls boolean?)
(s/def ::protocol #{"smtp" "smtps"})
(s/def ::user (and string? (complement str/blank?)))
(s/def ::password (and string? (complement str/blank?)))

(s/def ::smtp-server
  (s/keys :req-un [::host
                   ::port
                   ]
          :opt-un [
                   ::ssl
                   ::tls
                   ::user
                   ::password
                   ::debug
                   ]
          )
  )

(s/def ::charset (and string? (complement str/blank?)))
(s/def ::message-id-fn fn?)
(s/def ::from (and string? (complement str/blank?)))
(s/def ::to (and string? (complement str/blank?)))
(s/def ::cc (and string? (complement str/blank?)))
(s/def ::bcc (and string? (complement str/blank?)))

(s/def ::message
  (s/keys :req-un [
                   ::from
                   ]
          :opt-un [
                   ::charset
                   ::message-id-fn
                   ])
  )
