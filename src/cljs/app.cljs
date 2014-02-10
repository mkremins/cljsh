(ns app
  (:require [cljs.repl :as repl]))

(def log (.getElementById js/document "log"))

(def session
  (atom {:history {:commands [] :cursor 0}}))

(defn add-history-item! [code]
  (let [next-cursor (count (get-in @session [:history :commands]))]
    (swap! session update-in [:history :commands] #(conj % code))
    (swap! session assoc-in [:history :cursor] next-cursor)))

(defn prev-history-item! []
  (let [{:keys [commands cursor]} (:history @session)]
    (when-let [prev (get commands cursor)]
      (swap! session update-in [:history :cursor] dec)
      prev)))

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

(def keybinds
  {13 (fn [input]
        (let [code (.-value input)]
          (eval-print! code)
          (add-history-item! code))
        (set! (.-value input) ""))

   38 (fn [input]
        (set! (.-value input) (prev-history-item!)))})

(defn handle-key [ev]
  (when-let [keybind (keybinds (key-code ev))]
    (let [input (.-target ev)]
      (keybind input))))

(let [input (.getElementById js/document "input")]
  (.addEventListener input "keydown" handle-key))
