(ns tarayo.mail.mime.multipart.body-test
  (:require [clojure.java.io :as io]
            [clojure.test :as t]
            [tarayo.mail.mime.multipart.body :as sut])
  (:import javax.mail.internet.MimeBodyPart))

(t/deftest make-bodypart-text-html-test
  (let [part {:type "text/html" :content "<h1>hello</h1>"}
        bp (sut/make-bodypart part "UTF-8")]
    (t/is (instance? MimeBodyPart bp))

    (t/is (= "<h1>hello</h1>" ^String (.getContent bp)))
    ;; TODO: content-type
    ))

(t/deftest make-bodypart-test
  (t/testing "inline"
    (let [part {:type :inline :content (io/file "project.clj")}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/x-clojure"] (seq (.getHeader bp "Content-Type"))))
      (t/is (= "project.clj" (.getFileName bp)))
      (t/is (= "inline" (.getDisposition bp)))))

  (t/testing "specify content-type"
    (let [part {:type :inline :content (io/file "project.clj")
                :content-type "text/plain"}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/plain"]
               (seq (.getHeader bp "Content-Type"))))))

  (t/testing "attachment"
    (let [part {:type :attachment :content (io/file "project.clj")}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/x-clojure"] (seq (.getHeader bp "Content-Type"))))
      (t/is (= "project.clj" (.getFileName bp)))
      (t/is (= "attachment" (.getDisposition bp))))))
