package application;

import java.sql.Connection;
import java.sql.DriverManager;

public class sqliteConnection
{
	public static Connection dbConnector()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			Connection connect = DriverManager.getConnection("jdbc:sqlite:" + sqliteConnection.class.getResource("/application/UserDatabase.sqlite"));
			
			return connect;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
