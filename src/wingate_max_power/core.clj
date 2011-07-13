(ns wingate-max-power.core
  (:gen-class)
  (:require [wingate-max-power.ui :as ui]
            [wingate-max-power.processing :as processing]))

(defn -main [& args]
  (if (apply empty? args)
    (ui/show-ui)
    (apply map processing/process-file args)))

;(-main *command-line-args*)
