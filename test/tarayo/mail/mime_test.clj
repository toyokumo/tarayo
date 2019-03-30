(ns tarayo.mail.mime-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [tarayo.mail.mime :as sut]
            [tarayo.test-helper :as h])
  (:import [javax.mail.internet MimeMessage MimeMultipart]))

(t/deftest make-message-test
  (let [{:keys [session]} (h/test-connection)
        opts {:from "foo" :to "bar" :subject "hello" :body "world" :charset "UTF-8"}
        msg (sut/make-message session opts)]
    (t/is (instance? MimeMessage msg))
    (t/is (str/includes? (.getMessageID msg) "@tarayo"))
    (t/is (str/starts-with? (first (.getHeader msg "User-Agent")) "tarayo/"))
    (t/is (= "world" ^String (.getContent msg)))))

(t/deftest make-message-with-custom-user-agent-test
  (let [{:keys [session]} (h/test-connection)
        opts {:from "foo" :to "bar" :subject "hello" :body "world" :charset "UTF-8"
              :user-agent "FooBar"}
        msg (sut/make-message session opts)]
    (t/is (instance? MimeMessage msg))
    (t/is (= ["FooBar"] (seq (.getHeader msg "User-Agent"))))))

(t/deftest make-message-with-sequential-bodies-test
  (let [{:keys [session]} (h/test-connection)
        opts {:from "foo" :to "bar" :subject "hello" :charset "UTF-8"
              :body [{:type "text/plain" :content "world"}
                     {:type "text/html" :content "<p>world</p>"}]}
        msg (sut/make-message session opts)]
    (t/is (instance? MimeMessage msg))
    (t/is (instance? MimeMultipart (.getContent msg)))))
