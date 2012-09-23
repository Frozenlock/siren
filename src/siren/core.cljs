(ns siren.core
  (:require [domina :as domina]
            [domina.events :as events]
            [domina.css :as css]
            [enfocus.core :as ef]
            [clojure.string :as string])
  (:require-macros [enfocus.macros :as em]))


;; ====================== We need a little 'X' close button =====================

(declare remove-siren!)

(defn add-close-button! [element]
  (let [id (name (gensym "siren-close"))]
    (domina/append! element (str "<div class=\"siren-close-button\" id=\""id"\"><b>X</b></div>"))
    (let [close-button (domina/by-id id)]
      (domina/set-styles! close-button
                          {:position "absolute" :top "-10px" :left "-10px" :border-radius "20px"
                           :background "inherit" :padding "3px" :width "20px" :height "20px" :opacity "0"
                           :line-height "20px" :text-align "center" :cursor "pointer"})
      (events/listen! close-button :click #(remove-siren! element))
      (ef/at element (em/listen :mouseenter #(ef/at close-button (em/fade-in 150))))
      (ef/at element(em/listen :mouseleave #(ef/at close-button (em/fade-out 150)))))))

;;============================ Main Siren functions =============================

(defn- create-siren-container! []
  (let [id "siren-container"]
    (domina/append! (domina.css/sel "body") (str "<div id=\"" id "\"></div>"))
    (-> (domina/by-id id)
        (domina/set-styles! {:position "fixed" :top "10px" :right "10px"}))))


(defn- select-style [&[{:keys [style]}]]
  ":dark, :light, or :custom (if you intend to use CSS to modify the siren appearance)."
  (let [base-style {:font-size "15px" :margin "10px" :opacity "0" :width "300px" :position "relative"
                    :padding "10px" :border-radius "10px" :box-shadow "5px 5px 10px black"}
        dark (merge base-style {:background "rgba(0, 0, 0, 0.7)" :color "white"})
        light (merge base-style {:background "rgba(255, 255, 255, 0.7)" :color "black"})
        css {}]
    (get {:dark dark :light light :css css} style (or style dark))))


(defn- set-siren-content!
  ;; Now private. User should use 'replace-siren!' instead.
  "Set the siren content and add a close button (which is part of the
  content)"[element &[{:keys [content html-content]}]]
  (ef/at element (if html-content
                   (em/html-content html-content)
                   (em/content content)))
  (add-close-button! element)
  element)


(defn- create-siren-box! [&[content-or-options]]
  (let [args (if (string? content-or-options) {:content content-or-options}
                 content-or-options)
        class "siren-box"
        id (name (gensym "siren"))
        style (select-style args)]
    (when-not (domina/by-id "siren-container")
      (create-siren-container!))
    (domina/append! (domina/by-id "siren-container")
                    (str "<div id=\"" id "\" class=\"" class "\">Siren!</div>"))
    (let [element (domina/by-id id)]
      (domina/set-styles! element style)
      (set-siren-content! element args)
      element)))
  
(defn- create-and-show-siren-box! [content-or-options]
  (let [element (create-siren-box! content-or-options)]
    (ef/at element (em/fade-in 150))
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
         (em/chain (em/fade-out 500)
                   (em/resize :curwidth 0 300 #(domina/destroy! element) #(* % % % %)))))
;; the acceleration is used to hide any margin that can't be removed by a resize

(defn replace-siren!
  "Replace an existing siren box by keeping the same position. Return
  the new siren. The new siren will have the same initial timeout
  before disappearing (unless it's a sticky one). If you want to avoid
  surprise instantaneous resizing, you should specify a size in the
  style." [old-siren &[content-or-options]]
  (let [new-siren (create-siren-box! content-or-options)
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
  (create-and-show-siren-box! content-or-options))

(defn siren!
  "Create a siren box that will disappear in a given time. To override
  the default time, use the {:delay time-in-ms} form."
  [content-or-options]
  (-> (sticky-siren! content-or-options)
      (add-timeout! content-or-options)))

(defn continuous-siren-factory
  "Return a function that will always act on the same siren if it
  exists. Otherwise, it will create a new one"[]
  (let [siren-element (atom [""])]
    (fn [content-or-options]
      (if (exists? (first @siren-element))
        (swap! siren-element #(vector (replace-siren! % content-or-options)))
        (swap! siren-element #(vector (siren! content-or-options)))))))

