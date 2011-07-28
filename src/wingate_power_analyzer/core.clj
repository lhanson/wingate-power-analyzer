(ns wingate-power-analyzer.core
  (:gen-class)
  (:require [wingate-power-analyzer.ui :as ui]
            [wingate-power-analyzer.processing :as processing]))

(defn -main []
  (ui/show-ui))
