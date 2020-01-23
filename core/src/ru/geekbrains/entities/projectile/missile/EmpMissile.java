package ru.geekbrains.entities.projectile.missile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.objects.ObjectType;

public class EmpMissile extends Missile {

    public EmpMissile(TextureRegion textureRegion, float height, GameObject owner) {
        super(textureRegion, height, owner);

        this.type.add(ObjectType.EMP_MISSILE);


        engineTrail.color = new Color(0.65f, 0.87f, 1, 1);
        explosionColor = new Color(0.25f, 0.57f, 1, 1);

        explosionRadius = radius * 4;

        damage = 0.5f;

        isEmpOrdinance = true;
        empDamage = 2000;
    }

//    @Override
//    public void dispose() {
//
//        Projectile frag = new Fragment(6,  owner);
//        frag.type.add(ObjectType.EMP_FRAG);
//        frag.setMass(1);
//        frag.setTTL(10);
//
//        frag.pos.set(pos);
//        frag.vel.set(vel);
//        frag.dir.set(dir);
//        frag.owner = owner;
//
//        frag.setExplosionRadius(frag.getExplosionRadius()*30);
//
//
//        GameScreen.addObject(frag);
//
//        super.dispose();
//    }


}
