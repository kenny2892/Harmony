package client_application;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Controller
{
	@FXML private Group chatDisplay;
	@FXML private Group startDisplay;
	@FXML private TextField ipAddressField;
	@FXML private TextField portField;
	@FXML private ImageView startRoomIcon;
	@FXML private Line selectedRoomLine;
	@FXML private TextFlow mainTextFlow;
	@FXML private ScrollPane mainTxtScrollPane;
	@FXML private TextArea enterMsgTextArea;
	@FXML private Group loginScreen;
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private Text incorrectPassword;
	@FXML private Text badConnection;
	@FXML private Text accountInUse;

	@FXML private Rectangle closeHitBox;
	@FXML private Rectangle minimizeHitBox;
	@FXML private Rectangle closeBG;
	@FXML private Rectangle minimizeBG;
	@FXML private Line closeIcon1;
	@FXML private Line closeIcon2;
	@FXML private Line minimizeIcon;
	@FXML private Rectangle topBar;

	private String serverAddress;
	private int serverPort;
	private int roomNum;

	public void initialize()
	{
		enterMsgSetUp();
		startup();
		mainTxtScrollPane.vvalueProperty().bind(mainTextFlow.heightProperty());
	}

	public void clickStartRoom()
	{
		startup();

		if (roomNum != 0)
		{
			Main.clearRoom();
			Main.leaveServer();
		}

		roomNum = 0;
		peopleTextFlow.getChildren().clear();
	}

	public void startup()
	{
		startDisplay.setVisible(true);
		chatDisplay.setVisible(false);
		selectedRoomLine.setLayoutX(104);
		selectedRoomLine.setLayoutY(38);
		menuControls();
	}

	public void connect()
	{
		serverAddress = ipAddressField.getText();
		String portAsStr = portField.getText();

		if (serverAddress.length() == 0 || portAsStr.length() == 0)
		{
			badConnection.setVisible(true);
			return;
		}

		serverPort = Integer.parseInt(portField.getText());

		if (!Main.startConnection(serverAddress, serverPort))
		{
			badConnection.setVisible(true);
			return;
		}

		badConnection.setVisible(false);
		loginScreen.setVisible(true);

		checkForMsgs();
	}

	public void signIn()
	{
		if (!checkAccountDetails())
			return;

		Main.sendMsg("CHECK_IF_IN_USE//" + usernameField.getText());
	}

	private boolean joinRoom()
	{
		if (Main.getUsername() == null)
			return false;

		if (roomNum != 0)
		{
			Main.clearRoom();
			Main.leaveServer();

			try
			{
				Thread.sleep(500);
			}

			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		accountInUse.setVisible(false);
		incorrectPassword.setVisible(false);
		badConnection.setVisible(false);
		loginScreen.setVisible(false);
		startDisplay.setVisible(false);
		chatDisplay.setVisible(true);

		peopleTextFlow.getChildren().clear();
		mainTextFlow.getChildren().clear();
		return true;
	}

	public void clickRoomOne()
	{
		if (!joinRoom())
			return;

		Main.joinServer(1);
		selectedRoomLine.setLayoutY(138);

		if (Main.getRoomOne() != null)
			mainTextFlow.getChildren().addAll(Main.getRoomOne());

		roomNum = 1;
	}

	public void clickRoomTwo()
	{
		if (!joinRoom())
			return;

		Main.joinServer(2);
		selectedRoomLine.setLayoutY(238);

		if (Main.getRoomTwo() != null)
			mainTextFlow.getChildren().addAll(Main.getRoomTwo());

		roomNum = 2;
	}

	public void clickRoomThree()
	{
		if (!joinRoom())
			return;

		Main.joinServer(3);
		selectedRoomLine.setLayoutY(338);

		if (Main.getRoomThree() != null)
			mainTextFlow.getChildren().addAll(Main.getRoomThree());

		roomNum = 3;
	}

	private void enterMsgSetUp()
	{
		enterMsgTextArea.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent keyEvent)
			{
				String msg = enterMsgTextArea.getText();

				if (keyEvent.getCode() == KeyCode.ENTER)
				{
					if (msg.contains("\n"))
						msg = msg.replace("\n", "");

					if (msg == null || msg.length() <= 0)
					{
						enterMsgTextArea.clear();
						return;
					}

					Main.sendMsg(roomNum + "//" + msg); // TODO Add cmd initializers
				}

				else if (msg.length() > Main.CHAR_LIMIT)
					enterMsgTextArea.setText(msg.substring(0, 120));
			}
		});
	}

	private void checkForMsgs()
	{
		new Thread(() ->
		{
			try
			{
				InputStream input = Main.getSocket().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));

				while(!Main.getSocket().isClosed())
				{
					String msg = reader.readLine();
					Platform.runLater(() ->
					{
						displayMsg(msg);
					});
				}
			}
			catch(SocketException ex)
			{
				System.out.println("Socket has been closed.");
			}

			catch(Exception e)
			{
				e.printStackTrace();
			}
		}).start();
	}

	@FXML private TextFlow peopleTextFlow;

	private void addUser(String msg)
	{
		String[] parsedMsg = msg.split("//"); // USER//username//#color//room
		int room = 0;

		try
		{
			room = Integer.parseInt(parsedMsg[3]);
		}

		catch(Exception e)
		{
			return;
		}

		if (Main.isInRoom(parsedMsg[1]) || roomNum != room)
			return;

		String user = parsedMsg[1];

		for(int i = user.length(); i < 25; i++)
			user += " ";

		Node icon = getUserIcon(parsedMsg[1], parsedMsg[2]);
		TextFlow iconFlow = new TextFlow(new Text("\n   "), icon);

		Text username = new Text(user);
		username.setFill(Color.web(parsedMsg[2]));
		username.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		TextFlow userFlow = new TextFlow(new Text("\n   "), username);

		peopleTextFlow.getChildren().addAll(iconFlow, userFlow);
		Main.addUserToRoom(parsedMsg[1]);
	}

	private void removeUser(String msg)
	{
		String userToRemove = msg.substring(8);

		if (!Main.isInRoom(userToRemove))
			return;

		int indexToRemove = Main.getUserIndex(userToRemove) * 2;

		peopleTextFlow.getChildren().remove(indexToRemove); // Remove Pic
		peopleTextFlow.getChildren().remove(indexToRemove); // Remove Name
		Main.removeUserInRoom(indexToRemove / 2);
	}

	private String dm(String msg)
	{
		String[] parsedMsg = msg.split("//");

		String user = Main.getUsername();
		if (user != null && parsedMsg[1].compareTo(user) != 0)
			return null;

		else if (msg.startsWith("DM//USER_CHECK//"))
		{
			if (parsedMsg[2].compareTo("Yes") == 0)
				accountInUse.setVisible(true);

			else
			{
				loginScreen.setVisible(false);
				Main.setUsername(usernameField.getText());
			}

			return null;
		}

		else if (parsedMsg[3].compareTo("LIST") == 0) // DM//username//LIST//MSG//Harmony//#F7931E//
			return parsedMsg[3] + "//" + parsedMsg[4] + "//" + parsedMsg[5] + "//" + Main.getUserList();

		return parsedMsg[2] + "//" + parsedMsg[3] + "//" + parsedMsg[4] + "//" + parsedMsg[5]; // DM//USERNAME_TO_RECEIVE//MSG//USERNAME_OF_SENDER//COLOR_OF_SENDER//MESSAGE
	}

	private void displayMsg(String msg)
	{
		if (msg.startsWith("USER//"))
		{
			addUser(msg);
			return;
		}

		else if (msg.startsWith("CLOSED//"))
		{
			removeUser(msg);
			return;
		}

		else if (msg.startsWith("DM//"))
		{
			String result = dm(msg);

			if (result == null)
				return;

			msg = result;
		}

		String[] parsedMsg = msg.split("//"); // MSG//username_of_sender//#user_color//room_num//msg

		Node icon = getUserIcon(parsedMsg[1], parsedMsg[2]);

		Date curr = new Date();
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		Text date = new Text(dateFormat.format(curr) + "\n");
		date.setStroke(Color.web("#9b9fa1"));
		date.setFont(Font.font("Arial", FontWeight.THIN, 12));

		Text username = new Text(parsedMsg[1] + " ");
		username.setFill(Color.web(parsedMsg[2]));
		username.setFont(Font.font("Arial", FontWeight.BOLD, 16));

		TextArea txtMsg = new TextArea(parsedMsg[4] + "\n");
		txtMsg.setStyle("-fx-background-color: transparent;-fx-text-inner-color: #d7d5d9;");
		txtMsg.setFont(Font.font("Arial", 14));
		txtMsg.setMaxWidth(653);

		if (parsedMsg[4].length() <= 50)
			txtMsg.setMaxHeight(28.3);

		else if (parsedMsg[4].length() <= 100)
			txtMsg.setMaxHeight(60);

		else
			txtMsg.setMaxHeight(85);

		txtMsg.setWrapText(true);
		txtMsg.setEditable(false);
		ScrollBar scrollBarv = (ScrollBar) txtMsg.lookup(".scroll-bar:vertical");

		if (scrollBarv != null)
			scrollBarv.setDisable(true);

		Line divider = new Line();
		divider.setStartX(-100.0);
		divider.setEndX(620.0);
		divider.setStroke(Color.web("#727373"));

		TextFlow iconFlow = new TextFlow(new Text("\n"), icon);
		TextFlow msgFlow = new TextFlow(new Text("\n     "), username, date, new Text("\n     "), txtMsg);

		int intendedRoom = 0;
		try
		{
			intendedRoom = Integer.parseInt(parsedMsg[3]);
		}

		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}

		switch(intendedRoom)
		{
			case 1:
				Main.addToRoomOne(iconFlow, msgFlow, divider);
				break;

			case 2:
				Main.addToRoomTwo(iconFlow, msgFlow, divider);
				break;

			case 3:
				Main.addToRoomThree(iconFlow, msgFlow, divider);
				break;
		}

		if (intendedRoom == roomNum)
		{
			mainTextFlow.getChildren().addAll(iconFlow, msgFlow, divider);
			enterMsgTextArea.clear();
		}
	}

	private Node getUserIcon(String username, String hexColor)
	{
		URL path = null;
		Node icon = null;

		if (username.compareTo("Harmony") == 0)
			path = getClass().getResource("/resources/System Icon.png");

		else
		{
			path = getClass().getResource("/resources/user icons/" + username + ".jpg");

			if (path == null)
				path = getClass().getResource("/resources/user icons/" + username + ".png");
		}

		if (path != null)
		{
			icon = new ImageView(new Image(path.toExternalForm()));
			icon.setClip(new ImageView(
					new Image(getClass().getResource("/resources/user icons/User Clipping Mask.png").toExternalForm())));
			((ImageView) icon).setFitHeight(50);
			((ImageView) icon).setFitWidth(50);
			((ImageView) icon).setPreserveRatio(true);
		}

		else
		{
			Circle iconCircle = new Circle();
			iconCircle.setFill(Color.web(hexColor));
			iconCircle.setRadius(25);

			icon = iconCircle;
		}

		return icon;
	}

	@SuppressWarnings("resource")
	private boolean checkAccountDetails()
	{
		String user = usernameField.getText();
		String password = passwordField.getText();

		if (user.length() == 0 || password.length() == 0 || user.compareTo("Harmony") == 0)
			return false;

		try
		{
			Connection connect = sqliteConnection.dbConnector();

			String query = "Select * from User_Data Where username=?";
			PreparedStatement pst = connect.prepareStatement(query);
			pst.setString(1, user);

			ResultSet results = pst.executeQuery();
			if (results.next())
			{
				String selectedUser = results.getString("username");
				String selectedPassword = results.getString("password");

				if (selectedUser == null) // Create User
				{
					query = "Insert Into User_Data Values(?, ?)";
					pst = connect.prepareStatement(query);
					pst.setString(1, user);
					pst.setString(2, password);
					pst.executeUpdate();
				}

				else if (password.compareTo(selectedPassword) != 0) // Bad Password
				{
					incorrectPassword.setVisible(true);

					results.close();
					pst.close();
					connect.close();
					return false;
				}
			}

			else
			{
				query = "Insert Into User_Data Values(?, ?)";
				pst = connect.prepareStatement(query);
				pst.setString(1, user);
				pst.setString(2, password);
				pst.executeUpdate();
			}

			results.close();
			pst.close();
			connect.close();

			return true;
		}

		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void menuControls()
	{
		closeHitBox.setOnMouseEntered(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				closeBG.setFill(Color.web("#f04747"));
				closeIcon1.setStroke(Color.WHITE);
				closeIcon2.setStroke(Color.WHITE);
			}
		});

		closeHitBox.setOnMouseExited(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				closeBG.setFill(Color.web("#202225"));
				closeIcon1.setStroke(Color.web("#b9bbbe"));
				closeIcon2.setStroke(Color.web("#b9bbbe"));
			}
		});

		minimizeHitBox.setOnMouseEntered(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				minimizeBG.setFill(Color.web("#282b2e"));
				minimizeIcon.setStroke(Color.WHITE);
			}
		});

		minimizeHitBox.setOnMouseExited(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				minimizeBG.setFill(Color.web("#202225"));
				minimizeIcon.setStroke(Color.web("#b9bbbe"));
			}
		});

		topBar.setOnMousePressed(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				Main.setXOffset(event.getSceneX());
				Main.setYOffset(event.getSceneY());
			}
		});

		topBar.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				Main.setX(event);
				Main.setY(event);
			}
		});
	}

	public void minimizeApp()
	{
		Main.minimizeApp();
	}

	public void closeApp()
	{
		Main.closeApp();
	}
}
