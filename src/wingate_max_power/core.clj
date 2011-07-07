(ns wingate-max-power.core
  (:gen-class)
  (:use wingate-max-power.ui)
  (:import [java.io FileInputStream])
  (:import [org.apache.poi.ss.usermodel WorkbookFactory]))

(defn- process-file [filename]
  (println "Processing file" filename)
  (let [workbook (WorkbookFactory/create (FileInputStream. filename))
        worksheets (for [i (range (.getNumberOfSheets workbook))] (.getSheetAt workbook i))]
    (doseq [sheet worksheets] (println "Sheet" (.getSheetName sheet)))))

(defn -main [& args]
  (if (apply empty? args)
    (wingate-max-power.ui/show-ui)
    (apply map process-file args)))

(-main *command-line-args*)
