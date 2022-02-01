package ru.dreamworkerln.stargame;

import com.badlogic.gdx.Game;

import ru.dreamworkerln.stargame.screen.GameScreen;

public class StarGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
