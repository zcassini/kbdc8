(ns dc8.core
  (:require-macros [cljs.core.async.macros :refer [ go-loop]])
  (:require
   [reagent.core :as r]
   [cljs.core.async :refer [<! chan onto-chan timeout]]
   ))

(def challenge-string "This is the first sentence. This is another sentence. And we have one more.")
(def string-with-returns (clojure.string/replace challenge-string #"\.\s" ".\n"))
(def split-string (clojure.string/split string-with-returns #""))

(def letter-chan (chan))

(defonce app-state (r/atom {:text ""}))

(defn update-text! [f & args]
  (apply swap! app-state update :text f args))

(defn add-letter! [l]
  (update-text! str l))

(defn clear-text! []
  (reset! app-state {:text ""}))

(go-loop [received (<! letter-chan)]
  (if (nil? received)
    (prn "channel closed")
    (do
      (add-letter! received)
      (<! (timeout 1000))
      (recur (<! letter-chan)))))

(defn header []
  [:h1 "Welcome to the Kindred Bay Daily Challenge 8"])

(defn page []
  [:div {:style {:white-space "pre-wrap"}}
   (header)
   [:p (str (:text @app-state))]
   [:br]
   [:button {:on-click #(onto-chan letter-chan split-string false) } "Go"]
   [:button {:on-click clear-text!}"Clear"]
   ])

(defn reload []
  (r/render [page] (.getElementById js/document "app")))

(defn ^:export main []
  (reload))

