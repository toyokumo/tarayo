(ns tarayo.mail.mime.message-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [tarayo.mail.mime.message :as sut]
            [tarayo.mail.mime.address :as addr]
            [tarayo.test-helper :as h])
  (:import javax.mail.internet.MimeMessage))

(t/deftest make-message-test
  (let [{:keys [session]} (h/test-connection)]
    (t/testing "default message-id"
      (let [msg (sut/make-message session {})]
        (t/is (instance? MimeMessage msg))
        (t/is (nil? (.getMessageID msg)))
        (.saveChanges msg)
        (t/is (str/includes? (.getMessageID msg) "@tarayo"))))

    (t/testing "custom message-id"
      (let [msg (sut/make-message session {:message-id-fn (constantly "foo")})]
        (t/is (instance? MimeMessage msg))
        (t/is (nil? (.getMessageID msg)))
        (.saveChanges msg)
        (t/is (= "foo" (.getMessageID msg)))))))

(t/deftest add-to-test
  (let [{:keys [session]} (h/test-connection)
        msg (sut/make-message session {})
        addrs (addr/make-addresses ["foo@bar.com" "bar@baz.com"] "utf-8")
        addrs->set (fn [addrs] (set (map #(.getAddress %) addrs)))]
    (sut/add-to msg addrs)
    (t/is (= (addrs->set addrs)
             (addrs->set (.getRecipients msg sut/recipient-type-to))))
    )
  )
