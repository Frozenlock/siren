(ns siren.core
  (:require [domina :as domina]
            [domina.events :as events]
            [domina.css :as css]
            [enfocus.core :as ef]
            [clojure.string :as string])
  (:require-macros [enfocus.macros :as em]))


(defn- create-siren-container []
  (let [id "siren-container"]
    (domina/append! (domina.css/sel "body") (str "<div id=\"" id "\"></div>"))
    (-> (domina/by-id id)
        (domina/set-styles! {:position "fixed" :top "10px" :right "10px"}))))


(defn- select-style [&[{:keys [style]}]]
  ":dark, :light, or :custom (if you intend to use CSS to modify the siren appearance)."
  (let [base-style {:font-size "15px" :margin "10px"  :opacity "0" :width "300px"
                    :padding "10px" :border-radius "10px" :box-shadow "5px 5px 10px black"}
        dark (merge base-style {:background "rgba(0, 0, 0, 0.7)" :color "white"})
        light (merge base-style {:background "rgba(255, 255, 255, 0.7)" :color "black"})
        css {}]
    (get {:dark dark :light light :css css} style (or style dark))))

(defn set-siren-content [element &[{:keys [content html-content]}]]
  (ef/at element (if html-content
                   (em/html-content html-content)
                   (em/content content))))
   
  

(defn- create-siren-box [&[args]]
  (let [class "siren-box"
        id (gensym)
        style (select-style args)]
    (when-not (domina/by-id "siren-container")
      (create-siren-container))
    (domina/append! (domina/by-id "siren-container")
                    (str "<div id=\"" id "\" class=\"" class "\">Siren!</div>"))
    (let [element (-> (domina/by-id id)
                      (domina/set-styles! style))]
      (ef/at element (em/fade-in 150))
      (set-siren-content element args)
      element)))
                  
(defn remove-siren!
  "Apply a succession of tranformations to remove the siren"
  [element]
  (ef/at element
         (em/chain (em/fade-out 800)
                   (em/resize :curwidth 0 300 #(domina/destroy! element)))))


;; Weird move behavior while resizing is caused by a bug in enfocus.
;; Hopefully this will get fixed soon.
(defn remove-all-sirens!
  "Remove every siren on the screen, even the sticky ones."
  []
  (map #(remove-siren! %) (ef/nodes->coll (domina/by-class "siren-box"))))

(defn sticky-siren!
  "Creates a siren box without any timer. Will stay there until
  removed."
  [content-or-options]
  (if (string? content-or-options)
    (create-siren-box {:content content-or-options})
    (create-siren-box content-or-options)))

(defn siren!
  "Create a siren box that will disappear in a given time"
  [content-or-options]
  (let [siren (sticky-siren! content-or-options)]
    (ef/setTimeout #(remove-siren! siren) (or delay (or (:delay content-or-options) 2000)))
    siren))
