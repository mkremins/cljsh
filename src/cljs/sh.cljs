(ns sh)

(defn cwd []
  (.cwd js/process))

(defn cd [dir]
  (letfn [(chdir [d] (.chdir js/process d))]
    (if (= (first dir) "/")
      (chdir dir)
      (chdir (str (cwd) "/" dir)))
    (cwd)))

(defn ls [& [dir]]
  (let [fs (js/require "fs")]
    (vec (.readdirSync fs (str (cwd) "/" dir)))))
