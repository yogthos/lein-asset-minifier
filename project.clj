(defproject lein-asset-minifier "0.1.8"
  :description "plugin for CSS/Js asset minification"
  :url "https://github.com/yogthos/lein-asset-minifier"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [asset-minifier "0.1.4"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]]
  :eval-in-leiningen true)
