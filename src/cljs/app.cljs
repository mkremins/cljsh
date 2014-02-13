(ns app
  (:require [cljs.repl :as repl]
            [clojure.browser.dom :as dom]
            [goog.events.KeyCodes :as key]))

(def log (.getElementById js/document "log"))

(def session
  (atom {:history {:commands [] :cursor 0}}))

(defn update [nest k f & args]
  (apply (partial update-in nest [k] f) args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; history: pure functions returning modified copies of immutable datastructure

(defn append-item [history code]
  (-> history
    (update :commands #(conj % code))
    (assoc :cursor (count (:commands history)))))

(defn read-item [{:keys [commands cursor]}]
  (get commands cursor))

(defn advance-cursor [history]
  (let [new-history (update history :cursor inc)]
    (if (read-item new-history) new-history history)))

(defn withdraw-cursor [history]
  (let [new-history (update history :cursor dec)]
    (if (read-item new-history) new-history history)))

;; history: impure functions modifying atomic (mutable) session state

(defn add-history-item! [code]
  (swap! session update :history #(append-item % code)))

(defn next-history-item! []
  (let [history (:history @session)
        new-history (advance-cursor history)]
    (when-not (= (:cursor history) (:cursor new-history))
      (swap! session assoc :history new-history)
      (read-item new-history))))

(defn prev-history-item! []
  (let [history (:history @session)]
    (when-let [prev-item (read-item history)]
      (swap! session update :history withdraw-cursor)
      prev-item)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn log-entry [code {:keys [error value]}]
  (dom/element
    [:div {:class "entry"}
          [:p {:class "in"} [:span {:class "prompt"} "$ "] code]
          (if error
            [:p {:class "err out"} (str error)]
            [:p {:class "ok out"} (str value)])]))

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

(repl/init)
(let [input (.getElementById js/document "input")]
  (.addEventListener input "keydown" handle-key))
