package ru.dreamworkerln.stargame.entities.equipment.interfaces;

import java.util.List;
import java.util.Map;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.projectile.missile.AntiMissile;

public interface AntiLauncherSystem extends LauncherSystem {

    Map<GameObject, List<AntiMissile>> getTargetMissile();

}

