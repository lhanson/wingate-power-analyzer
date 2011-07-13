(ns wingate-max-power.processing
  (:import [java.io FileInputStream]
           [org.apache.poi.ss.usermodel WorkbookFactory]))

(defn process-file! [infile outfile]
  (println "Processing file" infile)
  (let [workbook (WorkbookFactory/create (FileInputStream. infile))
        worksheets (for [i (range (.getNumberOfSheets workbook))] (.getSheetAt workbook i))]
    (doseq [sheet worksheets] (println "Sheet" (.getSheetName sheet)))))
