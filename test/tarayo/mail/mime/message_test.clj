(ns tarayo.mail.mime.message-test
  (:require
   [clojure.test :as t]
   [tarayo.mail.mime.address :as addr]
   [tarayo.mail.mime.message :as sut]
   [tarayo.mail.mime.multipart :as multipart]
   [tarayo.test-helper :as h])
  (:import
   (jakarta.mail.internet
    InternetAddress
    MimeMessage
    MimeMultipart)
   java.util.Calendar))

(t/deftest make-message-test
  (let [{:keys [session]} (h/test-connection)]
    (t/testing "default message-id"
      (let [msg (sut/make-message session {})]
        (t/is (instance? MimeMessage msg))
        (t/is (nil? (.getMessageID msg)))
        (.saveChanges msg)
        (t/is (h/tarayo-message-id? (.getMessageID msg)))))

    (t/testing "custom message-id"
      (let [msg (sut/make-message session {:message-id-fn (constantly "foo")})]
        (t/is (instance? MimeMessage msg))
        (t/is (nil? (.getMessageID msg)))
        (.saveChanges msg)
        (t/is (= "foo" (.getMessageID msg)))))))

(defn- ^MimeMessage gen-test-message
  []
  (let [{:keys [session]} (h/test-connection)
        ^Calendar cal (doto (Calendar/getInstance)
                        (.set 2112 (dec 9) 3))]
    (doto (sut/make-message session {})
      (sut/add-to (addr/make-addresses ["to@foo.com" "to@bar.com"] "utf-8"))
      (sut/add-cc (addr/make-addresses ["cc@foo.com" "cc@bar.com"] "utf-8"))
      (sut/add-bcc (addr/make-addresses ["bcc@foo.com" "bcc@bar.com"] "utf-8"))
      (sut/set-from (addr/make-address "from@foo.com" "utf-8"))
      (sut/set-reply-to (addr/make-addresses ["reply-to@foo.com" "reply-to@bar.com"] "utf-8"))
      (sut/set-subject "hello, world" "utf-8")
      (sut/set-sent-date (.getTime cal))
      (sut/add-headers {"Foo" "Bar" "Bar" "Baz"}))))

(defn- addrs->set
  [addrs]
  (set (map #(.getAddress ^InternetAddress %) addrs)))

(t/deftest add-to-cc-bcc-test
  (let [msg (gen-test-message)
        types [sut/recipient-type-to sut/recipient-type-cc sut/recipient-type-bcc]
        [tos ccs bccs] (map #(addrs->set (.getRecipients msg %)) types)]
    (t/is (= #{"to@foo.com" "to@bar.com"} tos))
    (t/is (= #{"cc@foo.com" "cc@bar.com"} ccs))
    (t/is (= #{"bcc@foo.com" "bcc@bar.com"} bccs))))

(t/deftest set-from-test
  (let [msg (gen-test-message)]
    (t/is (= #{"from@foo.com"} (addrs->set (.getFrom msg))))))

(t/deftest set-reply-to-test
  (let [msg (gen-test-message)
        reply-tos (addrs->set (.getReplyTo msg))]
    (t/is (= #{"reply-to@foo.com" "reply-to@bar.com"} reply-tos))))

(t/deftest set-subject-test
  (let [msg (gen-test-message)]
    (t/is (= "hello, world" (.getSubject msg)))))

(t/deftest set-sent-date-test
  (let [msg (gen-test-message)
        sent-date (.getSentDate msg)
        sent-cal (doto (Calendar/getInstance)
                   (.setTime sent-date))]
    (t/is (inst? sent-date))
    (t/is (= [2112 (dec 9) 3]
             (map #(.get sent-cal %) [Calendar/YEAR Calendar/MONTH Calendar/DAY_OF_MONTH])))))

(t/deftest add-headers-test
  (let [msg (gen-test-message)]
    (t/is (= ["Bar"] (seq (.getHeader msg "Foo"))))
    (t/is (= ["Baz"] (seq (.getHeader msg "Bar"))))))

(t/deftest set-content-test
  (t/testing "multipart"
    (let [msg (gen-test-message)
          parts [{:content-type "text/html" :content "<p>hello</p>"}]
          mp (multipart/make-multipart "mixed" parts "utf-8")]
      (sut/set-content msg mp)
      (t/is (instance? MimeMultipart (.getContent msg)))))

  (t/testing "string"
    (let [msg (gen-test-message)]
      (sut/set-content msg "hello" "text/html; charset=utf-8")
      (t/is (= "hello" (.getContent msg)))
      (t/is (= "text/html; charset=utf-8" (.getContentType msg))))))
