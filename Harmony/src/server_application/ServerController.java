package server_application;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ServerController
{
	@FXML private TextArea consoleWindow;
	@FXML private TextField portField;
	@FXML private Text invalidTxt;
	@FXML private Button connectBtn;
	
	@FXML private Rectangle topBar;
	@FXML private Rectangle closeHitBox;
	@FXML private Rectangle closeBG;
	@FXML private Line closeIcon1;
	@FXML private Line closeIcon2;
	
	public void initialize()
	{
		invalidTxt.setVisible(false);
		menuControls();
	}
	
	public void writeToConsole(String text)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				consoleWindow.appendText(text + "\n");
			}
		});
	}
	
	public void openGitHub()
	{
		SocketServer.openGitHub();
	}
	
	public void connect()
	{
		try
		{
			int port = Integer.parseInt(portField.getText());
			SocketServer.startServer(port);
			connectBtn.setVisible(false);
		}
		
		catch(Exception e)
		{
			invalidTxt.setVisible(true);
		}
	}
	
	private void menuControls()
	{
		topBar.setOnMousePressed(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				SocketServer.setXOffset(event.getSceneX());
				SocketServer.setYOffset(event.getSceneY());
			}
		});

		topBar.setOnMouseDragged(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				SocketServer.setX(event);
				SocketServer.setY(event);
			}
		});
		
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
	}
	
	public void close()
	{
		SocketServer.stopServer();
	}
}
