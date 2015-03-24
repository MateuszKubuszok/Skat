(ns skat.i18n
  (:require [clojure.string :refer [join]]
            [taoensso.tower :as tower :refer (with-tscope)]))
(set! *warn-on-reflection* true)

(def skat-i18n-config "I18n configuration for Tower library"
  { :dictionary ; Map or named resource containing map
    { :en { :skat { :cli { :player-played       "%s played: %s"
                           :select-card-allowed "Allowed cards:\n%s"
                           :select-card-owned   "Owned cards:\n%s"
                           :select-nth-item     "Select which one you want:"
                           :select-player-name  "Select player's name:" } }
          :missing  "|Missing translation: [%1$s %2$s %3$s]|" } }
   :dev-mode? true ; Set to true for auto dictionary reloading
   :fallback-locale :en })

(def t "Transplation function" (tower/make-t skat-i18n-config))

(def ^:dynamic *lang* "Language, English by default" :en)
