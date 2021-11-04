(ns helper
  (:import
   (com.dumbster.smtp
    SimpleSmtpServer)))

(def test-message
  {:from "alice@example.com"
   :to "bob@example.com"
   :subject "hello"
   :body "world"})

(defmacro with-test-smtp-server
  [[server-sym port-sym] & body]
  `(with-open [~server-sym (SimpleSmtpServer/start SimpleSmtpServer/AUTO_SMTP_PORT)]
     (let [~port-sym (.getPort ~server-sym)]
       ~@body)))
