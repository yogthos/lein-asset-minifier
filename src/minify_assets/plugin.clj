(ns minify-assets.plugin
  (:require [robert.hooke]
            [leiningen.compile]
            [leiningen.minify-assets :refer [minify-assets]]))

(defn add-minify-assets-hook [f & args]
  (apply f args)
  (minify-assets (first args)))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.compile/compile
                         #'add-minify-assets-hook))

