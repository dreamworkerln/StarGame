package ru.geekbrains.entities.objects;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.geekbrains.screen.Renderer;

public class Planet extends GameObject {

    private GameObject target;


    public Planet(TextureRegion textureRegion, float height, GameObject owner) {
        super(owner, textureRegion, height);

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
