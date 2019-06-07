A D - rotate ship 
W S - throttle level controls
Space - push-to-max throttle (better use this)

LMB - fire cannon shell
RMB - launch self-guided missiles (position mouse cursor on top to fire at desired target)

// ---------------------------------------------------------------------------------------


Ship hull will destroyed in 3 shell hit, but slowly auto-repairing
so 4 needed to be sure.

Missile kill ship in one hit.

Minigun is auto-firing at close target (act as close-in weapon system (point-defence))
Most time bringing targets down but may miss on simultaneous incoming missiles from different directions).

Green curve - you trajectory in planet gravity force.
Blue curve - you shell trajectory in planet gravity force.

Force shield can repulse several (~4) hit from shell. It slowly regenerating to full charge.

// -------------------------------------------------------------------------------------------------

Objectives: survive till warp engine have been repaired(~5 min)



Difficulty levels

Choose one that fit you skill in

ru.geekbrains.screen.GameScreen.show() {
...
        // NOVICE
        ENEMY_RESPAWN_TIME = 500;
        ENEMIES_COUNT_IN_WAVE = 1;
...
}

Sound effects are disabled due to libgdx  random stattering (~20 ms) when playing sound.

Code need refactoring, etc., but better if it was written in C ++ to avoid using buffer objects for Vector2,
GC related things, so i give up on it.








