(ns tarayo.mail.mime.address-test
  (:require
   [clojure.test :as t]
   [tarayo.mail.mime.address :as sut])
  (:import
   (jakarta.mail.internet
    InternetAddress)))

(t/deftest make-address-test
  (t/testing "String"
    (let [x (sut/make-address "foo@bar.com" "utf-8")]
      (t/is (instance? InternetAddress x))
      (t/is (= "foo@bar.com" (.getAddress x)))))

  (t/testing "InternetAddress"
    (let [addr (InternetAddress. "bar@baz.com")
          x (sut/make-address addr "utf-8")]
      (t/is (instance? InternetAddress x))
      (t/is (= "bar@baz.com" (.getAddress x))))))

(t/deftest make-addresses-test
  (t/testing "string list"
    (let [x (sut/make-addresses ["foo@bar.com" "bar@baz.com"] "utf-8")]
      (t/is (.isArray (class x)))
      (t/is (every? #(instance? InternetAddress %) (seq x)))
      (t/is (= #{"foo@bar.com" "bar@baz.com"}
               (set (map #(.getAddress ^InternetAddress %) x))))))

  (t/testing "string"
    (let [x (sut/make-addresses "foo@bar.com" "utf-8")]
      (t/is (.isArray (class x)))
      (t/is (every? #(instance? InternetAddress %) (seq x)))
      (t/is (= #{"foo@bar.com"}
               (set (map #(.getAddress ^InternetAddress %) x)))))))
