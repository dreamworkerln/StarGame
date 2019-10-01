package ru.geekbrains.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

import ru.geekbrains.StarGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		//Graphics.DisplayMode displayMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
		//Graphics.DisplayMode[] modes = LwjglApplicationConfiguration.getDisplayModes();
		//config.setFromDisplayMode(modes[1]);

		float aspect = 3f/4f;
		config.height = 2000;
		config.width = (int) (config.height / aspect);


		//config.forceExit = false;
		//config.resizable = true;
		config.fullscreen = true;

		new LwjglApplication(new StarGame(), config);
	}
}
