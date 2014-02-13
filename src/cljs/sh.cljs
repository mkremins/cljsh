(ns sh)

(defn cd [dir]
  (letfn [(chdir [d] (.chdir js/process d))
          (cwd [] (.cwd js/process))]
    (if (= (first dir) "/")
      (chdir dir)
      (chdir (str (cwd) "/" dir)))
    (cwd)))
