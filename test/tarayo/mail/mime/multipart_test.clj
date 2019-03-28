(ns tarayo.mail.mime.multipart-test
  (:require [clojure.test :as t]
            [tarayo.mail.mime.multipart :as sut]
            [clojure.java.io :as io])
  (:import javax.mail.internet.MimeMultipart
           javax.mail.BodyPart
           ))

(t/deftest make-multipart-test
  (let [mp (sut/make-multipart [{:type "text/html"
                                 :content "foo"}

                                {:type :attachment
                                 :content (io/file "project.clj")}] "UTF-8")]
    (t/is (instance? MimeMultipart mp))

    (t/is (= 2 (.getCount mp)))

    (let [[x y :as bps] (map #(.getBodyPart mp %) (range (.getCount mp)))]
      (t/is (every? #(instance? BodyPart %) bps))

      ;; FIXME
      (t/is (= "foo" (.getContent x)))
      (t/is (= "project.clj" (.getFileName y))))))
