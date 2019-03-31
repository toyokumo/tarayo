(ns tarayo.integration.send-test
  (:require [camel-snake-kebab.core :as csk]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as t]
            [fudje.sweet :as fj]
            [org.httpkit.client :as http]
            [tarayo.core :as core]
            [tarayo.test-helper :as h])
  (:import java.util.Calendar))

(def ^:private mailhog-server
  {:host "localhost" :port 1025})

(def ^:private mailhog-search-api-endpoint
  (format "http://%s:8025/api/v2/search" (:host mailhog-server)))

(def ^:private ^Calendar now
  (doto (Calendar/getInstance)
    (.set 2112 (dec 9) 3)))

(defn- find-mail-by-from [from-address]
  (let [resp @(http/get mailhog-search-api-endpoint
                        {:query-params {:kind "from" :query from-address}})]
    (json/read-str (:body resp) :key-fn (comp keyword csk/->kebab-case))))

(t/deftest send-text-mail-test
  (let [from (h/random-address)]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from
                        :to "alice@example.com"
                        :subject "hello"
                        :body "world"
                        :charset "UTF-8"
                        :date (.getTime now)}))
    (let [resp (find-mail-by-from from)
          item (get-in resp [:items 0])]
      (t/is (= 1 (:total resp)))
      (t/is (= 1 (count (:to item))))
      (t/is
       (compatible
        (first (:to item))
        (fj/contains {:mailbox "alice" :domain "example.com"})))

      (t/is
       (compatible
        (get-in item [:content :headers])
        (fj/contains {:charset ["UTF-8"]
                      :content-type ["text/plain; charset=UTF-8"]
                      :date [(fj/checker #(str/starts-with? % "Sat, 3 Sep 2112 "))]
                      :from [from]
                      :message-id (fj/checker h/tarayo-message-id?)
                      :subject ["hello"]
                      :to ["alice@example.com"]
                      :user-agent (fj/checker h/tarayo-user-agent?)})))

      (t/is (= "world" (get-in item [:content :body]))))))

(t/deftest send-html-mail-test
  (let [from (h/random-address)]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from :to "alice@example.com" :subject "hello"
                        :body "<h1>world</h1>" :content-type "text/html"}))
    (let [resp (find-mail-by-from from)
          item (get-in resp [:items 0])]
      (t/is (= 1 (:total resp)))
      (t/is (= ["text/html; charset=utf-8"]
               (get-in item [:content :headers :content-type])))
      (t/is (= "<h1>world</h1>" (get-in item [:content :body]))))))

(t/deftest send-several-mails-in-one-session-test
  (let [from (h/random-address)]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from :to "alice@example.com" :subject "hello" :body "world"})
      (core/send! conn {:from from :to "bob@example.com" :subject "foo" :body "bar"}))
    (let [resp (find-mail-by-from from)]
      (t/is (= 2 (:total resp)))
      (t/is
       (compatible
        (map #(-> % :content :headers (select-keys [:to :subject])) (:items resp))
        (fj/just [{:to ["alice@example.com"] :subject ["hello"]}
                  {:to ["bob@example.com"] :subject ["foo"]}]
                 :in-any-order))))))

(t/deftest send-multipart-mixed-mail-test
  (let [from (h/random-address)]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from :to "alice@example.com" :subject "hello"
                        :body [{:type "text/plain" :content "world"}
                               {:type "attachment" :content (io/file "project.clj")}]}))
    (let [resp (find-mail-by-from from)
          item (get-in resp [:items 0])
          ;; NOTE: mailhog contains blank part
          mime-parts (drop-last (get-in item [:mime :parts]))]
      (t/is (= 1 (:total resp)))
      (t/is (= 2 (count mime-parts)))

      (t/is (str/starts-with? (get-in item [:content :headers :content-type 0])
                              "multipart/mixed; "))

      (t/is
       (compatible
        (first mime-parts)
        (fj/contains {:headers (fj/contains {:content-type ["text/plain; charset=utf-8"]})
                      :body "world"})))

      (t/is
       (compatible
        (second mime-parts)
        (fj/contains {:headers (fj/contains {:content-disposition ["attachment; filename=project.clj"]
                                             :content-type ["text/x-clojure"]})
                      :body (fj/checker any?)}))))))

(t/deftest send-multipart-alternative-mail-test
  (let [from (h/random-address)]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from :to "alice@example.com" :subject "hello"
                        :multipart "alternative"
                        :body [{:type "text/plain" :content "world"}
                               {:type "text/html" :content "<p>world</p>"}]}))
    (let [resp (find-mail-by-from from)
          item (get-in resp [:items 0])
          ;; NOTE: mailhog contains blank part
          mime-parts (drop-last (get-in item [:mime :parts]))]
      (t/is (= 1 (:total resp)))
      (t/is (= 2 (count mime-parts)))

      (t/is (str/starts-with? (get-in item [:content :headers :content-type 0])
                              "multipart/alternative; "))

      (t/is
       (compatible
        (first mime-parts)
        (fj/contains {:headers (fj/contains {:content-type ["text/plain; charset=utf-8"]})
                      :body "world"})))
      (t/is
       (compatible
        (second mime-parts)
        (fj/contains {:headers (fj/contains {:content-type ["text/html; charset=utf-8"]})
                      :body "<p>world</p>"}))))))
