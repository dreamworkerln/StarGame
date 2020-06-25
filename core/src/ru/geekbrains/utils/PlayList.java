package ru.geekbrains.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.geekbrains.screen.GameScreen;

public class PlayList {

    List<SoundPlay> list = new ArrayList<>();
    Map<SoundPlay, Long> map = new HashMap<>();

    SoundPlay current = null;



    long playListFinishTick = -1;

    public void add(SoundPlay soundPlay) {


        if (!map.containsKey(soundPlay)) {

            long tick = GameScreen.INSTANCE.getTick();
            list.add(soundPlay);
            map.put(soundPlay, tick + soundPlay.durationTick + 20);
        }
    }

    public void update(float dt) {

        // sound messages queue handling

        long tick = GameScreen.INSTANCE.getTick();
        map.entrySet().removeIf(e -> e.getValue() < tick);
        
        if(list.size() > 0 && tick > playListFinishTick) {

            current = list.remove(0);
            current.sound.play();
            playListFinishTick = tick + current.durationTick;
        }


    }
    
    public void clear() {

        if(current != null) {
            current.sound.stop();
        }
//        for (SoundPlay sp : list) {
//            sp.sound.stop();
//        }
        
        list.clear();
        map.clear();
        playListFinishTick = -1;
    }
}
