(ns wingate-max-power.ui
  (:import [javax.swing DefaultListModel])
  (:use seesaw.core
        seesaw.chooser
        seesaw.mig))

(def selected-files-model (DefaultListModel.))
(def selected-files-list (listbox :model selected-files-model))

(defn success-handler [dialog files]
  (doseq [file files]
    (if (not (. selected-files-model contains file))
      (. selected-files-model addElement file))))

(defn remove-files-handler [event]
  (alert "Removing files")
  (alert (str "Type:" (type selected-files-list)))
  (alert (str "Removing " (count (selection selected-files-list)) " files"))
  (doseq [selected (selection selected-files-list {:multi? true})] (alert "DOING")
    (alert (str "Selected: " selected))
    ((. selected-files-model removeElement selected))))

(defn file-chooser-handler [event]
  (choose-file :multi? true :success-fn success-handler))

(def main-panel
  (mig-panel
    :constraints ["fill, ins 0"]
    :items [ [(button :text "Choose data files" :listen [:action file-chooser-handler]) "wrap"]
             ["Selected files"]
             [(scrollable selected-files-list) "wrap"]
             [(button :text "Remove files" :listen [:action remove-files-handler]) "wrap"]
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
