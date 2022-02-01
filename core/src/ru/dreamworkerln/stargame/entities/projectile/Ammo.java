package ru.dreamworkerln.stargame.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.util.Set;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectSide;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;

public interface Ammo {


    void applyForce(Vector2 f);
    void setTarget(GameObject target);






    GameObject getOwner();
    void setOwner(GameObject owner);

    float getFirePower();
    void setFirePower(float mass);

    float getMass();
    void setMass(float mass);

    float getRadius();
    void setRadius(float radius);

    float getHealth();
    void setHealth(float health);

    void setMaxHealth(float maxHealth);
    float getMaxHealth();

    float getHealthGeneration();
    void setHealthGeneration(float healthGeneration);

    float getHealthRegenerationCoefficient();
    void setHealthRegenerationCoefficient(float healthRegenerationCoefficient);

    long getTTL();
    void setTTL(long TTL);

    long getAge();

    float getExplosionRadius();
    void setExplosionRadius(float explosionRadius);



    float getArmour();
    void setArmour(float armour);

    float getPenetration();
    void setPenetration(float penetration);

    Color getColor();
    void setColor(Color color);

    Color getExplosionColor();
    void setExplosionColor(Color color);


    float getDamage();
    void setDamage(float damage);

    float getEmpDamage();
    void setEmpDamage(float empDamage);

    float getMaxRotationSpeed();
    void setMaxRotationSpeed(float maxRotationSpeed);

    boolean isEmpArmament();
    void setEmpArmament(boolean empArmament);

    Vector2 getPos();
    void setPos(Vector2 pos);

    Vector2 getVel();
    void setVel(Vector2 pos);

    Vector2 getAcc();
    void setAcc(Vector2 pos);

    Vector2 getDir();
    void setDir(Vector2 pos);

    Set<ObjectType> getType();

    ObjectSide getSide();

    void setSide(ObjectSide side);

    float getMaxThrottle();

    int getReloadTime();
    void setReloadTime(int tick);

    long getLastFired();
    void setLastFired(long tick);


    default void copyTo(Ammo ammo) {

        ammo.setMass(getMass());
        ammo.setTTL(getTTL());
        ammo.setPenetration(getPenetration());
        ammo.setArmour(getArmour());
        ammo.setDamage(getDamage());
        ammo.setEmpDamage(getEmpDamage());
        ammo.setExplosionRadius(getExplosionRadius());
        ammo.setExplosionColor(getExplosionColor());
        ammo.setColor(getColor());
        ammo.setMaxHealth(getMaxHealth());
        ammo.setRadius(getRadius());
        ammo.setMaxRotationSpeed(getMaxRotationSpeed());
        ammo.setEmpArmament(isEmpArmament());
        ammo.setHealthGeneration(getHealthGeneration());
        ammo.setHealthRegenerationCoefficient(getHealthRegenerationCoefficient());
        ammo.setOwner(getOwner());
        ammo.setSide(getSide());
    }



}
