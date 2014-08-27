(ns minify-assets.file-watcher
  (:require [clojure.set :refer [rename-keys]]
            [clojure.java.io :refer [file]])
  (:import
       [java.nio.file
        FileSystems
        Path
        Paths
        StandardWatchEventKinds]))

(defn register-events! [dir watch-service]
  (.register dir
             watch-service
             (into-array
               [StandardWatchEventKinds/ENTRY_CREATE
                StandardWatchEventKinds/ENTRY_MODIFY
                StandardWatchEventKinds/ENTRY_DELETE
                StandardWatchEventKinds/OVERFLOW])
             (into-array [(com.sun.nio.file.SensitivityWatchEventModifier/HIGH)])))

(defn watch-loop [watch-service handler]
  (while true
      (when-let [k (.take watch-service)]
        (doseq [event (.pollEvents k)]
          (handler event))
        (.reset k))))

(defn watch [path handler]
  (let [dir  (-> path (file) (.toURI) (Paths/get))]
    (with-open [watch-service (.newWatchService (FileSystems/getDefault))]
      (register-events! dir watch-service)
      (watch-loop watch-service handler))))

(defn watch-thread [path handler]
  (Thread. #(watch path handler)))
