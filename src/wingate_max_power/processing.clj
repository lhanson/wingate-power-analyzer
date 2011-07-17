(ns wingate-max-power.processing
  (:import [java.io FileInputStream]
           [org.apache.poi.ss.usermodel Font WorkbookFactory]
           [org.apache.poi.xssf.usermodel XSSFWorkbook]))

(declare compute-sheet)

(defn process-file! [infile outfile]
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
          bold-font (doto (.createFont output-workbook)
                      (.setBoldweight Font/BOLDWEIGHT_BOLD)
                      (.setUnderline Font/U_SINGLE)
                      (.setFontHeightInPoints 14))
          bold-cellstyle (doto (.createCellStyle output-workbook) (.setFont bold-font))
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
      (doto (.createCell sub-header-row 0) (.setCellValue "Sheet"))
      (doto (.createCell sub-header-row 1) (.setCellValue "Peak 1s power"))
      (doto (.createCell sub-header-row 2) (.setCellValue "Peak occurs at"))
      (doseq [[newrow inputsheet]
              (map vector
                (iterate #(.createRow sheet (inc (.getRowNum %))) (.createRow sheet (+ 2 rownum)))
                input-worksheets)]
        (doto (.createCell newrow 0) (.setCellValue (.getSheetName inputsheet)))
        (doto (.createCell newrow 1) (.setCellValue "1024w"))
        (doto (.createCell newrow 2) (.setCellValue "1:24")))
      (.write output-workbook outstream))))

(defn- compute-sheet [worksheet]
  "Calculates the peak 1s power for the given worksheet"
  (println "Doing sheet" (.getSheetName worksheet)))
