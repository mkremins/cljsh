(ns app
  (:require [cljs.repl :as repl]
            [goog.events.KeyCodes :as key]))

(def log (.getElementById js/document "log"))

(def session
  (atom {:history {:commands [] :cursor 0}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; history: pure functions returning modified copies of immutable datastructure

(defn append-item [history code]
  (-> history
    (update-in [:commands] #(conj % code))
    (assoc :cursor (count (:commands history)))))

(defn read-item [{:keys [commands cursor]}]
  (get commands cursor))

(defn advance-cursor [history]
  (let [new-history (update-in history [:cursor] inc)]
    (if (read-item new-history) new-history history)))

(defn withdraw-cursor [history]
  (let [new-history (update-in history [:cursor] dec)]
    (if (read-item new-history) new-history history)))

;; history: impure functions modifying atomic (mutable) session state

(defn add-history-item! [code]
  (swap! session update-in [:history] #(append-item % code)))

(defn next-history-item! []
  (let [history (:history @session)
        new-history (advance-cursor history)]
    (when-not (= (:cursor history) (:cursor new-history))
      (swap! session assoc-in [:history] new-history)
      (read-item new-history))))

(defn prev-history-item! []
  (let [history (:history @session)]
    (when-let [prev-item (read-item history)]
      (swap! session update-in [:history] withdraw-cursor)
      prev-item)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn log-entry [code {:keys [error value]}]
  (let [entry (.createElement js/document "div")
        result (if error
                 (str "<p class=\"err out\">" error "</p>")
                 (str "<p class=\"ok out\">" value "</p>"))]
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
  {key/ENTER
   (fn [input]
     (let [code (.-value input)]
       (eval-print! code)
       (add-history-item! code))
     (set! (.-value input) ""))

   key/UP
   (fn [input]
     (set! (.-value input) (prev-history-item!)))

   key/DOWN
   (fn [input]
     (set! (.-value input) (next-history-item!)))})

(defn handle-key [ev]
  (when-let [keybind (keybinds (key-code ev))]
    (let [input (.-target ev)]
      (keybind input))))

(let [input (.getElementById js/document "input")]
  (.addEventListener input "keydown" handle-key))
