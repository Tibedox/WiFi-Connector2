package ru.tibedox.wificonnector2;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ru.tibedox.wificonnector2.WiFiConnector;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("WiFi Connector2");
		config.setWindowedMode(1280, 720);
		new Lwjgl3Application(new WiFiConnector(), config);
	}
}
