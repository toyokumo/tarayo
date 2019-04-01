(ns tarayo.core-test
  (:require [clojure.test :as t]
            [tarayo.core :as sut]
            [tarayo.mail.transport :as transport]
            [tarayo.test-helper :as h])
  (:import com.sun.mail.smtp.SMTPTransport
           javax.mail.Session))

(t/deftest connect-and-send!-test
  (h/with-test-smtp-server [srv port]
    (let [test-message {:from "alice@example.com" :to "bob@example.com"
                        :subject "hello" :body "world"}]
      (t/is (empty? (h/get-received-emails srv)))

      (with-open [conn (sut/connect {:port port})]
        (t/is (sut/connected? conn))
        (t/is (= {:result :success} (sut/send! conn test-message))))

      (t/is (= [test-message] (h/get-received-emails srv))))))

(t/deftest connect-defaults-test
  (with-redefs [transport/connect! (fn [t _] t)]
    (t/testing "no parameters"
      (let [{:keys [^Session session ^SMTPTransport transport]} (sut/connect)
            props (.getProperties session)
            url-name (.getURLName transport)]
        (t/are [x y] (= x (get props y))
          25,          "mail.smtp.port"
          "localhost", "mail.smtp.host"
          "false",     "mail.smtp.auth"
          nil,         "mail.smtp.starttls.enable")
        (t/is (= "smtp" (.getProtocol url-name)))))

    (t/testing "ssl"
      (let [server {:host "example.com" :ssl true}
            {:keys [^Session session ^SMTPTransport transport]} (sut/connect server)
            props (.getProperties session)
            url-name (.getURLName transport)]
        (t/are [x y] (= x (get props y))
          465,           "mail.smtps.port"
          "example.com", "mail.smtps.host"
          "false",       "mail.smtps.auth"
          nil,           "mail.smtp.starttls.enable")
        (t/is (= "smtps" (.getProtocol url-name)))))

    (t/testing "tls"
      (let [server {:tls true}
            {:keys [^Session session ^SMTPTransport transport]} (sut/connect server)
            props (.getProperties session)
            url-name (.getURLName transport)]
        (t/are [x y] (= x (get props y))
          587,         "mail.smtp.port"
          "localhost", "mail.smtp.host"
          "false",     "mail.smtp.auth"
          "true",     "mail.smtp.starttls.enable")
        (t/is (= "smtp" (.getProtocol url-name)))))

    (t/testing "user authentication"
      (let [server {:user "foo" :password "bar"}
            {:keys [^Session session ^SMTPTransport transport]} (sut/connect server)
            props (.getProperties session)
            url-name (.getURLName transport)]
        (t/are [x y] (= x (get props y))
          25,          "mail.smtp.port"
          "localhost", "mail.smtp.host"
          "true",      "mail.smtp.auth"
          nil,         "mail.smtp.starttls.enable")
        (t/is (= "smtp" (.getProtocol url-name)))))))

(t/deftest header-injection-test
  (h/with-test-smtp-server [srv port]
    (let [from (h/random-address)
          test-message {:from from :to "alice@example.com"
                        :subject "hello\nFrom: bob@example.com" :body "world"}]
      (with-open [conn (sut/connect {:port port})]
        (t/is (= {:result :success} (sut/send! conn test-message))))

      (t/is (= {:from from
                :to "alice@example.com"
                :subject "hello"
                :body "world"}
               (h/get-received-email-by-from srv from))))))
