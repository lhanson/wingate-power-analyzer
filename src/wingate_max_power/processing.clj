(ns wingate-max-power.processing
  (:import [java.io FileInputStream]
           [org.apache.poi.ss.usermodel Font WorkbookFactory]
           [org.apache.poi.xssf.usermodel XSSFWorkbook]))

(declare compute-sheet)

(defn process-file! [infile outfile power-start-pos time-start-pos & [{:keys [worksheet-prefix]}]]
  "Computes the max power from the given infile and writes report rows to outfile"
  (let [input-workbook (WorkbookFactory/create (FileInputStream. infile))
        input-worksheets (for [i (range (.getNumberOfSheets input-workbook))] (.getSheetAt input-workbook i))
        output-workbook (if (.exists outfile) (XSSFWorkbook. (FileInputStream. outfile)) (XSSFWorkbook.))
        sheet (if (> (.getNumberOfSheets output-workbook) 0)
                  (.getSheetAt output-workbook 0)
                  (.createSheet output-workbook))
        outstream (java.io.FileOutputStream. outfile)]

    (.setSheetName output-workbook 0 "Wingate Peak Power")

    (let [rownum (if (= 0 (.getLastRowNum sheet)) 0 (+ 2 (.getLastRowNum sheet)))
          file-header-font (doto (.createFont output-workbook)
                      (.setBoldweight Font/BOLDWEIGHT_BOLD)
                      (.setUnderline Font/U_SINGLE)
                      (.setFontHeightInPoints 14))
          subheader-font (doto (.createFont output-workbook) (.setUnderline Font/U_SINGLE))
          bold-cellstyle (doto (.createCellStyle output-workbook) (.setFont file-header-font))
          underline-cellstyle (doto (.createCellStyle output-workbook) (.setFont subheader-font))
          file-header-row (.createRow sheet rownum)
          sub-header-row (.createRow sheet (inc rownum))]
      (.setColumnWidth sheet 0 10000)
      (.setColumnWidth sheet 1 4750)
      (.setColumnWidth sheet 2 4750)
      ; File header row
      (doto (.createCell file-header-row 0)
        (.setCellStyle bold-cellstyle)
        (.setCellValue (str infile)))
      ; Subheaders
      (doto (.createCell sub-header-row 0) (.setCellStyle underline-cellstyle) (.setCellValue "Sheet"))
      (doto (.createCell sub-header-row 1) (.setCellStyle underline-cellstyle) (.setCellValue "Peak 1s power"))
      (doto (.createCell sub-header-row 2) (.setCellStyle underline-cellstyle) (.setCellValue "Peak occurs at"))
      (doseq [[newrow inputsheet]
              (map vector
                (iterate #(.createRow sheet (inc (.getRowNum %))) (.createRow sheet (+ 2 rownum)))
                (filter #(or (nil? worksheet-prefix) (.startsWith (.getSheetName %) worksheet-prefix)) input-worksheets))]
        (let [sheet-results (compute-sheet inputsheet power-start-pos time-start-pos)]
          (doto (.createCell newrow 0) (.setCellValue (.getSheetName inputsheet)))
          (doto (.createCell newrow 1) (.setCellValue (:peak-power sheet-results)))
          (doto (.createCell newrow 2) (.setCellValue (:peak-power-time sheet-results))))))
    (.write output-workbook outstream)))

(defn- compute-sheet [worksheet power-start-pos time-start-pos]
  "Calculates the peak 1s power for the given worksheet.
   Returns a map containing :peak-power and :peak-power-time."
  (println "Doing sheet" (.getSheetName worksheet))
  (if (not= (second power-start-pos) (second time-start-pos))
    (throw (Exception. "Expecting the power and time columns to start on the same row")))
  (loop [row (.getRow worksheet (second power-start-pos))]
    (if row
      (do
        (println "Power: " (.getNumericCellValue (.getCell row (first power-start-pos))) ", time: " (.getNumericCellValue (.getCell row (first time-start-pos))))
        (recur (.getRow worksheet (inc (.getRowNum row)))))))
  {:peak-power "200w" :peak-power-time "20:01"})
