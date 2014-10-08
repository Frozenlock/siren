(ns siren.core
  (:require [domina :as domina]
            [domina.events :as events]
            [domina.css :as css]
            [enfocus.core :as ef]
            [enfocus.effects :as eff]
            [enfocus.events :as efe]))

;; ====================== We need a little 'X' close button =====================

(declare remove-siren!)

(defn add-close-button! [element]
  (let [close-button (domina/single-node (domina/html-to-dom
                                          "<div class=\"siren-close-button\"><b>✕</b></div>"))]
    (domina/append! element close-button)
    (domina/set-styles! close-button
                        {:position "absolute" :top "-10px" :left "-10px" :border-radius "20px"
                         :background "inherit" :padding "3px" :width "20px" :height "20px" :opacity "0"
                         :line-height "20px" :text-align "center" :cursor "pointer"})
    (events/listen! close-button :click #(remove-siren! element))
    (ef/at element (efe/listen :mouseenter #(ef/at close-button (eff/fade-in 150))))
    (ef/at element(efe/listen :mouseleave #(ef/at close-button (eff/fade-out 150))))
    element))

;;============================ Main Siren functions =============================

(def base-style ;; now a standalone var: the users can use it directly.
  {:font-size "15px" :margin "10px" :opacity "0" :width "300px" :position "relative"
   :padding "10px" :border-radius "10px" :box-shadow "5px 5px 10px black"})


(defn- create-siren-container!
  "Create a new siren container on the DOM and return it."[]
  (let [id "siren-container"]
    (-> (domina/single-node (str "<div id=\"" id "\"></div>"))
        (domina/set-styles! {:position "fixed" :top "10px" :right "10px"})
        ((fn [e] (domina/append! (domina.css/sel "body") e) e)))))


(defn- select-style
  ":dark, :light, or :css (if you intend to use CSS to modify the siren appearance)."
  [{:keys [style]}]
  (let [dark (merge base-style {:background "rgba(0, 0, 0, 0.7)" :color "white"})
        light (merge base-style {:background "rgba(255, 255, 255, 0.7)" :color "black"})
        css {}]
    (get {:dark dark :light light :css css} style (or style dark))))


(defn- set-siren-content!
  "Set the siren content and add a close button (which is part of the
  content)"[element content]
  (-> element
      domina/destroy-children!
      (domina/append! content)
      add-close-button!))


(defn- create-siren-box! [content-or-options]
  (let [siren (-> (domina/single-node "<div class=\"siren-box\">Siren!</div>")
                  (domina/set-styles! (select-style content-or-options))
                  (set-siren-content! (:content content-or-options)))]
    (-> (or (domina/by-id "siren-container") (create-siren-container!))
        (domina/append! siren))
    siren))
  
(defn- create-and-show-siren-box! [content-or-options]
  (let [element (create-siren-box! content-or-options)]
    (ef/at element (eff/fade-in 150))
    element))

(defn- add-timeout! [element content-or-options]
  (let [delay (or (:delay content-or-options) 2000)]
    (ef/setTimeout #(remove-siren! element) delay)
    (domina/set-data! element :timeout delay)
    element))

(defn- exists?
  "Check if an element is still around"
  [element]
  (if (.-parentNode element) true))

(defn remove-siren!
  "Apply a succession of tranformations to remove the siren"
  [element]
  (ef/at element
         (eff/chain (eff/fade-out 500)
                   (eff/resize :curwidth 0 300 #(domina/destroy! element) #(* % % % %)))))
;; the acceleration is used to hide any margin that can't be removed by a resize

(defn replace-siren!
  "Replace an existing siren box by keeping the same position. Return
  the new siren. The new siren will have the same initial timeout
  before disappearing (unless it's a sticky one). If you want to avoid
  surprise instantaneous resizing, you should specify a size in the
  style." [old-siren content-or-options]
  (let [smap (if (map? content-or-options) content-or-options
                            {:content content-or-options})
        new-siren (create-siren-box! smap)
        timeout (domina/get-data old-siren :timeout)]
    (domina/insert-after! old-siren new-siren)
    (domina/destroy! old-siren)
    (domina/set-styles! new-siren {:opacity "1"})
    (when timeout (add-timeout! new-siren {:delay timeout}))
    new-siren))


(defn remove-all-sirens!
  "Remove every siren on the screen, even the sticky ones."
  []
  (map #(remove-siren! %) (ef/nodes->coll (domina/by-class "siren-box"))))

(defn sticky-siren!
  "Creates a siren box without any timer. Will stay there until
  removed."
  [content-or-options]
  (let [smap (if (map? content-or-options) content-or-options
                 {:content content-or-options})]
    (create-and-show-siren-box! smap)))

(defn siren!
  "Create a siren box that will disappear in a given time. To override
  the default time, use the {:delay time-in-ms} form."
  [content-or-options]
  (let [smap (if (map? content-or-options) content-or-options
                 {:content content-or-options})]
    (-> (sticky-siren! smap)
        (add-timeout! smap))))

(defn continuous-siren-factory
  "Return a function that will always act on the same siren if it
  exists. Otherwise, it will create a new one"[]
  (let [siren-element (atom [""])]
    (fn [content-or-options]
      (if (exists? @siren-element)
        (swap! siren-element #(replace-siren! % content-or-options))
        (swap! siren-element #(siren! content-or-options))))))

