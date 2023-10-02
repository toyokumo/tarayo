(ns tarayo.mail.transport-test
  (:require
   [clojure.test :as t]
   [tarayo.mail.mime :as mime]
   [tarayo.mail.session :as session]
   [tarayo.mail.transport :as sut]
   [tarayo.test-helper :as h])
  (:import
   (org.eclipse.angus.mail.smtp
    SMTPSSLTransport
    SMTPTransport)))

(t/deftest make-transport-test
  (t/testing "smtp protocol"
    (let [smtp-server {:host "localhost" :port 9876}
          sess (session/make-session smtp-server)
          trans (sut/make-transport sess)]
      (t/is (instance? SMTPTransport trans))
      (t/is (not (instance? SMTPSSLTransport trans)))
      (t/is (not (.isConnected trans)))))

  (t/testing "smpts protocol"
    (let [smtp-server {:host "localhost" :port 9876 :ssl.enable true}
          sess (session/make-session smtp-server)
          trans (sut/make-transport sess)]
      (t/is (instance? SMTPTransport trans))
      (t/is (instance? SMTPSSLTransport trans))
      (t/is (not (.isConnected trans))))))

(t/deftest connect!-and-send!-test
  (h/with-test-smtp-server [srv port]
    (let [smtp-server {:host "localhost" :port port}
          sess (session/make-session smtp-server)
          test-message {:from "alice@example.com" :to "bob@example.com"
                        :subject "hello" :body "world"}]
      (t/is (empty? (h/get-received-emails srv)))

      (with-open [trans (sut/make-transport sess)]
        (sut/connect! trans smtp-server)
        (t/is (.isConnected trans))
        (t/is (= {:result :success :code 250 :message "250 OK\n"}
                 (sut/send! trans (mime/make-message sess test-message)))))

      (let [mails (h/get-received-emails srv)]
        (t/is (= 1 (count mails)))
        (t/is (= test-message (first mails)))))))

(t/deftest connect!-error-test
  (h/with-test-smtp-server [srv port]
    (let [smtp-server {:host "localhost" :port port}
          sess (session/make-session smtp-server)
          trans (sut/make-transport sess)
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
