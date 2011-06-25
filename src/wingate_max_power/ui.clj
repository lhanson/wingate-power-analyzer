(ns wingate-max-power.ui
  (:use seesaw.core
         ;wingate-max-power.core
        ))

(defn show-ui []
  (invoke-now
    (-> (frame :title "Hello", 
               :content "Hello, Seesaw",
               :on-close :exit)
      pack!
      show!)))
