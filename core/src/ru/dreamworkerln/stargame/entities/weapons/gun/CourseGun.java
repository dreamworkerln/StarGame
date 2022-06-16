package ru.dreamworkerln.stargame.entities.weapons.gun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ru.dreamworkerln.stargame.entities.equipment.BPU;
import ru.dreamworkerln.stargame.entities.equipment.interfaces.GunSystem;
import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.objects.ObjectType;
import ru.dreamworkerln.stargame.entities.objects.PlayerShip;
import ru.dreamworkerln.stargame.entities.projectile.Ammo;
import ru.dreamworkerln.stargame.entities.projectile.shell.Shell;
import ru.dreamworkerln.stargame.screen.GameScreen;
import ru.dreamworkerln.stargame.screen.Renderer;

import java.util.*;
import java.util.function.Predicate;

public class CourseGun extends NonRotatableGun {

    public CourseGun(GameObject owner, float height) {
        super(height, owner);

        addAmmoType(() -> new Shell(calibre, calibre/8, owner));

    }


}
