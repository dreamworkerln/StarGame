package ru.dreamworkerln.stargame.entities.particles;


import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.screen.RendererType;

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

