package ru.geekbrains.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ru.geekbrains.screen.GameScreen;

public class PlayList {

    private LinkedHashSet<SoundPlay> list = new LinkedHashSet<>();
    private Map<SoundPlay.SoundType, Set<SoundPlay>> index = new HashMap<>(); // index to locate Soundtype in list

    private SoundPlay current = null;
    private long finishTick = -1; // tick on player will stop play

    public void add(SoundPlay soundPlay) {

        if (this.contains(soundPlay.type)) {
            return;
        }

        //long tick = GameScreen.INSTANCE.getTick();
        list.add(soundPlay);

        Set<SoundPlay> value = index.computeIfAbsent(soundPlay.type,
            type -> new HashSet<>());
        value.add(soundPlay);
        //map.put(soundPlay, tick + soundPlay.durationTick + 20);

    }

    /** Contains enqueued or now playing SoundPlay
     *
     * @return
     */
    public boolean contains(SoundPlay.SoundType type) {

        return index.containsKey(type);
    }
    

    public void update(float dt) {

        // sound messages queue handling

        long now = GameScreen.INSTANCE.getTick();



        if(list.size() > 0 && finishTick == -1) {

            current = list.iterator().next();
            current.sound.play();
            finishTick = now + current.durationTick;
        }

        // check that sound was played off
        if(finishTick != -1 && finishTick < now) {

            finishTick = -1;
            //remove from list
            list.remove(current);

            // remove from index[type].Set
            if(index.containsKey(current.type)) {
                Set<SoundPlay> value = index.get(current.type);
                value.remove(current);

                // remove whole index[type]
                if (value.size() == 0) {
                    index.remove(current.type);
                }
            }
        }
    }

    public void clear() {

        if (current != null) {
            current.sound.stop();
        }

        list.clear();
        index.clear();
        finishTick = -1;
    }
}
