# asciibird

A platformer for the Unix terminal, inspired by Flappy Bird.

![asciibird asciicast gif](https://github.com/jeffreyolchovy/asciibird/raw/master/asciicast.gif)

## Gameplay instructions

Use your arrow keys to avoid obstacles. Colliding with any obstacles, including the boundaries of the game, will terminate the current game.

Navigation with `wasd` and `hjkl` is also supported.

## Installation instructions

    $ git clone git@github.com:jeffreyolchovy/asciibird.git
    $ cd asciibird
    $ make

After building, issue the following command to start a new game:

```
$ bin/asciibird
```

## Static preview of gameplay

    ----------------------------
       ::::      ::::  ::::     
       ::::      ::::  ====     
       ::::      ::::           
       ::::      ::::           
       ::::      ::::           
       ====      ::::  ====     
                 ::::  ::::     
                 ::::  ::::     
       ====      ====  ::::     
       ::::            ::::     
       ::::   @>       ::::     
       ::::      ====  ::::     
    ----------------------------
    Score: 237
