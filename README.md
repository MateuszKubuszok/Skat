# Skat game engine

[![Build Status](https://travis-ci.org/MateuszKubuszok/skat.png)](https://travis-ci.org/MateuszKubuszok/skat)

Engine intented to be able to completely simulate Skat game, as well as act
in a bot-like manner.

Compiled program uses CLI interface as to demonstrate library's abilities.

## Notes

Game implements official rules without any local additions (no *Ramsch* for
instance). However it doesn't implement idea of giving deal up early.

Naming in the game is in most part derrived from English Wikipedia. However
there are some local names used. Cards suites use upper-silesian names:

  * **Kreuz** - for *Clubs*/*Acorns*/*Trefl*,
  * **Grün** - for *Spades*/*Leaves*/*Pik*,
  * **Herz** - for *Hearts*/*Hearts*/*Caro*,
  * **Schell** - for *Diamonds*/*Bells*/*Kier*

Similarly *Jack* is denoted with **W** and *Queen* with **D**. It can be changed
using i18n settings.

### To do and known issues

[See TODO list](TODO.md).

## Installation

Download source from [BitBucket](https://bitbucket.org/MateuszKubuszok/skat) or
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

On Windows (Cygwin) it might be required to run

   cmd /c chcp 65001

before to make sure that UTF-8 characters will be displayed correctly.

## License

Copyright © 2014-2015 Mateusz Kubuszok

Distributed under the Eclipse Public License version 1.0.
