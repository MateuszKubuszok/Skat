(ns skat.i18n
  (:require [clojure.string :refer [join]]
            [taoensso.tower :as tower :refer (with-tscope)]))
(set! *warn-on-reflection* true)

(def skat-i18n-config "I18n configuration for Tower library"
  { :dictionary
    { :en
      { :skat
        { :cli
          { :answer-yes         "yes"
            :answer-no          "no"
            :card-allowed       "Allowed cards:\n%s"
            :card-owned         "Owned cards:\n%s"
            :player-make-bid    "Place bid (more than %d) or pass (17):"
            :player-answer-bid  "You have been bid: %d\nDo you accept?"
            :player-played      "%s played: %s"
            :select-nth-item    "Select which one you want:"
            :select-player-name "Select player's name:" } }
        :missing  "|Missing translation: [%1$s %2$s %3$s]|" } }
   :dev-mode? false
   :fallback-locale :en })

(def t "Translation function" (tower/make-t skat-i18n-config))

(def ^:dynamic *lang* "Language, English by default" :en)
