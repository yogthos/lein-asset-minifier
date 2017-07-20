(ns leiningen.minify-assets
  (:require [asset-minifier.core :as minifier]
            [minify-assets.file-watcher :refer [watch-thread]]
            [clojure.java.io :refer [file]]
            [clojure.string :as s]
            [clojure.core.async :as async :refer [go <! >!]])
  (:import java.security.InvalidParameterException))

(defn- filter-results [& results]
  (->> results
       (partition 2)
       (remove #(nil? (second %)))
       (map (partial apply str))
       (apply str)))

(defn- minify [assets]
  (println "\nminifying assets...")
  (let [minify-result (minifier/minify assets)]
    (doseq [{:keys [sources
                    targets
                    target
                    original-size
                    compressed-size
                    gzipped-size
                    warnings
                    errors]} minify-result]
      (do
        (println (filter-results
                  "\nminifying: " (if targets
                                    (s/join ", " targets)
                                    target)
                  "\nassets: " (s/join ", " sources)
                  "\noriginal size: " original-size
                  "\ncompressed size: " compressed-size
                  "\ngzipped size: " gzipped-size))
        (when (not-empty warnings)
          (println "warnings:\n" (s/join "\n" warnings)))
        (when (not-empty errors)
          (println "errors:\n" (s/join "\n" errors)))))))

(def compiled? (atom false))

(defn- java-not-supports-watch? []
  (let [[major minor] (map #(Integer/parseInt %)
                           (.split (System/getProperty "java.version") "\\."))]
    (and (< major 2)
         (< minor 7))))

(defn- asset-paths [asset]
  (let [asset-file (file asset)]
    (->>
     (if (.isDirectory asset-file)
       asset-file
       (.getParentFile asset-file))
     (file-seq)
     (filter #(.isDirectory %))
     (map #(.getPath %)))))

(defn- watch-paths [sources]
  (cond
    (string? sources) (asset-paths sources)
    (coll? sources) (set (mapcat asset-paths sources))))

(defn- create-watchers [config]
  (->> config
       (map (fn [[_ {:keys [source]} :as config-item]]
              {:config-item config-item
               :paths (watch-paths source)}))
       (map (fn [{:keys [config-item paths]}]
              (for [path paths]
                (watch-thread path (fn* [] (minify [config-item]))))))
       (flatten)))

(defn- normalize-path [root path]
  (if (coll? path)
    (vec (map #(normalize-path root %) path))
    (let [f (file path)]
      (.getAbsolutePath (if (or (.isAbsolute f) (.startsWith (.getPath f) "\\"))
                          f (file root path))))))

(defn- normalize-assets [root assets]
  (for [[asset-type {:keys [source target]}] assets]
    [asset-type {:source (normalize-path root source)
                 :target (normalize-path root target)}]))

(defn- run-assets-watch [config]
  (if (java-not-supports-watch?)
    (throw (InvalidParameterException. "watching for changes is only supported on JDK 1.7+"))
    (let [watchers (create-watchers config)]
      (doseq [watcher watchers]
        (.start watcher))
      (.join (first watchers)))))

(defn- run-assets-minify [config]
  (when-not (deref compiled?) (minify config) (reset! compiled? true)))

(defn minify-assets [project & opts]
  (let [watch? (some #{"watch"} opts)
        root (:root project)
        config (normalize-assets root (:minify-assets project))]
    (if watch?
      (run-assets-watch config)
      (run-assets-minify config))))
