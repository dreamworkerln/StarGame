run  
cd StarGame/desktop/build/libs/  
java -jar desktop-1.0.jar  



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

Force shield can repulse several (~5) hit from shell. It's slowly regenerating to full charge.

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
GC related things, so i give up on it.








