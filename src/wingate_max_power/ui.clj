(ns wingate-max-power.ui
  (:import [javax.swing DefaultListModel])
  (:use seesaw.core
        seesaw.chooser
        seesaw.mig))

(def selected-files-model
  (doto (DefaultListModel.) (.addElement "one") (.addElement "two")))
(def selected-files-list (scrollable (listbox :model selected-files-model)))

(defn success-handler [dialog files]
  (let [new-files (map #(.getName %) files)]
    (. selected-files-model addElement (apply str new-files))))

(defn file-chooser-handler [event]
  (choose-file :multi? true :success-fn success-handler))

(def main-panel
  (mig-panel
    :constraints ["fill, ins 0"]
    :items [ [(button :text "Choose data files" :listen [:action file-chooser-handler]) "wrap"]
             ["Selected files"]
             [selected-files-list "wrap"]
             ["Worksheet prefix"]
             [(text) "wrap"]
             ["Start row"]
             [(text) "wrap"]
             ["Start column"]
             [(text) "wrap"]
           ]))

(defn show-ui []
  (native!)
  (invoke-now
    (-> (frame :title "Wingate Max Power Calculator", 
               :content main-panel
               :minimum-size [640 :by 480]
               :on-close :exit)
      pack!
      show!)))
