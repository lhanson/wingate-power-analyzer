(ns wingate-max-power.ui
  (:import [javax.swing DefaultListModel])
  (:import [javax.swing.event ListDataListener])
  (:require [clojure.string :as str])
  (:use seesaw.core
        seesaw.chooser
        seesaw.mig))

(declare update-button-states!)

(def list-listener
  (proxy [ListDataListener] []
    (intervalAdded   [event] (update-button-states!))
    (intervalRemoved [event] (update-button-states!))))
(def selected-files-model 
  (doto (DefaultListModel.) (.addListDataListener list-listener)))
(def selected-files-list (listbox :model selected-files-model))

(defn- remove-files-handler [event]
  (doseq [selected (selection selected-files-list {:multi? true})]
    (. selected-files-model removeElement selected)))

(def remove-files-button
  (button :text "Remove selected files" :enabled? false :listen [:action remove-files-handler]))

(defn- calculate-handler [event]
  (let [outfile (choose-file :type :save)]
    (alert (str "Output:" outfile " of type " (type outfile) ", exists: " (. outfile exists)))
    (let [files (map #(. selected-files-model getElementAt %) (range (. selected-files-model size)))]
      (alert (str "mapped " (count files) " files: " (str/join ", " files))))
    (alert "DONE!")))

(def calculate-power-button
  (button :text "Calculate Max Power" :enabled? false :listen [:action calculate-handler]))

(defn- success-handler [dialog files]
  (doseq [file files]
    (if (not (. selected-files-model contains file))
      (. selected-files-model addElement file))))

(defn- file-chooser-handler [event]
  (choose-file :multi? true :success-fn success-handler))

(def main-panel
  (mig-panel
    :constraints ["fill, ins 0"]
    :items [ [(button :text "Choose data files" :listen [:action file-chooser-handler]) "wrap"]
             ["Input files"]
             [(scrollable selected-files-list) "wrap, growx"]
             [remove-files-button "wrap"]
             ["Worksheet prefix"]
             [(text) "wrap, growx"]
             ["Start row"]
             [(text) "wrap, growx"]
             ["Start column"]
             [(text) "wrap, growx"]
             [calculate-power-button "wrap"]]))

(defn- update-button-states! []
  (alert "Updateing button states")
  (let [list-populated (> (. selected-files-model getSize) 0)]
    (config! remove-files-button :enabled? list-populated)
    (config! calculate-power-button :enabled? list-populated))
  (alert "DONE updating"))


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
