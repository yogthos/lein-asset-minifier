(ns leiningen.minify-assets
  (:require [asset-minifier.core :as minifier]
            [minify-assets.file-watcher :refer [start-watch!]]
            [clojure.java.io :refer [file]]
            [clojure.string :as s]
            [clojure.core.async :as async :refer [go <! >!]])
  (:import
       [java.nio.file
        FileSystems
        Path
        Paths
        StandardWatchEventKinds]))

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

(defn asset-path [asset]
  (let [asset-file (file asset)]
    (if (.isDirectory asset-file)
      asset
      (.getParent asset-file))))

(defn watch-paths [assets]
  (set
   (mapcat
     (fn[asset]
       (cond
        (string? asset)
        [(asset-path asset)]
        (coll? asset)
        (map asset-path asset)))
     (vals assets))))

(defn minify [assets options]
  (println "minifying assets...")
  (doseq [[[path target]
             {:keys [sources
                     original-size
                     compressed-size
                     gzipped-size
                     warnings
                     errors]}]
            (minifier/minify assets options)]
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
            (println "errors:\n" (s/join "\n" errors)))))))

(defn event-handler [assets options]
  (fn [e]
    (println (-> e (.context) (.toString)) "was modified!")
    (minify assets options)))

(def compiled? (atom false))

(defn unsupported-version? []
  (let [[major minor] (map #(Integer/parseInt %)
                        (.split (System/getProperty "java.version") "\\."))]
    (and (< major 2)
         (< minor 7))))

(defn minify-assets [project & opts]
  (let [watch? (some #{"watch"} opts)
        profile (remove #{"watch"} opts)
        {:keys [assets options]} (extract-options project profile)]
    (when (and watch? (unsupported-version?))
      (thorw (Exception. "watching for changes is only supported on JDK 1.7+")))
    (if watch?
      (let [watchers
             (for [path (watch-paths assets)]
               (start-watch! path (event-handler assets options)))]
        (doseq [watcher watchers] (.start watcher))
        (.join (first watchers)))
      (when (not @compiled?)
        (minify assets options)
        (reset! compiled? true)))))

(defn register-events! [dir watch-service]
  (.register dir
    watch-service
    (into-array
      [StandardWatchEventKinds/ENTRY_CREATE
       StandardWatchEventKinds/ENTRY_MODIFY
       StandardWatchEventKinds/ENTRY_DELETE
       StandardWatchEventKinds/OVERFLOW])
    (into-array
      [(com.sun.nio.file.SensitivityWatchEventModifier/HIGH)])))
