(ns tarayo.mail.mime.multipart-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :as t]
   [tarayo.mail.mime.multipart :as sut])
  (:import
   jakarta.mail.BodyPart
   (jakarta.mail.internet
    MimeBodyPart
    MimeMultipart)))

(t/deftest make-multipart-test
  (let [mp (sut/make-multipart "mixed"
                               [{:content-type "text/html" :content "foo"}
                                {:content (io/file "project.clj")}]
                               "UTF-8")]
    (t/is (instance? MimeMultipart mp))
    (t/is (= 2 (.getCount mp)))
    (t/is (str/starts-with? (.getContentType mp) "multipart/mixed; "))

    (let [bps (map #(.getBodyPart mp ^long %) (range (.getCount mp)))
          ^MimeBodyPart html (first bps)
          ^MimeBodyPart attach (second bps)]

      (t/is (every? #(instance? BodyPart %) bps))

      (t/is (= "foo" (.getContent html)))
      (t/is (= "text/html; charset=UTF-8" (.getContentType html)))

      (t/is (= "project.clj" (.getFileName attach)))
      (t/is (= "text/x-clojure" (.getContentType attach))))))

(t/deftest make-multipart-alternative-test
  (let [mp (sut/make-multipart "alternative"
                               [{:content-type "text/plain" :content "foo"}
                                {:content-type "text/html" :content "<p>foo</p>"}]
                               "UTF-8")]
    (t/is (instance? MimeMultipart mp))
    (t/is (= 2 (.getCount mp)))
    (t/is (str/starts-with? (.getContentType mp) "multipart/alternative; "))

    (let [bps (map #(.getBodyPart mp ^long %) (range (.getCount mp)))
          ^MimeBodyPart text (first bps)
          ^MimeBodyPart html (second bps)]
      (t/is (= "text/plain; charset=UTF-8" (.getContentType text)))
      (t/is (= "foo" (.getContent text)))

      (t/is (= "text/html; charset=UTF-8" (.getContentType html)))
      (t/is (= "<p>foo</p>" (.getContent html))))))
