package ru.geekbrains;

import com.badlogic.gdx.Game;

import ru.geekbrains.screen.GameScreen;

public class StarGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
