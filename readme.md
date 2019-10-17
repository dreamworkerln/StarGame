run
download release from
https://github.com/dreamworkerln/StarGame/releases/tag/1.0
java -jar desktop-1.0.jar



A D - rotate ship ( + SHIFT to slow down rotation)
W S - throttle level controls
SPACE  - push-to-max throttle (better use this)

LMB - shoot cannon shell
RMB - launch self-guided missiles (position mouse cursor on top to fire at desired target)
      (RMB + SHIFT or CTRL launch backwards)


// ---------------------------------------------------------------------------------------


Ship hull will be destroyed in 3 shell hit, but slowly auto-repairing so 4 hits are needed to be sure.

Missile kills ship in one hit.

Mini-gun is auto-firing at closest target (act as close-in weapon system (point-defence))
Most time it bringing targets down but may miss on simultaneous incoming missiles from different directions.
(Or when attacked by missile launched from close enemy ship.)
Doesn’t shoot at your missiles. (So launch missiles safely.)

Anti-missile system also help against inbound missiles on long range.
Doesn’t shoot at your missiles, but can unintentionally shoot them down.

Force shield can repulse several (~6) tangent hit or ~3 direct hit (fired from enemy ship flying towards you) by shell.
It's slowly regenerating to full charge, changing self colour.
Doesn’t repulse you shells so be careful.

Green curve - you trajectory in planet gravity force.
Blue curve - you shell trajectory in planet gravity force.

// -------------------------------------------------------------------------------------------------


Difficulty levels

Set one that fit you skill in StarGame/desktop/build/libs/config.ini

1 - NEVER PLAYED
2 - NOVICE
3 - EXPERIENCED
4 - SPECIALIST
5 - IMPERIAL NAVY LIEUTENANT
6 - IMPERIAL NAVY LORD-CAPITAN

Code need refactoring, etc., but better if it was written in C++ to avoid using buffer objects for Vector2, 
GC related things, so i give up to do this.
