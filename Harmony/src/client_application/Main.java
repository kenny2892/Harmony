package client_application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
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
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application
{
	private static Socket socket;
	private static String username;
	private static Color userColor;
	private static Stage stage;
	private static ArrayList<String> usersInRoom; // Notes from Beta Tester: Multiple people can't login at once, Can't do links (pases //), closing inifinite loop (closing without loging in), IDEA: how many msgs are in a room, Change swaping of chats
	private static ArrayList<Node> roomOneChat; // Arrows turn white, possible add loading room screen
	private static ArrayList<Node> roomTwoChat;
	private static ArrayList<Node> roomThreeChat;
	public static final int CHAR_LIMIT = 120;
	private static StartMode startMode = StartMode.TITLE;
	private static TitleMode titleMode = TitleMode.SERVER;
	private static File downloadDirectory;
	private static File fileToSend;
	
	private static double xOffset = 0, yOffset = 0;
	
	public enum StartMode
	{
		TITLE, SETTINGS
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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_application/Main.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
//			scene.getStylesheets().add(getClass().getResource("/client_application/application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.getIcons().add(new Image(getClass().getResource("/resources/System Icon.png").toExternalForm()));
			primaryStage.setTitle("Harmony");
			primaryStage.show();
			
//			controller = loader.getController();
			
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
			
			String home = System.getProperty("user.home");
			downloadDirectory = new File(home+"/Downloads/"); 
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
	
	public static void joinServer()
	{
		try
		{
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output, true);
			writer.println("\\u" + username + " #" + colorToHex(userColor));
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
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
	
	public static boolean setUsername(String name)
	{
		if(name == null || name.length() == 0)
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
	
	public static String getDownloadDirPath()
	{
		return downloadDirectory.getPath();
	}
	
	public static void minimizeApp()
	{
		stage.setIconified(true);
	}
	
	public static void closeApp()
	{
		try
		{
			if(socket != null && socket.isConnected())
				socket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		stage.close();
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
		if(mode == null)
			return;
		
		startMode = mode;
	}
	
	public static TitleMode getTitleMode()
	{
		return titleMode;
	}
	
	public static void setTitleMode(TitleMode mode)
	{
		if(mode == null)
			return;
		
		titleMode = mode;
	}
	
	public static String getHexColor()
	{
		if(userColor == null)
		{
			Random r = new Random();
			userColor = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		}
		
		return "#" + colorToHex(userColor);
	}
	
	public static void selectFile()
	{
		if(stage == null)
			return;
		
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose a File to Send to ");
		
		fileToSend = chooser.showOpenDialog(stage);
	}
	
	public static boolean sendFile(String userToSendTo)
	{
		if(fileToSend == null || !fileToSend.exists() || !usersInRoom.contains(userToSendTo))
		{
			// TODO
			return false;
		}
		
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
			
//			writer.close();
			in.close();
//			out.close();
			
			return true;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
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
		        hex2 = String.format("00000%s", hex1.substring(0,1));
		        break;
		    case 4:
		        hex2 = String.format("0000%s", hex1.substring(0,2));
		        break;
		    case 5:
		        hex2 = String.format("000%s", hex1.substring(0,3));
		        break;
		    case 6:
		        hex2 = String.format("00%s", hex1.substring(0,4));
		        break;
		    case 7:
		        hex2 = String.format("0%s", hex1.substring(0,5));
		        break;
		    default:
		        hex2 = hex1.substring(0, 6);
		    }
		
		    return hex2;
	}
}
