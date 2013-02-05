(defproject siren "0.2.0"
  :description "Easy notifications, Growl style (Clojurescript)"
  :source-path "src"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [domina "1.0.0"]
                 [enfocus "1.0.0-alpha3"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :cljsbuild {:builds [{:source-path "src"
                        :jar true
                        :compiler {:pretty-print true
                                   :output-dir ".cljsbuild/siren"
                                   :output-to "public/siren.js"}}]})