package ru.geekbrains.entities.equipment.interfaces;

import ru.geekbrains.entities.objects.GameObject;

public interface WeaponSystem {

    void startFire();
    void stopFire();

    float getPower();
    GameObject getFiringAmmoType();

    float getFireRate();
    void setFireRate(float rate);

    float getCalibre();
    void setCalibre(float calibre);

    //Vector2 getDir();
    //void setDir(Vector2 dir);

    void rotate();
}
