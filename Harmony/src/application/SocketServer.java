package application;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SocketServer
{
	private Selector selector;
	private Map<SocketChannel, List<byte[]>> dataMap;
	private InetSocketAddress listeningAddress;
	private ArrayList<ClientData> userMap;
	private Map<String, String> userAddRequests;
	
	public static void main(String[] args)
	{
		SocketServer server = new SocketServer("2601:602:c880:2820::8c1d", 8345); // 10.0.0.117
		server.startServer();
	}

	public SocketServer(String address, int port)
	{
		if(address == null)
			throw new IllegalArgumentException("The address param is null");

		listeningAddress = new InetSocketAddress(address, port);
		dataMap = new HashMap<SocketChannel, List<byte[]>>();
		userMap = new ArrayList<ClientData>();
		userAddRequests = new HashMap<String, String>();
	}

	public void startServer()
	{
		try
		{
			System.out.println("Starting Server at: " + listeningAddress.getHostString() + " Port# " + listeningAddress.getPort());
			this.selector = Selector.open();
			
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);

			serverChannel.socket().bind(listeningAddress);
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);

			while(true)
			{
				this.selector.select(); // Wait here until something happens

				Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
				while(keys.hasNext())
				{
					System.out.println("Selecting");
					
					SelectionKey key = (SelectionKey) keys.next();
					keys.remove();
					
					if(key.isValid())
					{
						if(key.isAcceptable())
							this.accept(key);

						else
							if(key.isReadable())
								this.read(key);
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

	private void accept(SelectionKey key)
	{
		try
		{
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			SocketChannel socketChannel = serverChannel.accept();
			socketChannel.configureBlocking(false);

			Socket socket = socketChannel.socket();
			SocketAddress remoteAddress = socket.getRemoteSocketAddress();
			System.out.println("Connected " + remoteAddress);

			dataMap.put(socketChannel, new ArrayList<byte[]>());
			socketChannel.register(this.selector, SelectionKey.OP_READ);
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private void read(SelectionKey key)
	{
		System.out.println("Reading");
		SocketChannel socketChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		String msg = "";

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
			}
		}

		catch(Exception e)
		{
			key.cancel();
			numberRead = -1;
		}
		
		if(numberRead == -1)
		{
			this.dataMap.remove(socketChannel);

			Socket socket = socketChannel.socket();
			SocketAddress remoteAddress = socket.getRemoteSocketAddress();

			ClientData toFind = new ClientData("", remoteAddress.toString().substring(1));
			
			int index = userMap.indexOf(toFind);
			
			if(index < 0)
				return;
			
			ClientData user = userMap.get(index);
			userMap.remove(index);
			userAddRequests.remove(remoteAddress.toString().substring(1));
			
			msg = "CLOSED//" + user.getUsername() + "\n";			
			
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

		else if(msg.startsWith("LEAVING//"))
		{
			String username = msg.substring(9).replace("\r\n", "");
			
			Socket socket = socketChannel.socket();
			SocketAddress remoteAddress = socket.getRemoteSocketAddress();

			userAddRequests.remove(remoteAddress.toString().substring(1));
			
			msg = "CLOSED//" + username + "\n";
		}
		
		else if(msg.startsWith("CHECK_IF_IN_USE//"))
		{
			Socket socket = socketChannel.socket();
			SocketAddress remoteAddress = socket.getRemoteSocketAddress();
			
			String username = msg.substring(17).replace("\r\n", "");			
			ClientData toFind = new ClientData(username, "");
			
			if(userMap.indexOf(toFind) < 0) // Account not in use
			{
				userMap.add(new ClientData(username, remoteAddress.toString().substring(1)));
				
				msg = "DM//USER_CHECK//No\n";
			}
			
			else
				msg = "DM//USER_CHECK//Yes\n";
		}
		
		else if(msg.startsWith("USER//"))
		{
			Socket socket = socketChannel.socket();
			SocketAddress remoteAddress = socket.getRemoteSocketAddress();
			
			for(Map.Entry<String, String> entry : userAddRequests.entrySet())
				sendMsg(entry.getValue().getBytes(StandardCharsets.UTF_8));
			
			userAddRequests.put(remoteAddress.toString().substring(1), msg);
		}
		
		else if(msg.startsWith("LIST//")) // TOD Add room check / Change to be client written
		{
			String username = msg.substring(6);
			
			msg = "DM//" + username + "//MSG//Harmony//#F7931E//";
			for(Map.Entry<String, String> entry : userAddRequests.entrySet())
				msg += entry.getValue()+ ", ";
			
			msg = msg.substring(0, msg.length() - 1) + "\n";
		}
			
		sendMsg(msg.getBytes(StandardCharsets.UTF_8));
	}

	private void sendMsg(byte[] msg)
	{
		try
		{
			ByteBuffer buffer = ByteBuffer.wrap(msg);
			System.out.println("Message: " + new String(msg, StandardCharsets.UTF_8));
			for(SelectionKey key : selector.keys())
			{
				if(key.isValid() && key.channel() instanceof SocketChannel)
				{
					SocketChannel socketChannel = (SocketChannel) key.channel();
					socketChannel.write(buffer);
					buffer.rewind();
					System.out.println("Msg sent");
				}
			}
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
