(ns tarayo.mail.mime-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [tarayo.mail.mime :as sut]
            [tarayo.mail.mime.message :as message]
            [tarayo.test-helper :as h])
  (:import [javax.mail.internet InternetAddress MimeMessage MimeMultipart]))

(t/deftest make-message-test
  (let [{:keys [session]} (h/test-connection)
        opts {:from "foo" :to "bar" :subject "hello" :body "world" :charset "UTF-8"}
        msg (sut/make-message session opts)]
    (t/is (instance? MimeMessage msg))
    (t/is (str/includes? (.getMessageID msg) "@tarayo"))
    (t/is (h/tarayo-user-agent? (first (.getHeader msg "User-Agent"))))
    (t/is (= "world" ^String (.getContent msg)))))

(t/deftest make-message-with-cc-bcc-test
  (let [{:keys [session]} (h/test-connection)
        opts {:from "foo" :to "bar" :subject "hello" :body "world" :charset "UTF-8"
              :cc "alice" :bcc "bob"}
        msg (sut/make-message session opts)]
    (t/is (instance? MimeMessage msg))

    (let [ccs (.getRecipients msg message/recipient-type-cc)
          bccs (.getRecipients msg message/recipient-type-bcc)]
      (t/is (= 1 (count ccs) (count bccs)))
      (t/is (= "alice" (.getAddress ^InternetAddress (first ccs))))
      (t/is (= "bob" (.getAddress ^InternetAddress (first bccs)))))))

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
              :body [{:content-type "text/plain" :content "world"}
                     {:content-type "text/html" :content "<p>world</p>"}]}
        msg (sut/make-message session opts)
        content (.getContent msg)]
    (t/is (instance? MimeMessage msg))
    (t/is (instance? MimeMultipart content))
    (t/is (str/starts-with? (.getContentType ^MimeMultipart content)
                            "multipart/mixed; "))))

(t/deftest make-message-with-multipart-type-test
  (let [{:keys [session]} (h/test-connection)
        base-opts {:from "foo" :to "bar" :subject "hello" :charset "UTF-8"
                  :body [{:content-type "text/plain" :content "world"}]}]
    (t/testing "alternative"
      (let [opts (assoc base-opts :multipart "alternative")
            ^MimeMultipart mp (.getContent (sut/make-message session opts))]
        (t/is (str/starts-with? (.getContentType mp) "multipart/alternative; "))))

    (t/testing "related"
      (let [opts (assoc base-opts :multipart "related")
            ^MimeMultipart mp (.getContent (sut/make-message session opts))]
        (t/is (str/starts-with? (.getContentType mp) "multipart/related; "))))))
