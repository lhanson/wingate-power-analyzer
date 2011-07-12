(ns wingate-max-power.ui
  (:import [javax.swing DefaultListModel])
  (:require [clojure.string :as str])
  (:use seesaw.core
        seesaw.chooser
        seesaw.mig))

(def selected-files-model (DefaultListModel.))
(def selected-files-list (listbox :model selected-files-model))

(defn- success-handler [dialog files]
  (doseq [file files]
    (if (not (. selected-files-model contains file))
      (. selected-files-model addElement file))))

(defn- remove-files-handler [event]
  (doseq [selected (selection selected-files-list {:multi? true})]
    (. selected-files-model removeElement selected)))

(defn- file-chooser-handler [event]
  (choose-file :multi? true :success-fn success-handler))

(defn- calculate-handler [event]
  (let [outfile (choose-file :type :save)]
    (alert (str "Output:" outfile))
    (let [files (map #(. selected-files-model getElementAt %) (range (. selected-files-model size)))]
      (alert (str "mapped " (count files) " files: " (str/join ", " files))))
    (alert "DONE!")))

(def main-panel
  (mig-panel
    :constraints ["fill, ins 0"]
    :items [ [(button :text "Choose data files" :listen [:action file-chooser-handler]) "wrap"]
             ["Selected files"]
             [(scrollable selected-files-list) "wrap, growx"]
             [(button :text "Remove files" :listen [:action remove-files-handler]) "wrap"]
             ["Worksheet prefix"]
             [(text) "wrap, growx"]
             ["Start row"]
             [(text) "wrap, growx"]
             ["Start column"]
             [(text) "wrap, growx"]
             [(button :text "Calculate Max Power" :listen [:action calculate-handler]) "wrap"]
           ]))

(defn show-ui []
  (native!)
  (invoke-now
    (-> (frame :title "Wingate Max Power Calculator", 
               :content main-panel
               :minimum-size [640 :by 480]
               ;:on-close :exit
               )
      pack!
      show!)))
