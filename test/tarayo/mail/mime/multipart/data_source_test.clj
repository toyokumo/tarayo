(ns tarayo.mail.mime.multipart.data-source-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :as t]
   [tarayo.mail.mime.multipart.data-source :as sut])
  (:import
   jakarta.activation.DataSource
   java.io.ByteArrayOutputStream))

(t/deftest byte-array-data-source-test
  (t/testing "default content-type"
    (let [bs (.getBytes "hello")
          ds (sut/byte-array-data-source bs)]
      (t/is (instance? DataSource ds))

      (with-open [in (.getInputStream ds)
                  out (ByteArrayOutputStream.)]
        (io/copy in out)
        (t/is (= (seq bs) (seq (.toByteArray out)))))

      (t/is (= "application/octet-stream" (.getContentType ds)))))

  (t/testing "custom content-type"
    (let [bs (.getBytes "hello")
          ds (sut/byte-array-data-source bs "text/plain")]
      (t/is (instance? DataSource ds))
      (t/is (= "text/plain" (.getContentType ds))))))
