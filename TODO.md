Known issues
===

 * `gameplay.clj` module lacks tests,
 * when all players bid 17, auction should be repeated,
 * with *Null* game declared player should not be asked about *hand*, *ouvert*
   and *schneider*/*schwarz* declarations,
 * with *schneider* not declared *schwarz* should not be asked about,
 * trick winner is not always properly recognized,
 * in *ouvert* game cards are not displayed to opponents and `PlayerKnowledge`
   is not initialized with information about solist's cards,
 * deal's result is not always correctly recognized,
 * there is no option to skip *skat* swapping and no previous preview of *skat*,
 * AI player is not implemented.
