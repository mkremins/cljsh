(ns app
  (:require [cljs.repl :as repl]))

(def log (.getElementById js/document "log"))

(def session
  (atom {:history {:commands [] :cursor 0}}))

(defn add-history-item! [code]
  (let [next-cursor (count (get-in @session [:history :commands]))]
    (swap! session update-in [:history :commands] #(conj % code))
    (swap! session assoc-in [:history :cursor] next-cursor)))

(defn next-history-item! []
  (let [{:keys [commands cursor]} (:history @session)]
    (when-let [next-item (get commands (inc cursor))]
      (swap! session update-in [:history :cursor] inc)
      next-item)))

(defn prev-history-item! []
  (let [{:keys [commands cursor]} (:history @session)]
    (when-let [prev-item (get commands cursor)]
      (swap! session update-in [:history :cursor] dec)
      prev-item)))

(defn log-entry [code result]
  (let [entry (.createElement js/document "div")
        result (if-let [err (:error result)]
                 (str "<p class=\"err out\">" err "</p>")
                 (str "<p class=\"ok out\">" (:value result) "</p>"))]
    (.add (.-classList entry) "entry")
    (set! (.-innerHTML entry)
          (str "<p class=\"in\"><span class=\"prompt\">$</span> " code "</p>"
               result))
    entry))

(defn eval-print! [code]
  (.appendChild log (log-entry code (repl/evaluate-code code))))

(defn key-code [ev]
  (or (.-key ev) (.-keyCode ev) (.-which ev)))

(def keybinds
  {13 (fn [input]
        (let [code (.-value input)]
          (eval-print! code)
          (add-history-item! code))
        (set! (.-value input) ""))

   38 (fn [input]
        (set! (.-value input) (prev-history-item!)))

   40 (fn [input]
        (set! (.-value input) (next-history-item!)))})

(defn handle-key [ev]
  (when-let [keybind (keybinds (key-code ev))]
    (let [input (.-target ev)]
      (keybind input))))

(let [input (.getElementById js/document "input")]
  (.addEventListener input "keydown" handle-key))
