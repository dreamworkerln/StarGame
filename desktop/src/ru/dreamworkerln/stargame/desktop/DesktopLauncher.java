package ru.dreamworkerln.stargame.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.dreamworkerln.stargame.StarGame;

public class DesktopLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        Graphics.DisplayMode displayMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
        Graphics.DisplayMode[] modes = LwjglApplicationConfiguration.getDisplayModes();

        try {
            config.setFromDisplayMode(modes[1]);
        }
        catch (Exception ignored) {}

        float aspect = 10f/16f;
        config.height = 1920;
        config.width = (int) (config.height / aspect);
        config.audioDeviceSimultaneousSources = 64;


        //config.forceExit = false;
        //config.resizable = true;
        //config.fullscreen = false;

        new LwjglApplication(new StarGame(), config);
    }
}
