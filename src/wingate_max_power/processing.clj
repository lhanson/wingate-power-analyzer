(ns wingate-max-power.processing
  (:import [java.io FileInputStream]
           [org.apache.poi.ss.usermodel WorkbookFactory]
           [org.apache.poi.xssf.usermodel XSSFWorkbook]))

(defn process-file! [infile outfile]
  "Computes the max power from the given infile and writes report rows to outfile"
  ; infile
  (let [input-workbook (WorkbookFactory/create (FileInputStream. infile))
        input-worksheets (for [i (range (.getNumberOfSheets input-workbook))] (.getSheetAt input-workbook i))]
    (doseq [sheet input-worksheets] (println "Sheet" (.getSheetName sheet))))

  ; outfile
  (let [output-workbook (if (.exists outfile) (XSSFWorkbook. (FileInputStream. outfile)) (XSSFWorkbook.))
        sheet (if (> (.getNumberOfSheets output-workbook) 0)
                  (.getSheetAt output-workbook 0)
                  (.createSheet output-workbook))
        outstream (java.io.FileOutputStream. outfile)]

    (.setSheetName output-workbook 0 "Wingate Peak Power")
    (let [rownum (if (= 0 (.getLastRowNum sheet)) 0 (+ 2 (.getLastRowNum sheet)))
          cell (.createCell (.createRow sheet rownum) 0)]
      ; TODO: bold and underline this
      (.setCellValue cell (str infile))
      (let [newcell (.createCell (.createRow sheet (inc rownum)) 0)]
        (.setCellValue newcell "Next cell")))
    (.write output-workbook outstream)
    )
)
