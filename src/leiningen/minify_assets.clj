(ns leiningen.minify-assets
  (:require [asset-minifier.core :refer [minify]]
            [clojure.string :as s]))

(defn extract-options
  "Given a project, returns a seq of cljsbuild option maps."
  [project]
  (or (:minify-assets project)
      (println "WARNING: no :minify-assets entry found in project definition.")))

(defn minify-assets [project]
  (let [{:keys [assets options]} (extract-options project)]
    (println "minifying assets...")
    (doseq [[[path target]
             {:keys [sources
                     original-size
                     compressed-size
                     gzipped-size
                     warnings
                     errors]}]
            (minify assets options)]
      (if (empty? path)
        (println "\nno sources found at path:" path)
        (do
          (println "\nsummary for:" path
                   "\nassets:" (s/join ", " sources)
                   "\noutput file:" target
                   "\noriginal size:" original-size
                   "\ncompressed size:" compressed-size
                   "\ngzipped size:" gzipped-size)
          (when (not-empty warnings)
            (println "warnings:\n" (s/join "\n" warnings)))
          (when (not-empty errors)
            (println "errors:\n" (s/join "\n" errors))))))))
