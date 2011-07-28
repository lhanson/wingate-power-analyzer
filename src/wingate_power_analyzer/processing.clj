(ns wingate-power-analyzer.processing
  (:use [clojure.string :only (trim lower-case)]
        [clojure.contrib.string :only (as-str)]
        ;clojure.contrib.logging [clj-logging-config.log4j]
        ;[clojure.contrib.pprint]
        [clojure.contrib.duck-streams :only (append-spit)]
        )
  (:import [java.io FileInputStream]
           [org.apache.poi.ss.usermodel Cell Font WorkbookFactory]
           [org.apache.poi.xssf.usermodel XSSFWorkbook]
           [org.apache.log4j FileAppender SimpleLayout]))

;(set-logger! :appender (FileAppender. (SimpleLayout.) "wingate-max-power.log"))

(declare compute-sheet)

(def *log-filename* "wingate-max-power.log")

(defn- debug
  "Short-term logging workaround until I can sort out file appenders in clj-logging-config"
  ([message] (debug *log-filename* message))
  ([filename message] (append-spit filename (str message "\n"))))

(defn test-log []
  ;(error "Testing error logging...")
  )

(defn process-file! [infile outfile & [{:keys [worksheet-prefix power-start-pos time-start-pos]}]]
  "Computes the max power from the given infile and writes report rows to outfile"
  (debug (str "Processing" infile))
  (let [input-workbook (WorkbookFactory/create (FileInputStream. infile))
        input-worksheets (for [i (range (.getNumberOfSheets input-workbook))] (.getSheetAt input-workbook i))
        output-workbook (if (.exists outfile) (XSSFWorkbook. (FileInputStream. outfile)) (XSSFWorkbook.))
        sheet (if (> (.getNumberOfSheets output-workbook) 0)
                  (.getSheetAt output-workbook 0)
                  (.createSheet output-workbook "Wingate Peak Power"))
        outstream (java.io.FileOutputStream. outfile)]

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
      (doto (.createCell sub-header-row 1) (.setCellStyle underline-cellstyle) (.setCellValue "Peak 1s power (watts)"))
      (doto (.createCell sub-header-row 2) (.setCellStyle underline-cellstyle) (.setCellValue "Peak begins at (seconds)"))
      (doseq [[newrow inputsheet]
              (map vector
                (iterate #(.createRow sheet (inc (.getRowNum %))) (.createRow sheet (+ 2 rownum)))
                (filter #(or (nil? worksheet-prefix) (re-matches (re-pattern (str worksheet-prefix "\\s*\\d+")) (.getSheetName %))) input-worksheets))]
        (let [sheet-results (compute-sheet inputsheet power-start-pos time-start-pos)]
          (doto (.createCell newrow 0) (.setCellValue (.getSheetName inputsheet)))
          (doto (.createCell newrow 1) (.setCellValue (:peak-power sheet-results)))
          (doto (.createCell newrow 2) (.setCellValue (:peak-power-time sheet-results))))))
    (.write output-workbook outstream)))

(defn- get-rows
  "Returns a sequence of non-empty rows in the worksheet. If start (and end) are given,
   the rows between those indices (inclusive) will be given."
  ([worksheet] (get-rows worksheet (.getFirstRowNum worksheet) (.getLastRowNum worksheet)))
  ([worksheet start] (get-rows worksheet start (.getLastRowNum worksheet)))
  ([worksheet start end] (filter identity (map #(.getRow worksheet %) (range start end)))))

(defn- get-cells
  "Returns a sequence of cells from the given worksheet.
  [worksheet] returns all cells
  [worksheet column-index] returns all cells in the given column
  [worksheet column-index row-index] returns all cells in the given column below the row index"
  ([worksheet]
    (apply concat (map #(iterator-seq (.cellIterator %)) (get-rows worksheet))))
  ([worksheet column-index]
    (get-cells worksheet column-index 0))
  ([worksheet column-index row-index]
    (filter #(and (= (.getColumnIndex %) column-index)
                  (>= (.getRowIndex %) row-index))
            (get-cells worksheet))))

(defn- get-numeric-column-below [cell]
  "Returns the [col row] coordinates of the next numeric cell below the one given"
  (let [worksheet (.getSheet (.getRow cell))
        cells (get-cells worksheet (.getColumnIndex cell) (inc (.getRowIndex cell)))
        match (some #(if (= Cell/CELL_TYPE_NUMERIC (.getCellType %)) %) cells)]
    (if match (vector (.getColumnIndex match) (.getRowIndex match)))))

(defn- find-start-pos [worksheet kind]
  "Finds a cell containing a string matching 'kind' and returns an [x y] pair representing
   the position of the next populated cell below it"
  (if-let [start-cell (some #(if (= (as-str kind) (trim (lower-case %))) %) (get-cells worksheet))]
    (get-numeric-column-below start-cell)))

(defn- load-power-data [worksheet power-start-pos time-start-pos]
  "Returns a sequence of [power time] vectors from the data in worksheet"
  (let [power-pos (or power-start-pos (find-start-pos worksheet :watts))
        time-pos (or time-start-pos (find-start-pos worksheet :seconds))]
    (if (or (nil? power-pos) (nil? time-pos))
      (throw (IllegalArgumentException. (str "Unable to find power and time columns automatically."
                                             "\n\nDid you specify the correct worksheet prefix?"))))
    (if (not= (second power-pos) (second time-pos))
      (throw (Exception. "Expecting the power and time columns to start on the same row")))
    (if-let [row (.getRow worksheet (second power-pos))]
      (concat (vector (vector (.getNumericCellValue (.getCell row (first power-pos)))
                              (.getNumericCellValue (.getCell row (first time-pos)))))
              (load-power-data worksheet (vector (first power-pos) (inc (second power-pos)))
                               (vector (first time-pos) (inc (second time-pos))))))))

(defn- average-power [samples]
  "For the given sequence of [power time] pairs, returns a vector with the average power
   and the corresponding start time"
  [(/ (apply + (map first samples)) (count samples))
   (second (first samples))])

(defn- bigger-sample [[p1 t1 :as a] [p2 t2 :as b]]
  "Compares the [power time] pairs and returns the one with the higher power"
  (if (> p1 p2) a b))

(defn- peak-1-second-power [data]
  "Returns the data point from the input representing the highest moving average over 1 second."
  (let [slices (partition 10 1 data)
        rolling-averages (map #(average-power %) slices)]
    (reduce bigger-sample rolling-averages)))

(defn- compute-sheet [worksheet & [power-start-pos time-start-pos]]
  "Calculates the peak 1s power for the given worksheet.
   Returns a map containing :peak-power and :peak-power-time."
  (debug (str "Computing sheet " (.getSheetName worksheet)))
  (let [peak (peak-1-second-power (load-power-data worksheet power-start-pos time-start-pos))]
    (println "Peak:" peak)
    {:peak-power (first peak) :peak-power-time (second peak)}))
