(ns leiningen.minify-assets
  (:require [asset-minifier.core :as minifier]
            [minify-assets.file-watcher :refer [watch-thread]]
            [clojure.java.io :refer [file]]
            [clojure.string :as s]
            [clojure.core.async :as async :refer [go <! >!]])
  (:import java.security.InvalidParameterException))

(defn extract-options
  "Given a project, returns a seq of cljsbuild option maps."
  [project & [profile]]
  (let [opts (:minify-assets project)
        profile (keyword profile)]
   (cond
    ;;when we have a profile specified, try to get the assets associated with it
    (and opts profile)
    (or (profile opts)
        (throw (InvalidParameterException. (str "WARNING: profile " profile " not found"))))
    ;;if no profile is specified try to look for the assets key
    (some #{:assets} (keys opts)) opts
    ;;if no assets found, look for dev profile by default
    opts (:dev opts)
    ;;no valid options found
    :else (throw (InvalidParameterException. "WARNING: no :minify-assets entry found in project definition.")))))

(defn filter-results [& results]
  (->> results
       (partition 2)
       (remove #(nil? (second %)))
       (map (partial apply str))
       (apply str)))

(defn minify [assets options]
  (println "\nminifying assets...")
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

(defn asset-path [asset]
  (let [asset-file (file asset)]
    (if (.isDirectory asset-file)
      asset
      (.getParent asset-file))))

(defn watch-paths [sources]
  (cond
    (string? sources)
    [(asset-path sources)]
    (coll? sources)
    (set (map asset-path sources))))

(defn create-watchers [options [target sources :as asset]]
  (doall
    (for [path (watch-paths sources)]
      (watch-thread path (event-handler (apply assoc {} asset) options)))))

(defn minify-assets [project & opts]
  (try
    (let [watch? (some #{"watch"} opts)
          profile (first (remove #{"watch"} opts))
          {:keys [assets options]} (extract-options project profile)]
      (when (and watch? (unsupported-version?))
        (throw (InvalidParameterException. "watching for changes is only supported on JDK 1.7+")))
      (if watch?
        (when-let [watchers (not-empty (mapcat (partial create-watchers options) assets))]
          (doseq [watcher watchers] (.start watcher))
          (.join (first watchers)))
        (when (not @compiled?)
          (minify assets options)
          (reset! compiled? true))))
    (catch InvalidParameterException e
      (println (.getMessage e)))))
