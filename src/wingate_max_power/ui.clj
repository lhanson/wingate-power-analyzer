(ns wingate-max-power.ui
  (:import [javax.swing DefaultListModel]
           [javax.swing.event ListDataListener])
  (:require [wingate-max-power.processing :as processing])
  (:use [clojure.contrib.string :only (blank? tail)]
        seesaw.core
        seesaw.chooser
        seesaw.mig))

(declare update-calculate-button! remove-files-button selected-files-list)

(def list-listener
  (proxy [ListDataListener] []
    (intervalAdded   [event] (update-calculate-button!))
    (intervalRemoved [event] (update-calculate-button!))))

(def selected-files-model 
  (doto (DefaultListModel.) (.addListDataListener list-listener)))
(defn- selection-handler [event]
  (config! remove-files-button :enabled? (selection selected-files-list {:multi? true})))
(def selected-files-list
  (listbox :model selected-files-model :listen [:selection selection-handler]))
(defn- remove-files-handler [event]
  (doseq [selected (selection selected-files-list {:multi? true})]
    (. selected-files-model removeElement selected)))
(def remove-files-button
  (button :text "Remove selected files" :enabled? false :listen [:action remove-files-handler]))
(def worksheet-prefix-textbox (text))

(defn- calculate-handler [event]
  (try
    (let [outfile-tmp (choose-file :type :save :filters [["Excel files" ["xlsx"]]])
          outfile (if (not= ".xlsx" (tail 5 (.getName outfile-tmp)))
                    (java.io.File. (str (.getCanonicalPath outfile-tmp) ".xlsx"))
                    outfile-tmp)]
      (let [files (map #(. selected-files-model getElementAt %) (range (. selected-files-model size)))
            worksheet-prefix (.getText worksheet-prefix-textbox)]
        (doseq [infile files]
          (try
            (processing/process-file! infile outfile (if (not (blank? worksheet-prefix)) {:worksheet-prefix worksheet-prefix}))
            (catch Throwable t (alert (str "Could not process " infile ": " t)))))))
      (alert "Finished!")
    (catch Throwable t (alert (str "Error:" t)))))

(def calculate-power-button
  (button :text "Calculate Max Power" :enabled? false :listen [:action calculate-handler]))

(defn- success-handler [dialog files]
  (doseq [file files]
    (if (not (. selected-files-model contains file))
      (. selected-files-model addElement file))))

(defn- file-chooser-handler [event]
  (choose-file :multi? true :success-fn success-handler :filters [["Excel files" ["xls" "xlsx"]]]))

(def main-panel
  (mig-panel
    :constraints ["fill, ins 0"]
    :items [ [(button :text "Choose data files" :listen [:action file-chooser-handler]) "wrap"]
             ["Input files"]
             [(scrollable selected-files-list) "growx, wrap"]
             [remove-files-button "wrap"]
             ["Worksheet prefix"]
             [worksheet-prefix-textbox "wrap, growx"]
             [calculate-power-button "wrap"]]))

(defn- update-calculate-button! []
  (config! calculate-power-button :enabled? (> (. selected-files-model getSize) 0)))

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
