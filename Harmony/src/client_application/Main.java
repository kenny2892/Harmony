package client_application;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application
{
	private static Socket socket;
	private static String username;
	private static Color userColor;
	private static int iconID;
	private static Controller controller;
	
	private static Stage stage;
	private static ArrayList<String> usersInRoom;
	private static ArrayList<String> usersToDM;
	private static HashMap<String, List<Node>> dmMsgs;
	private static String currentDm;
	private static ArrayList<Node> roomOneChat;
	private static ArrayList<Node> roomTwoChat;
	private static ArrayList<Node> roomThreeChat;
	private static int totalMsgCount = 0;
	private static int roomOneMsgCount = 0;
	private static int roomTwoMsgCount = 0;
	private static int roomThreeMsgCount = 0;
	private static StartMode startMode = StartMode.TITLE;
	private static TitleMode titleMode = TitleMode.SERVER;
	private static File downloadDirectory;
	private static File fileToSend;
	private static double xOffset = 0, yOffset = 0;

	public static final int CHAR_LIMIT = 80;
	public static final int MAX_ICON_ID = 5;

	public enum StartMode
	{
		TITLE, SETTINGS, DM
	}

	public enum TitleMode
	{
		SERVER, LOGIN, SIGNED_IN
	}

	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("/client_application/Main.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			controller = loader.getController();

			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.getIcons().add(new Image(getClass().getResource("/resources/System Icon.png").toExternalForm()));
			primaryStage.setTitle("Harmony");
			primaryStage.show();

			double height = scene.getHeight();
			double width = scene.getWidth();

			Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
			double newX = ((screenBounds.getWidth() - width) / 2);
			double newY = ((screenBounds.getHeight() - height) / 2);

			primaryStage.setX(newX);
			primaryStage.setY(newY);

			stage = primaryStage;

			roomOneChat = new ArrayList<Node>();
			roomTwoChat = new ArrayList<Node>();
			roomThreeChat = new ArrayList<Node>();
			
			usersInRoom = new ArrayList<String>();
			usersToDM = new ArrayList<String>();
			dmMsgs = new HashMap<String, List<Node>>();
			

			String home = System.getProperty("user.home");
			downloadDirectory = new File(home + "/Downloads/");
			iconID = 0;
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	public static boolean startConnection(String hostname, int port)
	{
		try
		{
			socket = new Socket(hostname, port);

			Random r = new Random();
			userColor = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));
			return true;
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static void sendMsg(String msg)
	{
		try
		{
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println(msg);
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void addToRoomOne(Node... elements)
	{
		for(Node element : elements)
			roomOneChat.add(element);
	}

	public static void addToRoomTwo(Node... elements)
	{
		for(Node element : elements)
			roomTwoChat.add(element);
	}

	public static void addToRoomThree(Node... elements)
	{
		for(Node element : elements)
			roomThreeChat.add(element);
	}

	public static ArrayList<Node> getRoomOne()
	{
		return roomOneChat;
	}

	public static ArrayList<Node> getRoomTwo()
	{
		return roomTwoChat;
	}

	public static ArrayList<Node> getRoomThree()
	{
		return roomThreeChat;
	}

	public static Socket getSocket()
	{
		return socket;
	}

	public static String getUsername()
	{
		return username;
	}

	public static Color getUserColor()
	{
		return userColor;
	}

	public static boolean setUserColor(Color color)
	{
		if (color == null)
			return false;

		userColor = color;
		return true;
	}

	public static int getIconID()
	{
		return iconID;
	}

	public static boolean setIconID(int id)
	{
		if (iconID < 0 || iconID > MAX_ICON_ID)
			return false;

		iconID = id;
		return true;
	}

	public static boolean setUsername(String name)
	{
		if (name == null || name.length() == 0)
			return false;

		username = name;
		return true;
	}

	public static int getUserIndex(String username)
	{
		return usersInRoom.indexOf(username);
	}

	public static void addUserToRoom(String username)
	{
		usersInRoom.add(username);
	}

	public static void removeUserInRoom(int indexToRemove)
	{
		usersInRoom.remove(indexToRemove);
	}

	public static boolean isInRoom(String userToCheck)
	{
		return usersInRoom.contains(userToCheck);
	}

	public static void clearRoom()
	{
		usersInRoom.clear();
	}

	public static String getUserList()
	{
		String list = "";
		for(String user : usersInRoom)
			list += user + ", ";

		list = list.substring(0, list.length() - 1);

		return list;
	}

	public static ArrayList<String> getUserArray()
	{
		return usersInRoom;
	}
	
	public static boolean isInDMs(String userToCheck)
	{
		return usersToDM.contains(userToCheck);
	}
	
	public static void addToDMs(String userToAdd)
	{
		usersToDM.add(userToAdd);
	}

	public static String getDownloadDirPath()
	{
		return downloadDirectory.getPath();
	}
	
	public static boolean setDownloadDirthPath(File newDir)
	{
		if(newDir == null || !newDir.exists())
			return false;
		
		downloadDirectory = newDir;
		return true;
	}
	
	public static void selectDirectory()
	{
		if (stage == null)
			return;

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose a Download Directory");

		File temp = chooser.showDialog(stage);
		
		if(temp != null)
		{
			downloadDirectory = temp;
			controller.updateDownloadPath();
		}
	}

	public static void minimizeApp()
	{
		stage.setIconified(true);
	}

	public static void closeApp()
	{
		try
		{
			if (socket != null && socket.isConnected())
				socket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		stage.close();
		System.exit(0);
	}

	public static void setXOffset(double newX)
	{
		xOffset = newX;
	}

	public static void setYOffset(double newY)
	{
		yOffset = newY;
	}

	public static void setX(MouseEvent event)
	{
		stage.setX(event.getScreenX() - xOffset);
	}

	public static void setY(MouseEvent event)
	{
		stage.setY(event.getScreenY() - yOffset);
	}

	public static StartMode getStartMode()
	{
		return startMode;
	}

	public static void setStartMode(StartMode mode)
	{
		if (mode == null)
			return;

		startMode = mode;
	}

	public static TitleMode getTitleMode()
	{
		return titleMode;
	}

	public static void setTitleMode(TitleMode mode)
	{
		if (mode == null)
			return;

		titleMode = mode;
	}

	public static String getHexColor()
	{
		if (userColor == null)
		{
			Random r = new Random();
			userColor = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		}

		return "#" + colorToHex(userColor);
	}

	public static void selectFile()
	{
		if (stage == null)
			return;

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose a File to Send to ");

		File temp = chooser.showOpenDialog(stage);
		
		if(temp != null)
			fileToSend = temp;
	}

	public static boolean sendFile(String userToSendTo)
	{
		if (fileToSend == null || !fileToSend.exists() || !usersInRoom.contains(userToSendTo))
			return false;

		try
		{
			InputStream in = new FileInputStream(fileToSend);
			OutputStream out = socket.getOutputStream();

			PrintWriter writer = new PrintWriter(out, true); // \\f Reciever File_Name

			String msg = "\\f " + userToSendTo + " " + fileToSend.getName() + " " + fileToSend.length();

			writer.println(msg);

			Thread.sleep(10);

			byte[] buffer = new byte[3000];
			int count = 0;
			while((count = in.read(buffer)) > 0)
				out.write(buffer, 0, count);

			in.close();
			return true;
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	public static String getRandomHexColor()
	{
		Random r = new Random();
		Color temp = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		
		return colorToHex(temp);
	}

	private static String colorToHex(Color color)
	{
		String hex1, hex2;

		hex1 = Integer.toHexString(color.hashCode()).toUpperCase();

		switch(hex1.length())
		{
			case 2:
				hex2 = "000000";
				break;
			case 3:
				hex2 = String.format("00000%s", hex1.substring(0, 1));
				break;
			case 4:
				hex2 = String.format("0000%s", hex1.substring(0, 2));
				break;
			case 5:
				hex2 = String.format("000%s", hex1.substring(0, 3));
				break;
			case 6:
				hex2 = String.format("00%s", hex1.substring(0, 4));
				break;
			case 7:
				hex2 = String.format("0%s", hex1.substring(0, 5));
				break;
			default:
				hex2 = hex1.substring(0, 6);
		}

		return hex2;
	}

	public static void msgCountIncrease(int roomNum)
	{
		if (roomNum < 1 && roomNum > 3)
			return;

		if (totalMsgCount == 0)
		{
			totalMsgCount++;
			return;
		}

		switch(roomNum)
		{
			case 1:
				roomOneMsgCount++;
				totalMsgCount++;
				break;

			case 2:
				roomTwoMsgCount++;
				totalMsgCount++;
				break;

			case 3:
				roomThreeMsgCount++;
				totalMsgCount++;
				break;
		}
	}

	public static int getRoomMsgCount(int roomNum)
	{
		switch(roomNum)
		{
			case 1:
				return roomOneMsgCount;

			case 2:
				return roomTwoMsgCount;

			case 3:
				return roomThreeMsgCount;
		}

		return 0;
	}

	public static int getTotalMsgCount()
	{
		return totalMsgCount;
	}

	public static void increaseTotalMsgCount()
	{
		totalMsgCount++;
	}
	
	public static void setCurrentDmUser(String username)
	{
		if(username == null)
			throw new IllegalArgumentException("Null username");
		
		if(usersToDM.contains(username))
			currentDm = username;
	}
	
	public static String getCurrentDmUser()
	{
		return currentDm;
	}
	
	public static void setDmMsg(String username, List<Node> msgs)
	{
		if(currentDm == null)
			return;
		
		else if(username == null)
			throw new IllegalArgumentException("Null username");
		
		else if(msgs == null)
			throw new IllegalArgumentException("Null msgs");
		
		if(dmMsgs.containsKey(username))
			dmMsgs.remove(username);
		
		dmMsgs.put(username, msgs);
	}
	
	public static void addDmMsg(String username, ArrayList<Node> msgs)
	{
		if(username == null)
			throw new IllegalArgumentException("Null username");
		
		else if(msgs == null)
			throw new IllegalArgumentException("Null msgs");
		
		List<Node> ogMsg = dmMsgs.get(username);
		
		if(ogMsg == null)
			return;
		
		ogMsg.addAll(msgs);
		dmMsgs.put(username, ogMsg);
	}
	
	public static List<Node> getDmMsg(String username)
	{
		if(!dmMsgs.containsKey(username))
			dmMsgs.put(username, new ArrayList<Node>());
		
		return dmMsgs.get(username);
	}

	public static void openGitHub()
	{
		try
		{
			Desktop.getDesktop().browse(new URL("https://github.com/kenny2892/Harmony").toURI());
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		catch(URISyntaxException e)
		{
			e.printStackTrace();
		}
	}
}
