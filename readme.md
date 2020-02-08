# StarGame - gravity game, CIWS simulator

![alt text](https://i.ibb.co/mqDpWS4/StarGame.png)

download release from  
https://github.com/dreamworkerln/StarGame/releases/tag/1.0

unzip
release.zip 

run (required java 1.8)  
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
EMP missile bring you shield down.  

Mini-gun is auto-firing at closest target (act as close-in weapon system (CIWS, point-defence))  
Most time it bringing targets down but may miss on simultaneous incoming missiles from different directions.  
(Or when attacked by missile launched from close enemy ship)  
Doesn’t shoot at your missiles, but can unintentionally shoot them down.  
(So launch missiles safely)  
  
Anti-missile system also help against inbound missiles on long range.  
Doesn’t shoot at your missiles, but can unintentionally shoot them down.  
  
AA cannon fire fragmentation shell on inbound missiles and fragmentation plasma shell on enemy ships.  
(Experimental F - stop firing at incoming kinetic kill vehicle, G - resume)  
  
Force shield can repulse many (~7) tangent hit, very many chase hit or ~2-3 direct hit  
(fired from enemy ship flying towards you) by shell.  
It's slowly regenerating to full charge, changing self colour.
Also it repulse missiles (~1) but ineffective on counter course inbound.  
  
Green curve - you trajectory in planet gravity force.  
Blue curve - you shell trajectory in planet gravity force.  

// -------------------------------------------------------------------------------------------------


Difficulty levels  

Set one that fit you skill in config.ini (app.rank=N)

1 - NEVER PLAYED (try this first, to learn orbital maneuvering if not played KSP before)  
2 - NOVICE  
3 - EXPERIENCED  
4 - SPECIALIST  
5 - IMPERIAL NAVY ENSIGN  
6 - IMPERIAL NAVY LIEUTENANT  
7 - IMPERIAL NAVY LORD-LIEUTENANT  
8 - IMPERIAL NAVY COMMANDER  
9 - IMPERIAL NAVY CAPITAN  


Code need refactoring, etc., but better if it was written in C++ to avoid using buffer objects for Vector2, 
GC related things, so i give up to do this.   

Old demo: https://www.youtube.com/watch?v=yvjAXCUgGY8  
New demo: https://youtu.be/QloDxACsnNY  
