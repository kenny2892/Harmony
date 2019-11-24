package server_application;

import java.nio.channels.SocketChannel;

public class ClientData
{
	private SocketChannel socketChannel;
	private String username, ip, color, iconID;
	private boolean isHarmony;

	public ClientData(SocketChannel socketChannel, String ip, String username, String color, boolean isHarmony)
	{		
		if(ip == null)
			throw new IllegalArgumentException("Null ip.");
		
		else if(socketChannel == null)
			throw new IllegalArgumentException("Null socket.");
		
		else if(username == null)
			throw new IllegalArgumentException("Null username.");
		
		else if(color == null)
			throw new IllegalArgumentException("Null userRequest.");

		this.socketChannel = socketChannel;
		this.username = username;
		this.color = color;
		this.ip = ip;
		this.isHarmony = isHarmony;
		this.iconID = "";
	}
	
	public SocketChannel getSocketChannel()
	{
		return socketChannel;
	}

	public String getUsername()
	{
		return username;
	}

	public String getColor()
	{
		return color;
	}
	
	public void setColor(String color)
	{
		if(color == null)
			throw new IllegalArgumentException("Null color.");
		
		this.color = color;
	}

	public String getIp()
	{
		return ip;
	}
	
	public boolean isHarmony()
	{
		return isHarmony;
	}

	public String getIconID()
	{
		return iconID;
	}
	
	public void setIconID(String iconID)
	{
		if(iconID == null)
			throw new IllegalArgumentException("Null iconID.");
		
		this.iconID = iconID;
	}
}
