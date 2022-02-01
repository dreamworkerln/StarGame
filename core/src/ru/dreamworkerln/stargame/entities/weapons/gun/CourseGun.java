package ru.dreamworkerln.stargame.entities.weapons.gun;

import ru.dreamworkerln.stargame.entities.objects.GameObject;
import ru.dreamworkerln.stargame.entities.projectile.shell.Shell;

public class CourseGun extends NonRotatableGun {
    public CourseGun(GameObject owner, float height) {
        super(height, owner);

        addAmmoType(() -> new Shell(calibre, calibre/8, owner));

    }

}
