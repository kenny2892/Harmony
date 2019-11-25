package server_application;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ServerController
{
	@FXML private TextArea consoleWindow;
	@FXML private TextField portField;
	@FXML private Text invalidTxt;
	@FXML private Button connectBtn;
	
	public void initialize()
	{
		invalidTxt.setVisible(false);
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
}
