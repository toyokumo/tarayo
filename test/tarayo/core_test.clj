(ns tarayo.core-test
  (:require [clojure.test :as t]
            [tarayo.core :as sut]
            [tarayo.test-helper :as h]))

(t/deftest connect-test
  (h/with-test-smtp-server [srv port]
    (let [test-message {:from "alice@example.com" :to "bob@example.com"
                        :subject "hello" :body "world"}]
      (t/is (empty? (h/get-received-emails srv)))

      (with-open [conn (sut/connect {:port port})]
        (t/is (sut/connected? conn))
        (t/is (= {:result :success} (sut/send! conn test-message))))

      (t/is (= [test-message] (h/get-received-emails srv))))))
