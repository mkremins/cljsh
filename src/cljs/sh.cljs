(ns sh
  (:require [clojure.browser.dom :as dom]
            [goog.dom :as gdom]))

(defn cwd []
  (.cwd js/process))

(defn absolute-path? [fpath]
  (= (first fpath) "/"))

(defn expand-path [fpath]
  (if (absolute-path? fpath)
    fpath
    (str (cwd) "/" fpath)))

(defn ls [& [dir]]
  (let [fs (js/require "fs")]
    (vec (.readdirSync fs (expand-path dir)))))

(defn cd [dir]
  (.chdir js/process (expand-path dir))
  (set! (.-innerHTML (.querySelector js/document "#cwd .wd")) (cwd))
  (let [files (.querySelector js/document "#cwd .columns-list")]
    (gdom/removeChildren files)
    (doseq [file (ls)]
      (dom/append files (dom/element [:li {} file]))))
  (cwd))
