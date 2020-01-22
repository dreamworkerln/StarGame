package ru.geekbrains.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;
import ru.geekbrains.screen.GameScreen;

public class EmpMissile extends Missile {

    public EmpMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        engineTrail.color = new Color(0.65f, 0.87f, 1, 1);
        damage = 0.5f;
    }

    @Override
    public void dispose() {

        Projectile frag = new Fragment(6,  owner);
        frag.type.add(ObjectType.EMP_FRAG);
        frag.setMass(1);
        frag.setTTL(10);

        frag.pos.set(pos);
        frag.vel.set(vel);
        frag.dir.set(dir);
        frag.owner = owner;

        frag.setExplosionRadius(frag.getExplosionRadius()*20);


        GameScreen.addObject(frag);

        super.dispose();
    }


}
