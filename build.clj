(ns build
  (:require
   [clojure.string :as str]
   [build-edn.main]))

(def ^:private config
  {:lib 'toyokumo/tarayo
   :version (str/trim (slurp "resources/VERSION"))
   :description "SMTP client library for Clojure. That’s it."
   :url "https://github.com/toyokumo/tarayo"
   :licenses [{:name "Apache, Version 2.0"
               :url "http://www.apache.org/licenses/LICENSE-2.0"}]})

(defmacro defwrapper [fn]
  `(defn ~fn [m#]
     (~(symbol (str "build-edn.main/" fn)) (merge config m#))))

(defwrapper pom)
(defwrapper jar)
(defwrapper java-compile)
(defwrapper uberjar)
(defwrapper install)
(defwrapper deploy)
(defwrapper update-documents)
(defwrapper lint)
(defwrapper bump-patch-version)
(defwrapper bump-minor-version)
(defwrapper bump-major-version)
(defwrapper add-snapshot)
(defwrapper remove-snapshot)
(defwrapper execute)
(defwrapper help)
