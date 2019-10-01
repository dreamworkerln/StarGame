package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.entities.objects.Projectile;
import ru.geekbrains.screen.GameScreen;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.storage.Game;

public class Bullet extends Projectile {


    public Bullet(float height, GameObject owner) {
        super(height, owner);

        type.add(ObjectType.BULLET);
        mass = 0.001f;

        setMaxHealth(0.01f);
        damage = 0.01f;
    }


}
