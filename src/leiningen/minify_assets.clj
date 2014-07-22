(ns leiningen.minify-assets
  (:require [asset-minifier.core :refer [minify]]
            [clojure.string :as s]))

(defn extract-options
  "Given a project, returns a seq of cljsbuild option maps."
  [project & [profile]]
  (let [opts (:minify-assets project)
        profile (keyword profile)]
   (cond
    (and opts profile (nil? (profile opts))) (println "WARNING: profile" (name profile) "not found")
    (some #{:assets} (keys opts)) opts
    (and opts profile) (profile opts)

    opts (:dev opts)
    :else (println "WARNING: no :minify-assets entry found in project definition."))))

(defn filter-results [& results]
  (->> results
       (partition 2)
       (remove #(nil? (second %)))
       (map (partial apply str))
       (apply str)))

(defn minify-assets [project & [profile]]
  (println "minifying assets...")
  (let [{:keys [assets options]} (extract-options project profile)]
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
          (println (filter-results
                    "\nminifying: " target
                    "\nassets: " (s/join ", " sources)
                    "\noriginal size: " original-size
                    "\ncompressed size: " compressed-size
                    "\ngzipped size: " gzipped-size))
          (when (not-empty warnings)
            (println "warnings:\n" (s/join "\n" warnings)))
          (when (not-empty errors)
            (println "errors:\n" (s/join "\n" errors))))))))
