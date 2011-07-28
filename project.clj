(defproject wingate-power-analyzer "0.0.1-SNAPSHOT"
  :description "A program which computes max 1-second power from Wingate test data"
  :main wingate-power-analyzer.core
  :dependencies [[clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.apache.poi/poi-ooxml "3.8-beta3"]
                 [seesaw "1.0.10"]
                 [log4j "1.2.16" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [clj-logging-config "1.4"]])
