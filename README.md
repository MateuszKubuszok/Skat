# Skat game engine

[![Build Status](https://travis-ci.org/MateuszKubuszok/Skat.png)](https://travis-ci.org/MateuszKubuszok/Skat)

Engine intented to be able to completely simulate
[Skat card game](http://en.wikipedia.org/wiki/Skat_(card_game)),
as well as act in a bot-like manner.

Compiled program uses CLI interface as to demonstrate library's abilities.

## Notes

Game implements official rules without any local additions (no *Ramsch* for
instance). It doesn't implement idea of giving deal up early or ability to
counter bidding as well.

Naming in the game is in most part derrived from English Wikipedia. However,
there are some local names used. Cards suites use upper-silesian names:

  * **Kreuz** - for *Clubs*/*Acorns*/*Trefl*,
  * **Grün** - for *Spades*/*Leaves*/*Pik*,
  * **Herz** - for *Hearts*/*Hearts*/*Caro*,
  * **Schell** - for *Diamonds*/*Bells*/*Kier*

Similarly *Jack* is denoted with **W** and *Queen* with **D**. It can be changed
using i18n settings.

## Installation

Download source from [GitHub](https://github.com/MateuszKubuszok/Skat)
and build using instructions below.

## Usage

Building requires Leiningen installed.

To only build standalone jar run:

    $ lein uberjar

It can be run with:

    $ java -jar skat-0.3.0-standalone.jar

Alternatively build and run program at once:

    $ lein run

On Windows (Cygwin) it might be required to run

    cmd /c chcp 65001

before to make sure that UTF-8 characters will be displayed correctly.

On start you can pass option to configure apps behavior:

    $ lein run -- --help
      -l, --lang LANG  :en  Interface language
      -d, --debug           Debug mode on
      -h, --help            Display help

Currently available translations are: `en` (English) and `pl` (Polish).

## License

Copyright © 2014-2015 Mateusz Kubuszok

Distributed under the Eclipse Public License version 1.0.
