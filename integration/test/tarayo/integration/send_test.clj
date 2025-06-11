(ns tarayo.integration.send-test
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :as t]
   [org.httpkit.client :as http]
   [tarayo.core :as core]
   [tarayo.test-helper :as h])
  (:import
   java.util.Calendar))

(def ^:private mailhog-server
  {:host "localhost" :port 1025})

(def ^:private mailhog-search-api-endpoint
  (format "http://%s:8025/api/v2/search" (:host mailhog-server)))

(def ^:private ^Calendar now
  (doto (Calendar/getInstance)
    (.set 2112 (dec 9) 3)))

(defn- find-mail-by-from
  [from-address]
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
          item (get-in resp [:items 0])
          headers (some-> item (get-in [:content :headers]))]
      (t/is (= 1 (:total resp)))
      (t/is (= 1 (count (:to item))))
      (t/is (= {:mailbox "alice" :domain "example.com"}
               (-> item :to first (select-keys [:mailbox :domain]))))

      (t/is (= ["UTF-8"] (:charset headers)))
      (t/is (= ["text/plain; charset=UTF-8"] (:content-type headers)))
      (t/is (str/starts-with? (first (:date headers)) "Sat, 3 Sep 2112 "))
      (t/is (= [from] (:from headers)))
      (t/is (h/tarayo-message-id? (:message-id headers)))
      (t/is (= ["hello"] (:subject headers)))
      (t/is (= ["alice@example.com"] (:to headers)))
      (t/is (h/tarayo-user-agent? (:user-agent headers)))

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
      (t/is (= 2 (:total resp) (count (:items resp))))
      (t/is (= [{:to ["bob@example.com"] :subject ["foo"]}
                {:to ["alice@example.com"] :subject ["hello"]}]
               (->> (:items resp)
                    (map #(-> % :content :headers (select-keys [:to :subject])))
                    (sort-by :subject)))))))

(t/deftest send-multipart-mixed-mail-test
  (let [from (h/random-address)]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from :to "alice@example.com" :subject "hello"
                        :body [{:content-type "text/plain" :content "world"}
                               {:content (io/file "build.clj")}]}))
    (let [resp (find-mail-by-from from)
          item (get-in resp [:items 0])
          mime-parts (->> (get-in item [:mime :parts])
                          ;; NOTE: mailhog contains blank part
                          drop-last)]
      (t/is (= 1 (:total resp)))
      (t/is (= 2 (count mime-parts)))

      (t/is (str/starts-with? (get-in item [:content :headers :content-type 0])
                              "multipart/mixed; "))

      (let [{:keys [headers body]} (first mime-parts)]
        (t/is (= ["text/plain; charset=utf-8"]
                 (:content-type headers)))
        (t/is (= "world" body)))

      (let [{:keys [headers body]} (second mime-parts)]
        (t/is (= ["text/x-clojure"] (:content-type headers)))
        (t/is (= ["attachment; filename=build.clj"] (:content-disposition headers)))
        (t/is (and (string? body) (not (str/blank? body))))))))

(t/deftest send-multipart-alternative-mail-test
  (let [from (h/random-address)]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from :to "alice@example.com" :subject "hello"
                        :multipart "alternative"
                        :body [{:content-type "text/plain" :content "world"}
                               {:content-type "text/html" :content "<p>world</p>"}]}))
    (let [resp (find-mail-by-from from)
          item (get-in resp [:items 0])
          ;; NOTE: mailhog contains blank part
          mime-parts (drop-last (get-in item [:mime :parts]))]
      (t/is (= 1 (:total resp)))
      (t/is (= 2 (count mime-parts)))

      (t/is (str/starts-with? (get-in item [:content :headers :content-type 0])
                              "multipart/alternative; "))

      (let [{:keys [headers body]} (first mime-parts)]
        (t/is (= ["text/plain; charset=utf-8"] (:content-type headers)))
        (t/is (= "world" body)))

      (let [{:keys [headers body]} (second mime-parts)]
        (t/is (= ["text/html; charset=utf-8"] (:content-type headers)))
        (t/is (= "<p>world</p>" body))))))
