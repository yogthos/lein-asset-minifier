(defproject lein-asset-minifier "0.4.6"
  :description "plugin for CSS/Js asset minification"
  :url "https://github.com/yogthos/lein-asset-minifier"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [asset-minifier "0.2.7"]
                 [org.clojure/core.async "1.6.681"]]
  :eval-in-leiningen true)
