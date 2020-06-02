package ru.geekbrains.entities.equipment.interfaces;

import java.util.List;
import java.util.Map;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.projectile.missile.AntiMissile;

public interface AntiLauncherSystem extends LauncherSystem {

    Map<GameObject, List<AntiMissile>> getTargetMissile();

}

