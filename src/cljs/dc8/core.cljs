(ns dc8.core
  (:require-macros [cljs.core.async.macros :refer [ go-loop]])
  (:require
   [reagent.core :as r]
   [cljs.core.async :refer [<! chan onto-chan timeout]]
   ))

(def challenge-string "This is the first sentence. This is another sentence. And we have one more.")
(def string-with-returns (clojure.string/replace challenge-string #"\.\s" ".\n"))
(def split-string (clojure.string/split string-with-returns #""))
(def delay-time 1000)
(def letter-chan (chan))

(defonce app-state (r/atom {:text ""
                            :processing?  false
                            :processed? false}))

(defn update-text! [f & args]
  (apply swap! app-state update :text f args))

(defn add-letter! [l]
  (update-text! str l))

(defn clear-text! []
  (swap! app-state assoc  :text ""))

(defn type-writer [c]
  (go-loop [received (<! c)]
    (if (nil? received)
      (swap! app-state assoc  :processed?  true)
      (do
        (add-letter! received)
        (<! (timeout delay-time))
        (recur (<! c))))))

(defn header []
  [:h1 "Welcome to the Kindred Bay Daily Challenge 8"])

(defn go-button-click  []
  (swap! app-state assoc :processing? true)
  (let [c (chan)]
    (type-writer c)
    (onto-chan c split-string)
    ))

(defn go-button []
  [:button {
            :on-click go-button-click
           :disabled (:processing? @app-state) }
   "Go"])

(defn clear-button-click []
  (clear-text!)
  (swap! app-state assoc :processed? false)
  (swap! app-state assoc  :processing?  false))

(defn clear-button []
  [:button {
            :disabled (not (:processed? @app-state))
            :on-click clear-button-click}
   "Clear"])

(defn page []
  [:div {:style {:white-space "pre-wrap"}}
   (header)
   [:p (str (:text @app-state))]
   [:br]
   (go-button)
   (clear-button)
   ])

(defn reload []
  (r/render [page] (.getElementById js/document "app")))

(defn ^:export main []
  (reload))

