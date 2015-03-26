# Skat game engine

[![Build Status](https://travis-ci.org/MateuszKubuszok/skat.png)](https://travis-ci.org/MateuszKubuszok/skat)

Engine intented to be able to completely simulate Skat game, as well as act
in a bot-like manner.

Compiled program uses CLI interface as to demonstrate library's abilities.

## Notes

Game implements official rules without any local additions (no *Ramsch* for
instance).

Naming in the game is in most part derrived from English Wikipedia. However
there are some local names used. Cards suites use upper-silesian names:

  * **Kreuz** - for *Clubs*/*Acorns*/*Trefl*,
  * **Grün** - for *Spades*/*Leaves*/*Pik*,
  * **Herz** - for *Hearts*/*Hearts*/*Caro*,
  * **Schell** - for *Diamonds*/*Bells*/*Kier*

Similarly *Jack* is denoted with **W** and *Queen* with **D**. It can be changed
using i18n settings.

### To be done

  * some actual functionality testing (several errors are known),
  * displaying current player name (for multiple human players case),
  * displaying solist's cards for ouvert game,
  * CPU player implementation.

## Installation

Download source from [Bitbucket](https://bitbucket.org/MateuszKubuszok/skat) or
[GitHub](https://github.com/MateuszKubuszok/skat) and build using instructions
below.

## Usage

Building requires Leiningen installed.

To only build standalone jar run:

    $ lein uberjar

It can be run with:

    $ java -jar skat-0.1.0-standalone.jar

Alternatively build and run program at once:

    $ lein run

## License

Copyright © 2014-2015 Mateusz Kubuszok

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
