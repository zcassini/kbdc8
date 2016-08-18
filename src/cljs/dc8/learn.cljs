(ns dc8.learn
  (:require [cljs.core.async :refer [>! <! alts! chan timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def ch (chan))

(go
  (println [:a] "Gonna take a nap")
 (<! (timeout 8000))
  (println [:a] "I slept one second, trying to put a value on channel")
  (>! ch 42)
  (println [:a] "I'm done!"))

(go
  (println [:b] "Gonna try taking from channel")
  (let [cancel (timeout 3000)
        [value ch] (alts! [ch cancel])]
    (if (= ch cancel)
      (println [:b] "Too slow, take from channel cancelled")
      (println [:b] "Got" value))))
