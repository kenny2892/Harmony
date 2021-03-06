package server_application;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SocketServer extends Application
{
	private static Selector selector;
	private static Room roomOne, roomTwo, roomThree, notInRoom, privateRoom;
	private static InetSocketAddress listeningAddress;
	
	private static boolean running;
	private static Stage stage;
	private static ServerController controller;
	private static double xOffset = 0, yOffset = 0;
	
	private static HashMap<String, String> privateChat;
	private static HashMap<String, String> privateChatOldRooms;
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/server_application/Server.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			controller = loader.getController();
			
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.sizeToScene();
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.getIcons().add(new Image(getClass().getResource("/resources/System Icon.png").toExternalForm()));
			primaryStage.setTitle("Harmony Server App");
			primaryStage.show();

			double height = scene.getHeight();
			double width = scene.getWidth();

			Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
			double newX = ((screenBounds.getWidth() - width) / 2);
			double newY = ((screenBounds.getHeight() - height) / 2);

			primaryStage.setX(newX);
			primaryStage.setY(newY);
			
			stage = primaryStage;
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void stopServer()
	{
		running = false;
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
	
	public static void startServer(int port)
	{
//		listeningAddress = new InetSocketAddress("localhost", port); // For Testing
		try
		{
			listeningAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), port);
		}
		
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}

		roomOne = new Room();
		roomTwo = new Room();
		roomThree = new Room();
		notInRoom = new Room();
		privateRoom = new Room();
		
		privateChat = new HashMap<String, String>();
		privateChatOldRooms = new HashMap<String, String>();
		
		running = true;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				startWatching();
			}
		}).start();
	}

	public static void startWatching()
	{
		try
		{
			controller.writeToConsole("Starting Server at: " + listeningAddress.getHostString() + " Port# " + listeningAddress.getPort());
			selector = Selector.open();
			
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);

			serverChannel.socket().bind(listeningAddress);
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);

			while(running)
			{
				selector.select(); // Wait here until something happens

				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while(keys.hasNext())
				{
					SelectionKey key = (SelectionKey) keys.next();
					keys.remove();
					
					if(key.isValid())
					{
						if(key.isAcceptable())
							accept(key);

						else
							if(key.isReadable())
								read(key);
					}
				}
			}
		}

		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}
	}

	private static void accept(SelectionKey key)
	{
		try
		{
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			SocketChannel socketChannel = serverChannel.accept();
			socketChannel.configureBlocking(false);

			Socket socket = socketChannel.socket();
			SocketAddress remoteAddress = socket.getRemoteSocketAddress();
			controller.writeToConsole("Connected to " + remoteAddress);

			socketChannel.register(selector, SelectionKey.OP_READ);
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void read(SelectionKey key)
	{
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		String msg = "";
		byte[] sentObj = new byte[0];

		int numberRead = -1;
		
		try
		{
			while((numberRead = socketChannel.read(buffer)) > 0)
			{
				buffer.flip();

				byte[] data = new byte[buffer.limit()];
				buffer.get(data);
				msg += new String(data, StandardCharsets.UTF_8);
				buffer.clear();
				
				if(msg.startsWith("\\f"))
				{
					fileTransfer(msg, numberRead, socketChannel, buffer, data, sentObj);
					return;
				}
			}
		}

		catch(Exception e)
		{
			key.cancel();
			numberRead = -1;
		}
		
		if(msg.contains("\r\n"))
			msg = msg.replace("\r\n", "");
		
		if(msg.startsWith("\\x")) // Disconnect User: \\x
		{
			key.cancel();
			numberRead = -1;
		}
		
		if(numberRead == -1) // Disconnected User
		{
			userDisconnect(msg, socketChannel, key);
			return;
		}
		
		else if(msg.startsWith("\\l")) // List users in the room: \\l
		{
			listUsers(msg, socketChannel);
			return;
		}
		
		else if(msg.startsWith("\\r")) // List the room names: \\r
		{
			listRooms(msg, socketChannel);			
			return;
		}
		
		else if(msg.startsWith("\\e ")) // Change Room: \\e RoomNum
		{
			changeRoom(msg, socketChannel);			
			return;
		}
		
		else if(msg.startsWith("\\w")) // What room is the client in: \\w
		{
			whatRoom(msg, socketChannel);			
			return;
		}
		
		else if(msg.startsWith("\\msg ")) // Message User: \\msg userToSendTo msg
		{
			msg(msg, socketChannel);			
			return;
		}
		
		else if(msg.startsWith("\\p")) // Enter Private Chat: \\p userToChatWith
		{
			joinPrivate(msg, socketChannel);
			return;
		}
		
		else if(msg.startsWith("\\q"))// Leave Private Chat: \\q
		{
			leavePrivate(socketChannel);
			return;
		}
		
		else if(msg.startsWith("\\u ")) // Link Username: Non-Harmony: \\u username
		{							  //					 Harmony: \\u username #color iconID
			registerUser(msg, socketChannel);			
			return;
		}
		
		else if(msg.startsWith("\\checkUser ")) // Check if username is in use: \\checkUser username
		{
			checkUser(msg, socketChannel);			
			return;
		}
		
		else if(msg.startsWith("\\dmUserCheck "))
		{
			dmCheckUser(msg, socketChannel);
			return;
		}
		
		else if(msg.startsWith("\\userUpdate ")) // Update User Info: \\userUpdate color iconID
		{
			updateUser(msg, socketChannel);			
			return;
		}
		
		else if(msg.startsWith("\\dm ")) // Dm message: \\dm userToReci
		{
			dm(msg, socketChannel);
			return;
		}
		
		Room curr = null;
		
		if(roomOne.containsClientBySocketChannel(socketChannel))
			curr = roomOne;
		
		else if(roomTwo.containsClientBySocketChannel(socketChannel))
			curr = roomTwo;
		
		else if(roomThree.containsClientBySocketChannel(socketChannel))
			curr = roomThree;
		
		else if(privateRoom.containsClientBySocketChannel(socketChannel))
			curr = privateRoom;
		
		sendMsg(msg.getBytes(StandardCharsets.UTF_8), curr, curr.findClientBySocket(socketChannel));
	}
	
	private static void fileTransfer(String msg, int numberRead, SocketChannel socketChannel, ByteBuffer buffer, byte[] data, byte[] sentObj)
	{
		String[] parts = msg.split(" "); // \f Reciever File_Name Size
		int size = 0;

		try
		{
			if(parts[parts.length - 1].contains("\r\n"))
				parts[parts.length - 1] = parts[parts.length - 1].replace("\r\n", "");
			
			size = Integer.parseInt(parts[parts.length - 1]);
			Thread.sleep(10);
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		try
		{
			Thread.sleep(1000);
			int totalRead = size;

			totalRead -= socketChannel.read(buffer);
			do
			{
				buffer.flip();

				data = new byte[buffer.limit()];
				buffer.get(data);
				byte[] temp = new byte[sentObj.length + data.length];
				System.arraycopy(sentObj, 0, temp, 0, sentObj.length);
				System.arraycopy(data, 0, temp, sentObj.length, data.length);
				
				sentObj = temp;
				
				buffer.clear();
			}while((totalRead -= socketChannel.read(buffer)) > 0);
		}
		
		catch(Exception e)
		{
			controller.writeToConsole(e.getMessage());
		}
		
		if(msg.contains("\r\n"))
			msg = msg.replace("\r\n", "");					
		
		ClientData clientToSendTo = null;
		clientToSendTo = roomOne.findClientByUsername(parts[1]);
		
		if(clientToSendTo == null)
		{
			clientToSendTo = roomTwo.findClientByUsername(parts[1]);
			
			if(clientToSendTo == null)
				clientToSendTo = roomThree.findClientByUsername(parts[1]);
		}
		
		if(clientToSendTo != null)
		{
			String toSendMsg = "\\f ";
			
			if(clientToSendTo.isHarmony()) // Harmony: \\f reciever color iconID file_name length || Non-Harmony: \\f file_name length
				toSendMsg += clientToSendTo.getUsername() + " " + clientToSendTo.getColor() + " " + clientToSendTo.getIconID() + " ";
			
			for(int i = 2; i < parts.length - 1; i++)
				toSendMsg += parts[i] + " ";
								
			toSendMsg += sentObj.length;
			
			try
			{
				SocketChannel socketToSend = clientToSendTo.getSocketChannel();
				ByteBuffer bufferToSend = ByteBuffer.wrap(toSendMsg.getBytes(StandardCharsets.UTF_8));
				socketToSend.write(bufferToSend);
				bufferToSend.rewind();
				Thread.sleep(1000);
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
			
			ByteBuffer fileBuffer = ByteBuffer.allocate(sentObj.length);
			fileBuffer.clear();
			fileBuffer.put(sentObj);
			fileBuffer.flip();
			
			int totalSent = 0;
			
			try
			{
				while(fileBuffer.hasRemaining())
					totalSent += clientToSendTo.getSocketChannel().write(fileBuffer);
			}
			
			catch(Exception e)
			{
				controller.writeToConsole(e.getMessage());
			}
			
			controller.writeToConsole("File sent. Size: " + totalSent);
		}	
	}
	
	private static void dm(String msg, SocketChannel socketChannel)
	{
		String[] parsedMsg = msg.split(" ");
		
		ClientData sender = null;
		
		if(sender == null)
			sender = roomOne.findClientBySocket(socketChannel);
		
		if(sender == null)
			sender = roomTwo.findClientBySocket(socketChannel);
		
		if(sender == null)
			sender = roomThree.findClientBySocket(socketChannel);
		
		if(sender == null)
			sender = notInRoom.findClientBySocket(socketChannel);
		
		if(sender == null)
			return;
		
		ClientData receiver = null;
		
		if(receiver == null)
			receiver = roomOne.findClientByUsername(parsedMsg[1]);
		
		if(receiver == null)
			receiver = roomTwo.findClientByUsername(parsedMsg[1]);
		
		if(receiver == null)
			receiver = roomThree.findClientByUsername(parsedMsg[1]);
		
		if(receiver == null)
			receiver = notInRoom.findClientByUsername(parsedMsg[1]);
		
		if(receiver == null)
			return;
		
		msg = "DM//" + sender.getUsername() + "//" + receiver.getUsername() + "//" + sender.getColor() + "//" + sender.getIconID() + "//" + parsedMsg[2]; // DM//sender//receiver//#user_color//iconID//msg
		
		try
		{
			ByteBuffer sendBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
			receiver.getSocketChannel().write(sendBuffer);
			sendBuffer.rewind();
			
			sender.getSocketChannel().write(sendBuffer);
			sendBuffer.rewind();
			controller.writeToConsole(sender.getUsername() + " sent a DM to " + receiver.getUsername());
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void userDisconnect(String msg, SocketChannel socketChannel, SelectionKey key)
	{
		ClientData client = null;
		
		if(roomOne.containsClientBySocketChannel(socketChannel))
		{
			client = roomOne.findClientBySocket(socketChannel);
			roomOne.removeClient(socketChannel);
		}
		
		else if(roomTwo.containsClientBySocketChannel(socketChannel))
		{
			client = roomTwo.findClientBySocket(socketChannel);
			roomTwo.removeClient(socketChannel);
		}
		
		else if(roomThree.containsClientBySocketChannel(socketChannel))
		{
			client = roomThree.findClientBySocket(socketChannel);
			roomThree.removeClient(socketChannel);
		}
		
		else if(notInRoom.containsClientBySocketChannel(socketChannel))
			notInRoom.removeClient(socketChannel);
		
		else if(privateRoom.containsClientBySocketChannel(socketChannel))
		{
			client = privateRoom.findClientBySocket(socketChannel);
			privateRoom.removeClient(socketChannel);
			privateChatOldRooms.remove(client.getUsername());
		}
		
		if(client == null)
		{
			key.cancel();
			return;
		}
		
		msg = "CLOSED//" + client.getUsername();
		sendMsgToHarmonyClients(msg.getBytes(StandardCharsets.UTF_8));
		
		try
		{
			if(socketChannel.isConnected())
				socketChannel.close();
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		key.cancel();
	}
	
	private static void listUsers(String msg, SocketChannel socketChannel)
	{
		Room curr = findWhichRoom(socketChannel);
		
		if(curr.equals(notInRoom))
			return;
		
		ArrayList<ClientData> clientsInRoom = curr.getClientList();
		
		String userList = "";
		for(ClientData client : clientsInRoom)
			userList += client.getUsername() + ", ";
		
		userList = userList.substring(0, userList.length() - 2);
		
		ClientData client = curr.findClientBySocket(socketChannel);
		if(client.isHarmony())
			userList = "Harmony//#F7931E//100//" + userList + "//" + findRoomNum(curr);
		
		try
		{
			ByteBuffer sendBuffer = ByteBuffer.wrap(userList.getBytes(StandardCharsets.UTF_8));
			socketChannel.write(sendBuffer);
			sendBuffer.rewind();
			controller.writeToConsole(client.getUsername() + ": User List sent");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void listRooms(String msg, SocketChannel socketChannel)
	{
		Room curr = findWhichRoom(socketChannel);
		
		if(curr.equals(notInRoom))
			return;
		
		msg = "One, Two, Three";
		ClientData client = curr.findClientBySocket(socketChannel);
		if(client.isHarmony())
			msg = "Harmony//#F7931E//100//" + msg + "//" + findRoomNum(curr);
		
		try
		{
			ByteBuffer sendBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
			socketChannel.write(sendBuffer);
			sendBuffer.rewind();
			controller.writeToConsole(client.getUsername() + ": List of Rooms Sent");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void changeRoom(String msg, SocketChannel socketChannel)
	{
		Room curr = findWhichRoom(socketChannel);
		
		ClientData client = curr.findClientBySocket(socketChannel);
		
		if(curr.equals(roomOne))
			roomOne.removeClient(socketChannel);
		
		else if(curr.equals(roomTwo))
			roomTwo.removeClient(socketChannel);
		
		else if(curr.equals(roomThree))
			roomThree.removeClient(socketChannel);
		
		else if(curr.equals(notInRoom))
			notInRoom.removeClient(socketChannel);
		
		String[] parts = msg.split(" ");
		String requestedRoom = parts[1].toLowerCase();
		Room newRoom = null;		
		
		int roomNum = -1;
		switch(requestedRoom)
		{
			case "-1":
				notInRoom.addClient(client);
				newRoom = notInRoom;
				roomNum = -1;
				break;
			
			case "one":
				
			case "1":
				roomOne.addClient(client);
				msg = "Server: Joined Room One.";
				roomNum = 1;
				newRoom = roomOne;
				break;
				
			case "two":
				
			case "2":
				roomTwo.addClient(client);
				msg = "Server: Joined Room Two.";
				roomNum = 2;
				newRoom = roomTwo;
				break;
				
			case "three":
				
			case "3":
				roomThree.addClient(client);
				msg = "Server: Joined Room Three.";
				roomNum = 3;
				newRoom = roomThree;
				break;
				
			default:
				msg = "Server: Incorrect room name.";
		}
		
		try
		{
			if(!client.isHarmony())
			{
				ByteBuffer sendBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
				socketChannel.write(sendBuffer);
				sendBuffer.rewind();
				controller.writeToConsole(client.getUsername() + ": Changed to Room #" + roomNum);
			}
			
			else
			{
				for(ClientData clientToBeSent : newRoom.getClientList())
				{
					Room otherRoom = findWhichRoom(clientToBeSent.getSocketChannel());
					
					msg = "USER//" + clientToBeSent.getUsername() + "//" + clientToBeSent.getColor() + "//" + clientToBeSent.getIconID() + "//" + findRoomNum(otherRoom);
					ByteBuffer updateBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
					socketChannel.write(updateBuffer);
					updateBuffer.rewind();
					controller.writeToConsole(client.getUsername() + ": Sent Add Request for " + client.getUsername());	
				}
			}
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		msg = "CLOSED//" + client.getUsername();
		sendMsgToHarmonyClients(msg.getBytes(StandardCharsets.UTF_8));
		msg = "USER//" + client.getUsername() + "//" + client.getColor() + "//" + client.getIconID() + "//" + roomNum;
		sendMsgToHarmonyClients(msg.getBytes(StandardCharsets.UTF_8));
	}
	
	private static void whatRoom(String msg, SocketChannel socketChannel)
	{
		Room curr = findWhichRoom(socketChannel);
		
		if(curr.equals(roomOne))
			msg = "Server: You're in Room One.";
		
		else if(curr.equals(roomTwo))
			msg = "Server: You're in Room Two.";
		
		else if(curr.equals(roomThree))
			msg = "Server: You're in Room Three.";
		
		else if(curr.equals(notInRoom))
			msg = "Server: You're not in a Room.";
		
		ClientData client = curr.findClientBySocket(socketChannel);
		if(client.isHarmony())
			msg = "Harmony//#F7931E//100//" + msg.substring(8) + "//" + findRoomNum(curr);
		
		try
		{
			ByteBuffer sendBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
			socketChannel.write(sendBuffer);
			sendBuffer.rewind();
			controller.writeToConsole(client.getUsername() + ": What Room are they in");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void joinPrivate(String msg, SocketChannel socketChannel)
	{
		String userToChatWith = msg.replace("\\p ", "");
		ClientData client = null;
		Room curr = findWhichRoom(socketChannel);
		
		switch(findRoomNum(curr))
		{
			case 1:
				client = roomOne.findClientBySocket(socketChannel);
				roomOne.removeClient(socketChannel);
				privateChatOldRooms.put(client.getUsername(), "1");
				break;
				
			case 2:
				client = roomTwo.findClientBySocket(socketChannel);
				roomTwo.removeClient(socketChannel);
				privateChatOldRooms.put(client.getUsername(), "2");
				break;
				
			case 3:
				client = roomThree.findClientBySocket(socketChannel);
				roomThree.removeClient(socketChannel);
				privateChatOldRooms.put(client.getUsername(), "3");
				break;
			
			case 4:
				client = privateRoom.findClientBySocket(socketChannel);
				privateChat.remove(client.getUsername());
				privateRoom.removeClient(socketChannel);
				break;
				
			default:
				return;
		}
		
		privateRoom.addClient(client);
		privateChat.put(client.getUsername(), userToChatWith);
		
		controller.writeToConsole(client.getUsername() + " has entered Private Mode. Linked to: " + userToChatWith);
	}
	
	private static void leavePrivate(SocketChannel socketChannel)
	{
		ClientData client = privateRoom.findClientBySocket(socketChannel);
		
		if(client == null)
			return;
		
		privateChat.remove(client.getUsername());
		
		switch(privateChatOldRooms.get(client.getUsername()))
		{
			case "1":
				roomOne.addClient(client);
				break;
				
			case "2":
				roomTwo.addClient(client);
				break;
				
			case "3":
				roomThree.addClient(client);
				break;
				
			default:
				notInRoom.addClient(client);
				break;
		}
		
		controller.writeToConsole(client.getUsername() + " has left Private Mode.");
	}
	
	private static void msg(String msg, SocketChannel socketChannel)
	{
		String[] parts = msg.split(" ");
		
		if(parts.length < 2)
			return;
		
		msg = "";
		for(int i = 2; i < parts.length; i++)
			msg += parts[i] + " ";
		
		msg = msg.substring(0, msg.length() - 1);
		
		Room curr = findWhichRoom(socketChannel);
		ClientData clientToSendTo = curr.findClientByUsername(parts[1]);
		ClientData client = curr.findClientBySocket(socketChannel);
		
		if(clientToSendTo == null)
			return;
		
		else if(clientToSendTo.isHarmony())
			msg = client.getUsername() + "//" + client.getColor() + "//" + client.getIconID() + "//" + msg + "//" + findRoomNum(curr); // userToSendTo color iconID msg roomNum
		
		SocketChannel socketToSendTo = clientToSendTo.getSocketChannel();
		
		try
		{
			ByteBuffer bufferSend = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
			socketToSendTo.write(bufferSend);
			bufferSend.rewind();
			controller.writeToConsole(client.getUsername() + ": DM to " + clientToSendTo.getUsername());
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void registerUser(String msg, SocketChannel socketChannel)
	{
		ClientData clientToAdd = null;
		Socket socket = socketChannel.socket();
		SocketAddress remoteAddress = socket.getRemoteSocketAddress();
		
		if(msg.contains("\r\n"))
			msg = msg.replace("\r\n", "");
		
		String[] parts = msg.split(" ");
		
		if(parts.length == 2) // Non-Harmony
			clientToAdd = new ClientData(socketChannel, remoteAddress.toString().substring(1), parts[1], getRandomColor(), false);
		
		else if(parts.length == 4) // Harmony
		{
			clientToAdd = new ClientData(socketChannel, remoteAddress.toString().substring(1), parts[1], parts[2].replace("\r\n", ""), true);
			clientToAdd.setIconID(parts[3]);
		}
		
		notInRoom.addClient(clientToAdd);
		
		try
		{
			msg = "One, Two, Three";
			ByteBuffer sendBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
			socketChannel.write(sendBuffer);
			sendBuffer.rewind();
			controller.writeToConsole(clientToAdd.getUsername() + ": Has been Registered");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void checkUser(String msg, SocketChannel socketChannel)
	{
		String[] parts = msg.split(" ");
		String username = parts[1];
		
		if(roomOne.findClientByUsername(username) == null && roomTwo.findClientByUsername(username) == null &&
		   roomThree.findClientByUsername(username) == null && notInRoom.findClientByUsername(username) == null)
		{
			msg = "USER_CHECK//No";
		}
		
		else
			msg = "USER_CHECK//Yes";
		
		try
		{
			ByteBuffer bufferSend = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
			socketChannel.write(bufferSend);
			bufferSend.rewind();
			controller.writeToConsole(username + ": Did a User Check");
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void dmCheckUser(String msg, SocketChannel socketChannel)
	{
		String[] parts = msg.split(" ");
		String username = parts[1];
		ClientData clientToDM = roomOne.findClientByUsername(username);
		
		if(clientToDM == null)
			clientToDM = roomTwo.findClientByUsername(username);
			
		if(clientToDM == null)
			clientToDM = roomThree.findClientByUsername(username);
		
		if(clientToDM == null)
			clientToDM = notInRoom.findClientByUsername(username);
		
		if(clientToDM == null)
		{
			msg = "DM_USER_CHECK//No";
		}
		
		else
			msg = "DM_USER_CHECK//Yes//" + username + "//" + clientToDM.getIconID() + "//" + clientToDM.getColor();
		
		try
		{
			ByteBuffer bufferSend = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
			socketChannel.write(bufferSend);
			bufferSend.rewind();
			controller.writeToConsole("Did a DM User Check on: " + username);
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void updateUser(String msg, SocketChannel socketChannel)
	{
		Room curr = findWhichRoom(socketChannel);
		ClientData client = curr.findClientBySocket(socketChannel);
		
		if(!client.isHarmony())
			return;
		
		String[] parsedMsg = msg.split(" ");
		
		int roomNum = findRoomNum(curr);
		
		switch(roomNum)
		{
			case -1:
				notInRoom.updateColor(socketChannel, parsedMsg[1]);
				notInRoom.updateIconID(socketChannel, parsedMsg[2]);
				break;
				
			case 1:
				roomOne.updateColor(socketChannel, parsedMsg[1]);
				roomOne.updateIconID(socketChannel, parsedMsg[2]);
				break;
				
			case 2:
				roomTwo.updateColor(socketChannel, parsedMsg[1]);
				roomTwo.updateIconID(socketChannel, parsedMsg[2]);
				break;
				
			case 3:
				roomThree.updateColor(socketChannel, parsedMsg[1]);
				roomThree.updateIconID(socketChannel, parsedMsg[2]);
				break;
		}
	}

	private static void sendMsg(byte[] msg, Room curr, ClientData sender)
	{
		try
		{
			if(curr == null)
				return;
			
			else if(curr.equals(privateRoom))
			{
				sendPrivateChatMsg(msg, sender);
				return;
			}
			
			controller.writeToConsole("Message: " + new String(msg, StandardCharsets.UTF_8));
			
			byte[] ogMsg = msg;
			
			for(SelectionKey key : selector.keys())
			{
				if(key.isValid() && key.channel() instanceof SocketChannel)
				{
					boolean send = false;
					
					SocketChannel socketChannel = (SocketChannel) key.channel();
					Room recieverRoom = findWhichRoom(socketChannel);
					ClientData reciever = recieverRoom.findClientBySocket(socketChannel);
					
					if(recieverRoom.equals(notInRoom))
						continue;
					
					else if(recieverRoom.equals(privateRoom))
					{
						String recieveFrom = privateChat.get(reciever.getUsername());
						
						if(recieveFrom == null || sender.getUsername().compareTo(recieveFrom) != 0)
							continue;
					}
					
					if(!reciever.isHarmony() && recieverRoom.equals(curr)) // Is in room and isn't a Harmony Client
						send = true;
					
					else if(reciever.isHarmony()) // Is Harmony
					{
						int roomNum = findRoomNum(curr);
							
						if(roomNum != -1)
						{
							String fixedMsg = new String(ogMsg, StandardCharsets.UTF_8);
							if(fixedMsg.contains("\r\n"))
								fixedMsg = fixedMsg.replace("\r\n", "");
							
							String newMsg = sender.getUsername() + "//" + sender.getColor() + "//" + sender.getIconID() + "//" + fixedMsg + "//" + findRoomNum(curr);
							msg = newMsg.getBytes(StandardCharsets.UTF_8);
							send = true;
						}
					}
					
					if(send)
					{
						ByteBuffer buffer = ByteBuffer.wrap(msg);
						socketChannel.write(buffer);
						buffer.rewind();
						controller.writeToConsole(reciever.getUsername() + ": Msg Sent");
					}
				}
			}
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void sendPrivateChatMsg(byte[] msg, ClientData sender)
	{
		String personToSendTo = privateChat.get(sender.getUsername());
		
		if(personToSendTo == null)
			return;
		
		int roomToSendTo = 1;
		ClientData clientToSendTo = roomOne.findClientByUsername(personToSendTo);
		
		if(clientToSendTo == null)
		{
			clientToSendTo = roomTwo.findClientByUsername(personToSendTo);
			roomToSendTo = 1;
		}
		
		if(clientToSendTo == null)
		{
			clientToSendTo = roomThree.findClientByUsername(personToSendTo);
			roomToSendTo = 2;
		}
		
		if(clientToSendTo == null)
		{
			clientToSendTo = privateRoom.findClientByUsername(personToSendTo);
			roomToSendTo = 3;
		}
		
		if(clientToSendTo == null)
			return;
		
		if(clientToSendTo.isHarmony())
		{
			String fixedMsg = new String(msg, StandardCharsets.UTF_8);
			if(fixedMsg.contains("\r\n"))
				fixedMsg = fixedMsg.replace("\r\n", "");
			
			String newMsg = sender.getUsername() + "//" + sender.getColor() + "//" + sender.getIconID() + "//" + fixedMsg + "//" + roomToSendTo;
			msg = newMsg.getBytes(StandardCharsets.UTF_8);
		}
		
		try
		{
			ByteBuffer buffer = ByteBuffer.wrap(msg);
			clientToSendTo.getSocketChannel().write(buffer);
			buffer.rewind();
			
			sender.getSocketChannel().write(buffer);
			buffer.rewind();
			controller.writeToConsole(sender.getUsername() + " Sent a message to " + clientToSendTo.getUsername());
		}
		
		catch(Exception e)
		{
			controller.writeToConsole(e.getMessage());
		}
	}
	
	private static void sendMsgToHarmonyClients(byte[] msg)
	{
		try
		{			
			controller.writeToConsole("Message: " + new String(msg, StandardCharsets.UTF_8));
			
			for(SelectionKey key : selector.keys())
			{
				if(key.isValid() && key.channel() instanceof SocketChannel)
				{
					SocketChannel socketChannel = (SocketChannel) key.channel();
					Room clientRoom = findWhichRoom(socketChannel);
					ClientData client = null;
					
					try
					{
						client = clientRoom.findClientBySocket(socketChannel);
					}
					
					catch(Exception e)
					{
						continue;
					}
					
					if(clientRoom.equals(notInRoom))
						continue;
					
					if(client.isHarmony())
					{
						ByteBuffer buffer = ByteBuffer.wrap(msg);
						socketChannel.write(buffer);
						buffer.rewind();
						controller.writeToConsole(client.getUsername() + ": Msg Sent");
					}
				}
			}
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static Room findWhichRoom(SocketChannel socketChannel)
	{
		if(roomOne.containsClientBySocketChannel(socketChannel))
			return roomOne;
		
		else if(roomTwo.containsClientBySocketChannel(socketChannel))
			return roomTwo;
		
		else if(roomThree.containsClientBySocketChannel(socketChannel))
			return roomThree;
		
		else if(notInRoom.containsClientBySocketChannel(socketChannel))
			return notInRoom;
		
		else if(privateRoom.containsClientBySocketChannel(socketChannel))
			return privateRoom;
		
		return null;
	}
	
	private static int findRoomNum(Room room)
	{
		int roomNum = -1;
		if(room.equals(roomOne))
			roomNum = 1;
		
		else if(room.equals(roomTwo))
			roomNum = 2;
		
		else if(room.equals(roomThree))
			roomNum = 3;
		
		else if(room.equals(privateRoom))
			roomNum = 4;
		
		return roomNum;
	}
	
	private static String getRandomColor()
	{
		Random r = new Random();
		Color userColor = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		
		String hex1, hex2;
		
		hex1 = Integer.toHexString(userColor.hashCode()).toUpperCase();
		
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
		
		    return "#" + hex2;
	}
}
