(ns sh
  (:require [clojure.browser.dom :as dom]
            [clojure.string :as string]
            [goog.dom :as gdom]))

(defn cwd []
  (.cwd js/process))

(defn home []
  (aget js/process "env" "HOME"))

(defn absolute-path? [fpath]
  (= (first fpath) "/"))

(defn expand-path [fpath]
  (let [fpath (string/replace fpath #"~" (home))]
    (if (absolute-path? fpath)
      fpath
      (str (cwd) "/" fpath))))

(defn ls [& [dir]]
  (let [fs (js/require "fs")]
    (vec (.readdirSync fs (if dir (expand-path dir) (cwd))))))

(defn cd [dir]
  (.chdir js/process (expand-path dir))
  (set! (.-innerHTML (.querySelector js/document "#cwd .wd")) (cwd))
  (let [files (.querySelector js/document "#cwd .columns-list")]
    (gdom/removeChildren files)
    (doseq [file (ls)]
      (dom/append files (dom/element [:li {} file]))))
  (cwd))
