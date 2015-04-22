(ns skat)
(set! *warn-on-reflection* true)

;; Used as a Card representation in whole project.
;;
;; :color property should belong to #{ :kreuz, :grun, :herz, :schell }
;; :figure property should belong to #{ :7, :8, :9, :10, :W, :D, :K, :A }
(defrecord Card [color figure])

;; Used to store bidding order for auctions between deals.
;;
;; :front (ger. Vorhand) "sits" on left from the dealer,
;; :middle (ger. Mittelhand) "sits" on right from the dealer,
;; :rear (ger. Hinterhand) is the "dealer" hinself.
;;
;; (Since dealing is done by the engine no we have no actual dealer on the
;; implementation level and the only thing that matters is order of bidding and
;; card playing during first trick.)
;;
;; Bidding is done in order:
;; * :middle player bids :front player till one of them pass,
;; * winner of previous subbid is bid by :rear player.
;;
;; When deal ends bidders are rotated before next auction begns (consequence of
;; rotating the "dealer's position"):
;;   :front -> :rear -> :middle -> :front -> ...
(defrecord Bidders [front middle rear])

;; Used to store auction results (and sub results).
;;
;; :winner is the (sub)bid's winner (during the game he'll be called solist),
;; :cards are winner's cards,
;; :bid is the value of bid placed by solist.
(defrecord Bidding [winner cards bid])

;; Used to store details about declared game.
;;
;; :solist is the bid winner and declarer of a current game,
;; :suit describes currently played game:
;;   #{ :grand, :kreuz, :grun, :herz, :schell, :null },
;; :hand tells whether solists decided not to pick skat cards,
;; :ouvert tells whether solists decided to show his cards to his opponents
;;   (requires :hand),
;; :announced-schneider? tells whether solist declares that opponenets will get
;;    less that 30 points worth of cards,
;; :announced-schwarz? tells that solist declare to take all cards,
;; :declared-bid tells minimal game value solist must achieve.
(defrecord Configuration [solist
                          suit
                          hand?
                          ouvert?
                          announced-schneider?
                          announced-schwarz?
                          declared-bid])

;; Used to describe current state of deal.
;;
;; :knowledge is a map of [Player, PlayerKnowledge] pairs,
;; :trick is a current Trick,
;; :skat is a pair of 2 cards owned by solist but not used during the game.
(defrecord Deal [knowledge trick skat])

;; Used to describe deal result.
;;
;; :solist is a bid winner and game declarer,
;; :success? tells whether solist won game,
;; :bid tells how much player bid,
;; :game-value tells how much game was worth.
(defrecord Result [solist success? bid game-value])

;; Used to describe Player's knowledge about the ongoing deal.
;;
;; :self is a Player himself,
;; :cards-played is map of [Player, Vector of Cards] pairs storing knowledge
;;   about past tricks,
;; :cards-owned is map of [Player, Vector of Cards] pairs storing knowledge
;;   about owned cards,
;; :cards-taken is map of [Player, Vector of Cards] pairs storing knowledge
;;   about cards taken by each player.
(defrecord PlayerKnowledge [self cards-played cards-owned cards-taken])

(defprotocol Player
  "Abstract entity choosing from Player-specific options"
  (id [this] "Returns Player's ID (name)")
  (play-1st-card [this situation] "Play card as 1st Player")
  (play-2nd-card [this situation c1] "Play card as 2nd Player")
  (play-3rd-card [this situation c1 c2] "Play card as 3rd Player")
  (place-bid [this cards last-bid] "Place bid considering prevous bid's value")
  (respond-to-bid [this cards bid] "Respond to placed bid (accept/decline)")
  (declare-suit [this cards final-bid] "Choose suit as a solist")
  (declare-hand [this cards final-bid] "Choose hand (yes/no) as a solist")
  (declare-schneider [this cards final-bid]
    "Choose schneider (yes/no) as a solist")
  (declare-schwarz [this cards final-bid] "Choose schwarz (yes/no) as a solist")
  (declare-ouvert [this cards final-bid] "Choose ouvert (yes/no) as a solist")
  (skat-swapping [this config skat-owned cards-owned skat-card]
    "Choose card for swapping with a skat card"))

;; Used to descibe Player's situation in current trick.
;;
;; :self is a Player himself,
;; :config is currently used game Configuration,
;; :knowledge is current PlayerKnowledge,
;; :order is order of cards played in this Trick,
;; :cards-allowed are cards allowed to play in this trick according to played
;;   game and first played card (if already played).
(defrecord PlayerSituation [self config knowledge order cards-allowed])

;; Used to describe current Trick.
;;
;; :order is map with keys #{ :p1, :p2, :p3 } describing orders of card played
;;   during the trick.
(defrecord Trick [order])

(defprotocol GameDriver
  "Abstract entity choosing not-Player-specific options and receiving results"
  (create-players [this] "Creates Players as Bidders")
  (auction-result [this bidding] "Reports auction result")
  (declare-game [this bidding] "Creates Configuration basing on Bidding")
  (declaration-result [this config] "Reports game declaration's result")
  (trick-results [this results] "Reports trick's result")
  (deal-results [this results] "Reports deal's result")
  (game-results [this points] "Reports whole game's result"))
