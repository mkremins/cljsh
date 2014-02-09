(ns app
  (:require [cljs.repl :as repl]))

(def input (.getElementById js/document "input"))
(def log (.getElementById js/document "log"))

(defn log-entry [in out]
  (let [entry (.createElement js/document "div")]
    (.add (.-classList entry) "entry")
    (set! (.-innerHTML entry)
          (str "<p class=\"in\"><span class=\"prompt\">$</span> " in "</p>"
               "<p class=\"out\">" out "</p>"))
    entry))

(defn eval-print! [code]
  (let [result (repl/evaluate-code code)]
    (.appendChild log (log-entry code (:value result)))))

(defn key-code [ev]
  (or (.-key ev) (.-keyCode ev) (.-which ev)))

(defn handle-key [ev]
  (condp = (key-code ev)
    13 (do (eval-print! (.-value input))
           (set! (.-value input) ""))
    nil))

(.addEventListener input "keydown" handle-key)
