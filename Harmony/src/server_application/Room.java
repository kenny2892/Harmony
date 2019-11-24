package server_application;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Room 
{
	private ArrayList<ClientData> clients;
	
	public Room()
	{
		clients = new ArrayList<ClientData>();
	}
	
	public ClientData findClientByUsername(String usernameToFind)
	{
		for(int i = 0; i < clients.size(); i++)
		{
			ClientData client = clients.get(i);
			
			if(usernameToFind.compareTo(client.getUsername()) == 0)
				return client;
		}
		
		return null;
	}
	
	public ClientData findClientBySocket(SocketChannel socketChannel)
	{
		for(int i = 0; i < clients.size(); i++)
		{
			ClientData client = clients.get(i);
			
			if(socketChannel.equals(client.getSocketChannel()))
				return client;
		}
		
		return null;
	}
	
	public boolean addClient(ClientData client)
	{
		if(client == null)
			throw new IllegalArgumentException("Client is null.");
		
		boolean canAdd = true;
		for(ClientData toCheck : clients)
		{
			if(toCheck.getIp().compareTo(client.getIp()) == 0)
				canAdd = false;
			
			if(toCheck.getUsername().compareTo(client.getUsername()) == 0)
				canAdd = false;
			
			if(toCheck.getSocketChannel().equals(client.getSocketChannel()))
				canAdd = false;
			
			if(!canAdd)
				break;
		}

		if(canAdd)
			clients.add(client);
		
		return canAdd;
	}
	
	public boolean removeClient(String username)
	{
		if(username == null)
			throw new IllegalArgumentException("Username is null.");
		
		int index = -1;
		
		for(int i = 0; i < clients.size(); i++)
		{
			ClientData client = clients.get(i);
			
			if(username.compareTo(client.getUsername()) == 0)
			{
				index = i;
				break;
			}
		}
		
		if(index > -1)
		{
			clients.remove(index);
			return true;
		}
		
		return false;
	}
	
	public boolean removeClient(SocketChannel socketChannel)
	{
		if(socketChannel == null)
			throw new IllegalArgumentException("Socket Channel is null.");
		
		int index  = -1;
		
		for(int i = 0; i < clients.size(); i++)
		{
			ClientData client = clients.get(i);
			
			if(socketChannel.equals(client.getSocketChannel()))
			{
				index = i;
				break;
			}
		}
		
		if(index > -1)
		{
			clients.remove(index);
			return true;
		}
		
		return false;
	}
	
	public boolean containsClientBySocketChannel(SocketChannel socketChannel)
	{
		return findClientBySocket(socketChannel) != null;
	}
	
	public ArrayList<ClientData> getClientList()
	{
		return clients;
	}
	
	public void updateColor(SocketChannel socketChannel, String color)
	{
		if(socketChannel == null)
			throw new IllegalArgumentException("Socket Channel is null.");
		
		else if(color == null)
			throw new IllegalArgumentException("Color is null.");
		
		for(ClientData client : clients)
		{
			if(client.getSocketChannel().equals(socketChannel))
			{
				client.setColor(color);
				break;
			}
		}
	}
	
	public void updateIconID(SocketChannel socketChannel, String iconID)
	{
		if(socketChannel == null)
			throw new IllegalArgumentException("Socket Channel is null.");
		
		else if(iconID == null)
			throw new IllegalArgumentException("iconID is null.");
		
		for(ClientData client : clients)
		{
			if(client.getSocketChannel().equals(socketChannel))
			{
				client.setIconID(iconID);
				break;
			}
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Room other = (Room) obj;
		if (clients == null)
		{
			if (other.clients != null)
				return false;
		}
		
		else if (!clients.equals(other.clients))
			return false;
		
		return true;
	}	
}
