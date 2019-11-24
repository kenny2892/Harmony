package client_application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import client_application.Main.StartMode;
import client_application.Main.TitleMode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
	@FXML private TextFlow peopleTextFlow;
	@FXML private Pane fileTransferPanel;
	@FXML private ComboBox<String> fileUserSelect;
	@FXML private Text msgCountTxt;
	
	@FXML private Group serverScreen;
	@FXML private Group loginScreen;
	@FXML private Group signedInScreen;
	@FXML private Pane titlePanel;
	@FXML private Pane settingsPanel;
	@FXML private Rectangle titleClicked;
	@FXML private Rectangle titleHover;
	@FXML private Rectangle settingsClicked;
	@FXML private Rectangle settingsHover;
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private Text incorrectPassword;
	@FXML private Text badConnection;
	@FXML private Text accountInUse;
	@FXML private Text filePathTxt;

	@FXML private Rectangle closeHitBox;
	@FXML private Rectangle minimizeHitBox;
	@FXML private Rectangle closeBG;
	@FXML private Rectangle minimizeBG;
	@FXML private Line closeIcon1;
	@FXML private Line closeIcon2;
	@FXML private Line minimizeIcon;
	@FXML private Rectangle topBar;
	
	@FXML private Text iconIndexText;
	@FXML private ColorPicker userColorPicker;
	@FXML private Pane userIconPane;
	@FXML private Rectangle settingsHitBox;
	@FXML private Rectangle titleHitBox;

	private String serverAddress;
	private int serverPort;
	private int roomNum;
	private String[] roomNames;
	
	public void initialize()
	{
		enterMsgSetUp();
		startup();
		roomNames = new String[3];
		mainTxtScrollPane.vvalueProperty().bind(mainTextFlow.heightProperty());
	}

	public void clickStartRoom()
	{
		selectedRoomLine.setLayoutX(104);
		selectedRoomLine.setLayoutY(38);
		
		titleClicked.setVisible(false);
		titleHitBox.setVisible(false);
		settingsClicked.setVisible(false);
		settingsHitBox.setVisible(false);
		
		showStartDisplay();

		if (roomNum != 0)
		{
			Main.clearRoom();
			Main.sendMsg("\\e -1");
		}

		roomNum = 0;
		
		peopleTextFlow.getChildren().clear();
	}

	public void startup()
	{
		selectedRoomLine.setLayoutX(104);
		selectedRoomLine.setLayoutY(38);
		menuControls();
		chatDisplay.setVisible(false);
		startDisplay.setVisible(true);
		
		showStartDisplay();
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
		serverScreen.setVisible(false);
		
		Main.setTitleMode(TitleMode.LOGIN);

		checkForMsgs();
		userColorPicker.setValue(Color.web(Main.getHexColor()));
	}

	public void signIn()
	{
		if (!checkAccountDetails())
			return;

		String username = usernameField.getText();
		if(username.contains(" "))
			username.replace(" ", "_");
			
		Main.sendMsg("\\checkUser " + username + " " + Main.getHexColor());
	}

	private boolean joinRoom(int num)
	{
		if (Main.getUsername() == null)
			return false;

		roomNum = num;
		
		String roomName = "";
		switch(num)
		{
			case 1:
				roomName = roomNames[0];
				break;

			case 2:
				roomName = roomNames[1];
				break;

			case 3:
				roomName = roomNames[2];
				break;
		}
		
		if (roomNum > 0)
		{
			Main.clearRoom();
			Main.sendMsg("\\e " + roomName);

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
		fileTransferPanel.setVisible(false);

		peopleTextFlow.getChildren().clear();
		mainTextFlow.getChildren().clear();
		
		startRoomIcon.setImage(new Image(getClass().getResource("/resources/room icons/Start_Room.png").toExternalForm()));
		msgCountTxt.setText("Message Count: " + Main.getRoomMsgCount(this.roomNum));
		return true;
	}
	
	public void showStartDisplay()
	{
		startDisplay.setVisible(true);
		chatDisplay.setVisible(false);
		
		if(Main.getStartMode() == StartMode.TITLE || Main.getUsername() == null)
			titleMenu();
		
		else
			settingsMenu();
		
		startRoomIcon.setImage(new Image(getClass().getResource("/resources/room icons/Start_Room_Highlight.png").toExternalForm()));
	}

	public void clickRoomOne()
	{
		if (!joinRoom(1))
			return;

		selectedRoomLine.setLayoutY(138);

		if (Main.getRoomOne() != null)
			mainTextFlow.getChildren().addAll(Main.getRoomOne());

		roomNum = 1;
	}

	public void clickRoomTwo()
	{
		if (!joinRoom(2))
			return;

		selectedRoomLine.setLayoutY(238);

		if (Main.getRoomTwo() != null)
			mainTextFlow.getChildren().addAll(Main.getRoomTwo());

		roomNum = 2;
	}

	public void clickRoomThree()
	{
		if (!joinRoom(3))
			return;

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
					
					Main.sendMsg(msg);
					enterMsgTextArea.clear();
					
					if(msg.startsWith("\\x"))
					{
						try
						{
							Thread.sleep(500);
						}
						
						catch(InterruptedException e)
						{
							e.printStackTrace();
						}
						
						closeApp();
					}
				}

				else if (msg.length() > Main.CHAR_LIMIT)
					enterMsgTextArea.setText(msg.substring(0, Main.CHAR_LIMIT));
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
				
				while(!Main.getSocket().isClosed())
				{
					byte[] buffer = new byte[2048];
					int count = 0;
					
					try
					{
						count = input.read(buffer);
					}
					
					catch(Exception e)
					{
						break;
					}
					
					String msg = new String(buffer, StandardCharsets.UTF_8).substring(0, count);
					
					if(msg.startsWith("\\f"))
					{
						String[] parts = msg.split(" ");  // \f reciever color iconID file_name length
						
						try
						{
							String fileName = "";
							for(int i = 4; i < parts.length - 1; i++)
								fileName += parts[i] + " ";
							
							fileName = fileName.substring(0, fileName.length() - 1);
							
							String extension = fileName.substring(fileName.lastIndexOf("."));
							fileName = fileName.substring(0, fileName.lastIndexOf("."));
							
							File fileToDownload = new File(Main.getDownloadDirPath() + "/" + fileName + extension);
							
							int num = 0;
							while(fileToDownload.exists())
							{
								fileToDownload = new File(Main.getDownloadDirPath() + "/" + fileName + num + extension);
								num++;
							}
							
							fileToDownload.createNewFile();
							
							Thread.sleep(1000);
							OutputStream out = new FileOutputStream(fileToDownload);
							
							int length = Integer.parseInt(parts[parts.length - 1]);
							byte[] fileBuffer = new byte[2048];
							
							int byteCount = 0;
							while((byteCount = input.read(fileBuffer)) > 0)
							{
								out.write(fileBuffer, 0, byteCount);
								length -= byteCount;
								
								if(length <= 0)
									break;
							}
							
							out.close();
							
							String sendMsg = parts[1] + "//" + parts[2] + "//" + parts[3] + "//You Received a New File: " + fileName + "//" + roomNum;
							
							Platform.runLater(() ->
							{
								displayMsg(sendMsg);
							});
						}
						
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					
					else
					{
						Platform.runLater(() ->
						{
							displayMsg(msg);
						});
					}
				}
			}
			catch(SocketException ex)
			{
				System.out.println("Socket has been closed.");
				ex.printStackTrace();
			}

			catch(Exception e)
			{
				e.printStackTrace();
			}
		}).start();
	}
	
	private void addUser(String msg)
	{
		String[] parsedMsg = msg.split("//"); // USER//username//#color//IconID//RoomNum
		String user = parsedMsg[1];
		
		if (Main.isInRoom(user))
			return;
		
		try
		{
			int roomNum = Integer.parseInt(parsedMsg[4]);
			
			if(this.roomNum != roomNum)
				return;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}

		for(int i = user.length(); i < 25; i++)
			user += " ";

		Node icon = getUserIcon(parsedMsg[3], parsedMsg[2], 25);
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

	private void displayMsg(String msg)
	{
		String[] parsedMsg = null;
		
		if(msg.contains("//"))
			parsedMsg = msg.split("//"); // username_of_sender//#user_color//iconID//msg//room_num
		
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

		else if (msg.startsWith("USER_CHECK//")) // USER_CHECK//Answer
		{
			if (parsedMsg[1].compareTo("Yes") == 0)
				accountInUse.setVisible(true);

			else
			{
				String username = usernameField.getText();
				if(username.contains(" "))
					username.replace(" ", "_");
				
				loginScreen.setVisible(false);
				Main.setUsername(username);
				Main.setTitleMode(TitleMode.SIGNED_IN);
				Main.sendMsg("\\u " + username + " " + Main.getHexColor() + " " + Main.getIconID());
			}
			
			return;
		}
		
		Main.msgCountIncrease(this.roomNum);
		
		if(Main.getTotalMsgCount() == 1)
		{
			if(msg.contains(", "))
				parsedMsg = msg.split(", ");
			
			else
				parsedMsg = msg.split(",");
			
			if(parsedMsg.length != 3)
				return;
			
			roomNames[0] = parsedMsg[0];
			roomNames[1] = parsedMsg[1];
			roomNames[2] = parsedMsg[2];
			return;
		}
		
		String usernameOfSender = parsedMsg[0];
		String hexColor = parsedMsg[1];
		String iconID = parsedMsg[2];
		String roomNum = parsedMsg[parsedMsg.length - 1];
		
		msg = "";
		for(int i = 3; i < parsedMsg.length - 1; i++)
			msg += parsedMsg[i] + "//";
		
		msg = msg.substring(0, msg.length() - 2);
		
		Node icon = getUserIcon(iconID, hexColor, 25);

		Date curr = new Date();
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		Text date = new Text("      " + dateFormat.format(curr) + "\n");
		date.setStroke(Color.web("#9b9fa1"));
		date.setFont(Font.font("Arial", FontWeight.THIN, 12));

		Text username = new Text(usernameOfSender);
		username.setFill(Color.web(hexColor));
		username.setFont(Font.font("Arial", FontWeight.BOLD, 16));

		TextArea txtMsg = new TextArea(msg + "\n");
		txtMsg.setStyle("-fx-background-color: transparent;-fx-text-inner-color: #d7d5d9;");
		txtMsg.setFont(Font.font("Arial", 14));
		txtMsg.setMaxWidth(653);

		if (msg.length() <= 50)
			txtMsg.setMaxHeight(45);

		else if (msg.length() <= 100)
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
			intendedRoom = Integer.parseInt(roomNum);
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

		if (intendedRoom == this.roomNum)
		{
			mainTextFlow.getChildren().addAll(iconFlow, msgFlow, divider);
			enterMsgTextArea.clear();
		}
		
		msgCountTxt.setText("Message Count: " + Main.getRoomMsgCount(this.roomNum));
	}

	private Node getUserIcon(String iconStr, String hexColor, int radius)
	{
		URL path = null;
		Node icon = null;
		int iconID = 100;
		
		try
		{
			iconID = Integer.parseInt(iconStr);
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		if(iconID < 1 || iconID > Main.MAX_ICON_ID)
			iconID = 100;

		path = getClass().getResource("/resources/user icons/" + iconID + ".jpg");

		if (path == null)
			path = getClass().getResource("/resources/user icons/" + iconID + ".png");
		
		if(path == null || iconID == 100)
			icon = getDefaultIcon(Color.web(hexColor), radius);

		else if (path != null)
		{
			icon = new ImageView(new Image(path.toExternalForm()));
			
			ImageView mask = new ImageView(new Image(getClass().getResource("/resources/user icons/User Clipping Mask.png").toExternalForm()));
			mask.setFitHeight(radius * 2);
			mask.setFitWidth(radius * 2);
			mask.setPreserveRatio(true);
			
			((ImageView) icon).setFitHeight(radius * 2);
			((ImageView) icon).setFitWidth(radius * 2);
			((ImageView) icon).setPreserveRatio(true);
			icon.setClip(mask);
		}

		return icon;
	}
	
	private Group getDefaultIcon(Color color, int radius)
	{
		ImageView userIcon = new ImageView(new Image(getClass().getResource("/resources/user icons/Default_White.png").toExternalForm()));
		
		ColorAdjust adjust = new ColorAdjust();

		adjust.setBrightness(color.getBrightness());
		adjust.setHue(color.getHue());
		adjust.setSaturation(color.getSaturation());
		adjust.setContrast(1);

		Blend colorize = new Blend(BlendMode.MULTIPLY, adjust,
				new ColorInput(0, 0, userIcon.getImage().getWidth(), userIcon.getImage().getHeight(), color));

		userIcon.setEffect(colorize);

		userIcon.setCache(true);
		userIcon.setCacheHint(CacheHint.SPEED);

		ImageView mask = new ImageView(new Image(getClass().getResource("/resources/user icons/Default_Clipping_Mask.png").toExternalForm()));
		mask.setFitHeight(radius * 2);
		mask.setFitWidth(radius * 2);
		mask.setPreserveRatio(true);
		
		ImageView bg = new ImageView(new Image(getClass().getResource("/resources/user icons/User Clipping Mask.png").toExternalForm()));
		bg.setFitHeight(radius * 2 - 5);
		bg.setFitWidth(radius * 2 - 5);
		bg.setLayoutX(2);
		bg.setLayoutY(2);
		bg.setPreserveRatio(true);
		
		userIcon.setClip(mask);
		userIcon.setFitHeight(radius * 2);
		userIcon.setFitWidth(radius * 2);
		userIcon.setPreserveRatio(true);
		
		return new Group(bg, userIcon);
	}

	@SuppressWarnings("resource")
	private boolean checkAccountDetails()
	{
		String user = usernameField.getText();
		String password = passwordField.getText();

		if (user.length() == 0 || password.length() == 0 || user.compareTo("Harmony") == 0)
			return false;

		else if(user.contains(" "))
			user.replace(" ", "_");
		
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
	
	public void titleMenu()
	{		
		titleClicked.setVisible(false);
		titleHover.setVisible(false);
		settingsClicked.setVisible(false);
		settingsHover.setVisible(false);
		titlePanel.setVisible(false);
		settingsPanel.setVisible(false);
		
		Main.setStartMode(StartMode.TITLE);
		titleClicked.setVisible(true);
		titlePanel.setVisible(true);
		
		serverScreen.setVisible(false);
		loginScreen.setVisible(false);
		signedInScreen.setVisible(false);
		
		switch(Main.getTitleMode())
		{
			case SERVER:
				serverScreen.setVisible(true);
				break;

			case LOGIN:
				loginScreen.setVisible(true);
				break;

			case SIGNED_IN:
				signedInScreen.setVisible(true);
				break;
		}
	}
	
	public void settingsMenu()
	{
		if (Main.getUsername() == null)
			return;
		
		titleClicked.setVisible(false);
		titleHover.setVisible(false);
		settingsClicked.setVisible(false);
		settingsHover.setVisible(false);
		titlePanel.setVisible(false);
		settingsPanel.setVisible(false);

		Main.setStartMode(StartMode.SETTINGS);
		settingsClicked.setVisible(true);
		settingsPanel.setVisible(true);
		
		filePathTxt.setText(Main.getDownloadDirPath());
		updateIconPreview();
	}
	
	public void iconRightArrow()
	{
		if(Main.getIconID() + 1 <= Main.MAX_ICON_ID + 1)
		{
			Main.setIconID(Main.getIconID() + 1);
			updateIconPreview();
		}
	}
	
	public void iconLeftArrow()
	{
		if(Main.getIconID() - 1 >= 1)
		{
			Main.setIconID(Main.getIconID() - 1);
			updateIconPreview();
		}
	}
	
	public void updateIconPreview()
	{
		iconIndexText.setText(Main.getIconID() + "");
		Main.setUserColor(userColorPicker.getValue());
		userIconPane.getChildren().clear();
		userIconPane.getChildren().add(getUserIcon(Main.getIconID() + "", Main.getHexColor(), 51));
		
		Main.sendMsg("\\userUpdate " + Main.getHexColor() + " " + Main.getIconID());
	}
	
	public void startEnter()
	{
		if(roomNum == 0)
			return;
		
		startRoomIcon.setImage(new Image(getClass().getResource("/resources/room icons/Start_Room_Highlight.png").toExternalForm()));
	}
	
	public void startExit()
	{
		if(roomNum == 0)
			return;
		
		startRoomIcon.setImage(new Image(getClass().getResource("/resources/room icons/Start_Room.png").toExternalForm()));
	}
	
	public void titleEnter()
	{
		if(titleClicked.isVisible())
			return;
		
		titleHover.setVisible(true);
	}
	
	public void titleExit()
	{
		if(titleClicked.isVisible())
			return;
		
		titleHover.setVisible(false);
	}
	
	public void settingsEnter()
	{
		if(settingsClicked.isVisible())
			return;
		
		settingsHover.setVisible(true);
	}
	
	public void settingsExit()
	{
		if(settingsClicked.isVisible())
			return;
		
		settingsHover.setVisible(false);
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
	
	public void sendFileBtn()
	{
		ObservableList<String> users = FXCollections.observableArrayList(Main.getUserArray());
		
		fileUserSelect.setItems(users);
		fileTransferPanel.setVisible(true);
	}
	
	public void sendFile()
	{
		if(Main.sendFile(fileUserSelect.getValue()))
			fileTransferPanel.setVisible(false);
		
		// TODO Else
	}
	
	public void selectFileBtn()
	{
		Main.selectFile();
	}
	
	public void cancelFileSend()
	{
		fileTransferPanel.setVisible(false);
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
