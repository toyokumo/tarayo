(ns tarayo.mail.transport-test
  (:require
   [clojure.test :as t]
   [tarayo.mail.mime :as mime]
   [tarayo.mail.session :as session]
   [tarayo.mail.transport :as sut]
   [tarayo.test-helper :as h])
  (:import
   javax.mail.Transport))

(t/deftest make-transport-test
  (let [smtp-server {:host "localhost" :port 9876 :protocol "smtp"}
        sess (session/make-session smtp-server)
        trans (sut/make-transport sess (:protocol smtp-server))]
    (t/is (instance? Transport trans))
    (t/is (not (.isConnected trans)))))

(t/deftest connect!-and-send!-test
  (h/with-test-smtp-server [srv port]
    (let [smtp-server {:host "localhost" :port port :protocol "smtp"}
          sess (session/make-session smtp-server)
          test-message {:from "alice@example.com" :to "bob@example.com"
                        :subject "hello" :body "world"}]
      (t/is (empty? (h/get-received-emails srv)))

      (with-open [trans (sut/make-transport sess (:protocol smtp-server))]
        (sut/connect! trans smtp-server)
        (t/is (.isConnected trans))
        (t/is (= {:result :success :code 250 :message "250 OK\n"}
                 (sut/send! trans (mime/make-message sess test-message)))))

      (let [mails (h/get-received-emails srv)]
        (t/is (= 1 (count mails)))
        (t/is (= test-message (first mails)))))))

(t/deftest connect!-error-test
  (h/with-test-smtp-server [srv port]
    (let [smtp-server {:host "localhost" :port port :protocol "smtp"}
          sess (session/make-session smtp-server)
          trans (sut/make-transport sess (:protocol smtp-server))
          test-message {:from "alice@example.com" :to "bob@example.com"
                        :subject "hello" :body "world"}]
      (t/is (empty? (h/get-received-emails srv)))
      (t/is (not (.isConnected trans)))

      (let [{:keys [result code message cause]} (sut/send! trans (mime/make-message sess test-message))]
        (t/is (= :failed result))
        (t/is (= 0 code))
        (t/is (and (string? message)
                   (seq message)))
        (t/is (instance? Exception cause)))

      (t/is (empty? (h/get-received-emails srv))))))
