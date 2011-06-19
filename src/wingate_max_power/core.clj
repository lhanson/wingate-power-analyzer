(ns wingate-max-power.core
  (:import [java.io FileInputStream])
  (:import [org.apache.poi.xssf.usermodel XSSFWorkbook]))

(defn- process-file [filename]
  (let [worksheets (iterator-seq (.iterator (XSSFWorkbook. (FileInputStream. filename))))]
    (println "Processing file" filename ", has" (count worksheets) "worksheets")))

(map process-file *command-line-args*)
