package ru.geekbrains.entities.equipment.interfaces;

import ru.geekbrains.entities.projectile.Ammo;

public interface WeaponSystem {

    void startFire();
    void stopFire();

    Ammo getFiringAmmo();

    Class<? extends Ammo> getCurrentAmmoType();
    void setCurrentAmmoType(Class<? extends Ammo> ammo);

    float getFireRate();
    void setFireRate(float rate);

    float getCalibre();
    void setCalibre(float calibre);

    //Vector2 getDir();
    //void setDir(Vector2 dir);

    void rotate(float dt);
}
