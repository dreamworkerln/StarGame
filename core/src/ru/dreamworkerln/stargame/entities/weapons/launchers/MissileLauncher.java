package ru.dreamworkerln.stargame.entities.weapons.launchers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.projectile.missile.Missile;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;


public class MissileLauncher extends AbstractMissileLauncher {

    private final static Sound MISSILE_FIRE_01 = Gdx.audio.newSound(Gdx.files.internal("launch04.mp3"));

    public MissileLauncher(float height, GameObject owner) {

        super(height, owner);
        type.add(ObjectType.MISSILE_LAUNCHER);

        gunFire = MISSILE_FIRE_01;

        addAmmoType(() -> new Missile(new TextureRegion(MISSILE_TEXTURE), 2, owner));
    }

}
