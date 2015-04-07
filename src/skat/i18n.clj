(ns skat.i18n
  (:require [taoensso.tower :as tower :refer [with-tscope]]))
(set! *warn-on-reflection* true)

(defn long-str "Build ling strings" [& strings]
  (clojure.string/join "\n" strings))

(def skat-i18n-config "I18n configuration for Tower library"
  { :dictionary
    { :en
      { :skat
        { :cli
          { :answer { :yes "yes"
                      :no  "no" }
            :cards { :allowed! "Allowed cards:\n%s"
                     :owned!   "Owned cards:\n%s" }
            :figure { :r7  " 7"
                      :r8  " 8"
                      :r9  " 9"
                      :r10 "10"
                      :W   " W"
                      :D   " D"
                      :K   " K"
                      :A   " A" }
            :player { :cpu-type         "CPU player"
                      :human-type       "Human player"
                      :name             "[Player %s]"
                      :make-bid         "Place bid (more than %d) or pass (17):"
                      :answer-bid!      "You have been bid: %d\nDo you accept?"
                      :choose-suit      "Choose suit:"
                      :choose-hand      "Choose hand:"
                      :choose-schneider "Announce schneider:"
                      :choose-schwarz   "Announce schwarz:"
                      :choose-ouvert    "Choose ouvert:"
                      :swap-skat-card   "Choose card to swap for %s:"
                      :played           "%s played: %s"
                      :won-bid          "%s won bid: %d"
                      :bid-draw         "No one won bid"
                      :declared! (long-str "Solist: %s"
                                           "Suit:   %s"
                                           "Hand:   %s"
                                           "Ouvert: %s"
                                           "Announced schneider: %s"
                                           "Announced schwarz:   %s"
                                           "Placed bid: %d") }
            :results { :deal! "Solist: %s\nBid:    %d\nWon:    %s"
                       :game! "Player: %s\nPoints: %d" }
            :select { :nth-item    "Select which one you want:"
                      :player-name "Select player's name:" }
            :suit { :grand  "Grand",
                    :kreuz  "Kreuz"
                    :grun   "Grün"
                    :herz   "Herz"
                    :schell "Schell"
                    :null   "Null" } } }
        :missing  "|Missing translation: [%1$s %2$s %3$s]|" }
      :pl
      { :skat
        { :cli
          { :answer { :yes "tak"
                      :no  "nie" }
            :cards { :allowed! "Dozwolone karty:\n%s"
                     :owned!   "Posiadane karty:\n%s" }
            :figure { :r7  " 7"
                      :r8  " 8"
                      :r9  " 9"
                      :r10 "10"
                      :W   " W"
                      :D   " D"
                      :K   " K"
                      :A   " A" }
            :player { :cpu-type         "Grach CPU"
                      :human-type       "Gracz ludzki"
                      :name             "[Gracz %s]"
                      :make-bid         "Licytuj (powyżej %d) lub spasuj (17):"
                      :answer-bid!      "Otrzymany zakład: %d\nPrzyjmujesz?"
                      :choose-suit      "Wybierz grę:"
                      :choose-hand      "Czy gra z ręki?:"
                      :choose-schneider "Zapowiadasz schneider?:"
                      :choose-schwarz   "Zapowiedasz schwarz?:"
                      :choose-ouvert    "Gra otwarta?:"
                      :swap-skat-card   "Wybierz kartę to wymiany za %s:"
                      :played           "%s zagrał: %s"
                      :won-bid          "%s wygrał licytację: %d"
                      :bid-draw         "Nikt nie wygrał licytacji"
                      :declared! (long-str "Solista: %s"
                                           "Gra:     %s"
                                           "Z ręki:  %s"
                                           "Otwarta: %s"
                                           "Zapowiedziany schneider: %s"
                                           "Zapowiedziany schwarz:   %s"
                                           "Wylicytowano: %d") }
            :results { :deal! "Solista: %s\nZakład:  %d\nWygrana: %s"
                       :game! "Gracz:  %s\nPunkty: %d" }
            :select { :nth-item    "Wybierz opcję:"
                      :player-name "Wybierz imię gracza:" }
            :suit { :grand  "Grand",
                    :kreuz  "Kreuz"
                    :grun   "Grün"
                    :herz   "Herz"
                    :schell "Schell"
                    :null   "Null" } } }
        :missing  "|Brak tłumaczenia: [%1$s %2$s %3$s]|" } }
   :dev-mode? false
   :fallback-locale :en })

(def t "Translation function" (tower/make-t skat-i18n-config))

(def ^:dynamic *lang* "Language" :en)
