(defproject stubby "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.0.1"]
                 [compojure "1.1.1"]
                 [slingshot "0.10.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.iplantc/clojure-commons "1.4.0-SNAPSHOT"]
                 [slingshot "0.10.3"]]
  :plugins [[lein-ring "0.7.1"]]
  :ring {:handler stubby.core/app})
