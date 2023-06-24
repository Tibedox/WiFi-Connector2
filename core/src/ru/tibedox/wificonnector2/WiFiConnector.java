package ru.tibedox.wificonnector2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector3;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class WiFiConnector extends ApplicationAdapter {
	public static final float SCR_WIDTH = 1280;
	public static final float SCR_HEIGHT = 720;
	
	SpriteBatch batch; 
	OrthographicCamera camera;
	Vector3 touch;
	BitmapFont font;
	InputKeyboard keyboard;
	boolean isEnterIP;

	Texture imgBackGround;
	Texture imgRed;
	Texture imgBlue;

	TextButton btnCreateServer;
	TextButton btnCreateClient;
	TextButton btnExit;

	// всё, что требуется для работы сетевого соединения
	private InetAddress ipAddress;
	private String ipAddressOfServer = "?";
	MyServer server;
	MyClient client;
	boolean isServer;
	boolean isClient;
	MyRequest requestFromClient;
	MyResponse responseFromServer;

	@Override
	public void create () {
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCR_WIDTH, SCR_HEIGHT);
		touch = new Vector3();
		createFont();
		keyboard = new InputKeyboard(SCR_WIDTH, SCR_HEIGHT, 15);

		imgBackGround = new Texture("swamp.jpg");
		imgRed = new Texture("circlered.png");
		imgBlue = new Texture("circleblue.png");

		btnCreateServer = new TextButton(font, "Create Server", 100, 600);
		btnCreateClient = new TextButton(font, "Create Client", 100, 400);
		btnExit = new TextButton(font, "Exit", 100, 100);

		requestFromClient = new MyRequest();
		responseFromServer = new MyResponse();
	}

	@Override
	public void render() {
		// касания экрана
		if(Gdx.input.justTouched()){
			touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touch);

			if(btnCreateServer.hit(touch.x, touch.y) && !isServer && !isClient && !isEnterIP) {
				server = new MyServer(responseFromServer);
				ipAddressOfServer = detectIP();
				isServer = true;
			}
			if(btnCreateClient.hit(touch.x, touch.y) && !isServer && !isClient && !isEnterIP){
				isEnterIP = true;
			}
			if(isEnterIP && keyboard.endOfEdit(touch.x, touch.y)) {
				isEnterIP = false;
				isClient = true;
				ipAddressOfServer = keyboard.getText();
				client = new MyClient(ipAddressOfServer, requestFromClient);
				if(client.isCantConnected){
					isClient = false;
					client = null;
					ipAddressOfServer = "Server not found";
				}
			}
			if(btnExit.hit(touch.x, touch.y) && !isEnterIP){
				Gdx.app.exit();
			}
		}
		if(Gdx.input.isTouched() && !isEnterIP) {
			touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touch);
		}

		// события игры
		if(isServer){
			responseFromServer.text = "blue: ";
			responseFromServer.x = touch.x;
			responseFromServer.y = touch.y;
			requestFromClient = server.getRequest();
		} else if(isClient){
			requestFromClient.text = "red: ";
			requestFromClient.x = touch.x;
			requestFromClient.y = touch.y;
			client.send();
			responseFromServer = client.getResponse();
		}


		// вывод изображений
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(imgBackGround, 0, 0, SCR_WIDTH, SCR_HEIGHT);

		batch.draw(imgRed, requestFromClient.x-50, requestFromClient.y-50, 100, 100);
		batch.draw(imgBlue, responseFromServer.x-50, responseFromServer.y-50, 100, 100);
		font.draw(batch, "Server "+ responseFromServer.text+ (int) responseFromServer.x+" "+ (int) responseFromServer.y, 100, 300);
		font.draw(batch, "Client "+ requestFromClient.text+ (int) requestFromClient.x+" "+ (int) requestFromClient.y, 100, 200);

		btnCreateServer.font.draw(batch, btnCreateServer.text, btnCreateServer.x, btnCreateServer.y);
		font.draw(batch, "Server's IP: "+ ipAddressOfServer, btnCreateServer.x, btnCreateServer.y-100);
		btnCreateClient.font.draw(batch, btnCreateClient.text, btnCreateClient.x, btnCreateClient.y);
		btnExit.font.draw(batch, btnExit.text, btnExit.x, btnExit.y);
		if(isEnterIP) {
			keyboard.draw(batch);
		}

		batch.end();
	}

	void createFont(){
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ubuntumono.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.characters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyzАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;:,{}\"´`'<>";
		parameter.size = 50;
		parameter.color = Color.ORANGE;
		parameter.borderWidth = 3;
		parameter.borderColor = Color.BLACK;
		font = generator.generateFont(parameter);
	}

	@Override
	public void dispose () {
		batch.dispose();
		keyboard.dispose();
		font.dispose();
		imgBackGround.dispose();
		imgRed.dispose();
		imgBlue.dispose();
	}

	public String detectIP() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (!address.isLinkLocalAddress() && !address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
						ipAddress = address;
						//System.out.println("IP-адрес устройства: " + ipAddress.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if(ipAddress != null){
			return ipAddress.getHostAddress();
		}
		return "";
	}
}

