(defproject siren "0.2.1"
            :description "Easy notifications, Growl style (Clojurescript)"
            :source-path "src"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2356"]
                           [domina "1.0.2"]
                           [enfocus "2.1.0"]]
            :plugins [[lein-cljsbuild "1.0.3"]]
            :cljsbuild
            {:builds [{:id           "dev"
                       :source-paths ["src"]
                       :compiler     {
                                       :output-to     "public/siren.js"
                                       :output-dir    ".cljsbuild"
                                       :optimizations :none
                                       :source-map    true
                                     }}
                      ]})