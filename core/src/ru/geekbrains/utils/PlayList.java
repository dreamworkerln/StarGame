package ru.geekbrains.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ru.geekbrains.screen.GameScreen;

public class PlayList {

    List<SoundPlay> list = new ArrayList<>();
    Map<SoundPlay, Long> map = new HashMap<>();

    private Optional<SoundPlay> current = Optional.empty();



    private long playListFinishTick = -1;

    public void add(SoundPlay soundPlay) {


        if (!map.containsKey(soundPlay)) {

            long tick = GameScreen.INSTANCE.getTick();
            list.add(soundPlay);
            map.put(soundPlay, tick + soundPlay.durationTick + 20);
        }
    }

    public boolean contains(SoundPlay soundPlay) {
        return map.containsKey(soundPlay) || current.orElse(null) == soundPlay;
    }

    public void update(float dt) {

        // sound messages queue handling

        long tick = GameScreen.INSTANCE.getTick();
        map.entrySet().removeIf(e -> e.getValue() < tick);
        
        if(list.size() > 0 && tick > playListFinishTick) {

            current = Optional.of(list.remove(0));
            current.get().sound.play();
            current.ifPresent(sp -> playListFinishTick = tick + sp.durationTick);
        }
    }
    
    public void clear() {

        current.ifPresent(sp -> sp.sound.stop());
        list.clear();
        map.clear();
        playListFinishTick = -1;
    }
}
