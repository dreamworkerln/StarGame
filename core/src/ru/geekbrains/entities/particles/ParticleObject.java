package ru.geekbrains.entities.particles;


import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.screen.Renderer;
import ru.geekbrains.screen.RendererType;

/**
 * Object without world interaction
 */
public abstract class ParticleObject extends GameObject {






    public ParticleObject(float height, GameObject owner) {

        super(owner, height);
        setRenderType();
    }

    public ParticleObject(GameObject owner) {

        super(owner);
        setRenderType();
    }

    private void setRenderType() {
        rendererType.add(RendererType.SHAPE);
    }

}

