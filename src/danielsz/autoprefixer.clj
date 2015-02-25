(ns danielsz.autoprefixer
  {:boot/export-tasks true}
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as core]
   [boot.util       :as util]
   [boot.tmpdir     :as tmpd]))

(defn- find-css-files [fs files]
  (->> fs
       core/input-files
       (core/by-name files)
       (map (juxt core/tmppath core/tmpfile identity))))

(core/deftask autoprefixer
  [f files    FILES       [str] "A vector of filenames to process with autoprefixer."
   b browsers BROWSERS    str   "A string describing browsers autoprefixer will target."]
  (let [tmp-dir (core/temp-dir!)]
    (core/with-pre-wrap fileset
      (doseq [[in-path in-file file] (find-css-files fileset files)]
        (boot.util/info "Autoprefixing %s\n" (:path file))
        (let [out-file (doto (io/file tmp-dir in-path) io/make-parents)]
          ; See https://github.com/boot-clj/boot/issues/141
          (if browsers
            (util/dosh "autoprefixer" (.getPath in-file) "-o" (.getPath out-file) "-b" browsers)
            (util/dosh "autoprefixer" (.getPath in-file) "-o" (.getPath out-file)))))
      (-> fileset
          (core/add-resource tmp-dir)
          core/commit!))))
