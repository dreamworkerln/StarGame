package ru.geekbrains.entities.weapons.gun;

import ru.geekbrains.entities.objects.GameObject;
import ru.geekbrains.entities.projectile.shell.Shell;

public class CourseGun extends NonRotatableGun {
    public CourseGun(GameObject owner, float height) {
        super(height, owner);

        addAmmoType(() -> new Shell(calibre, calibre/8, owner));

    }

}
