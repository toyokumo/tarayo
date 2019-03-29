(ns tarayo.mail.mime-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [tarayo.mail.mime :as sut]
            [tarayo.test-helper :as h])
  (:import javax.mail.internet.MimeMessage))

(t/deftest make-message-test
  (let [{:keys [session]} (h/test-connection)
        msg (sut/make-message session {:from "foo" :to "bar" :subject "hello" :body "world" :charset "UTF-8"})]
    (t/is (instance? MimeMessage msg))
    (t/is (str/includes? (.getMessageID msg) "@tarayo"))))
