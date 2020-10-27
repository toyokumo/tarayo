(ns tarayo.mail.session-test
  (:require
   [clojure.test :as t]
   [tarayo.mail.session :as sut])
  (:import
   jakarta.mail.Session))

(t/deftest make-session-test
  (let [sess (sut/make-session {:host "localhost" :port 25})]
    (t/is (instance? Session sess))
    (t/is (= {"mail.smtp.host" "localhost"
              "mail.smtp.port" 25
              "mail.smtps.host" "localhost"
              "mail.smtps.port" 25}
             (.getProperties sess)))
    (t/is (not (.getDebug sess)))))

(t/deftest make-session-with-debug-test
  (let [sess (sut/make-session {:host "localhost" :port 25 :debug true})]
    (t/is (instance? Session sess))
    (t/is (= {"mail.smtp.host" "localhost"
              "mail.smtp.port" 25
              "mail.smtps.host" "localhost"
              "mail.smtps.port" 25}
             (.getProperties sess)))
    (t/is (.getDebug sess))))

(t/deftest make-session-with-tls-test
  (let [sess (sut/make-session {:host "localhost" :port 25
                                :starttls.enable true})
        props (.getProperties sess)]
    (t/is (instance? Session sess))
    (t/is (every? #(= "true" (get props %))
                  ["mail.smtp.starttls.enable"
                   "mail.smtps.starttls.enable"]))))

(t/deftest make-session-with-string-key-test
  (let [opts {"mail.smtp.host" "localhost"
              "mail.smtp.port" 25}
        sess (sut/make-session opts)]
    (t/is (instance? Session sess))
    (t/is (= {"mail.smtp.host" "localhost"
              "mail.smtp.port" 25}
             (.getProperties sess)))))

(t/deftest make-session-with-kebab-case-key-test
  (let [sess (sut/make-session {:host "localhost" :port 25
                                :socket-factory.port 123})]
    (t/is (instance? Session sess))
    (t/is (= {"mail.smtp.host" "localhost"
              "mail.smtp.port" 25
              "mail.smtp.socketFactory.port" 123
              "mail.smtps.host" "localhost"
              "mail.smtps.port" 25
              "mail.smtps.socketFactory.port" 123}
             (.getProperties sess)))))
