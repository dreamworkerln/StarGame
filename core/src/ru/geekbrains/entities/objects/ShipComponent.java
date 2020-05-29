package ru.geekbrains.entities.objects;

import ru.geekbrains.entities.particles.ParticleObject;

public class ShipComponent extends ParticleObject {
    public ShipComponent(float height, GameObject owner) {
        super(height, owner);
    }

    public ShipComponent(GameObject owner) {
        super(owner);
    }
}
