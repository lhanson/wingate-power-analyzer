(ns wingate-power-analyzer.core
  (:gen-class)
  (:require [wingate-power-analyzer.ui :as ui]
            [wingate-power-analyzer.processing :as processing]))

(defn -main [& args]
  (if (apply empty? args)
    (ui/show-ui)
    (apply map processing/process-file! args)))

(-main *command-line-args*)
