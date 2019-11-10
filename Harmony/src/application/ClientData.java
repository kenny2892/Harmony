package application;

public class ClientData
{
	private String username, ip;

	public ClientData(String username, String ip)
	{
		if (username == null || ip == null)
			throw new IllegalArgumentException("Null username of IP");

		this.username = username;
		this.ip = ip;
	}

	public String getUsername()
	{
		return username;
	}

	public String getIp()
	{
		return ip;
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
		
		ClientData other = (ClientData) obj;
		if (!username.equals(other.username) && !ip.equals(other.ip))
			return false;
		
		return true;
	}

}
