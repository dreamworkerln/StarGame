package ru.geekbrains.entities.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import ru.geekbrains.entities.equipment.BPU;
import ru.geekbrains.entities.particles.Message;
import ru.geekbrains.entities.projectile.FragMissile;
import ru.geekbrains.entities.projectile.Missile;
import ru.geekbrains.entities.projectile.Shell;
import ru.geekbrains.entities.weapons.Gun;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;

public class Planet extends GameObject {

    private GameObject target;


    public Planet(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        name = "planet";


        mass = 1000000f;

        setMaxHealth(1000000f);

        this.type.add(ObjectType.PLANET);

    }

    // Силы планетарной обороны
    public void hit(GameObject o) {

    }


    @Override
    public void update(float dt) {

        //dir.rotate(0.1f);
        //sprite.setAngle(dir.angle());
    }

    @Override
    public void draw(Renderer renderer) {
        super.draw(renderer);
    }

}
