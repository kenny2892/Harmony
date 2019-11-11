package client_application;

import java.sql.Connection;
import java.sql.DriverManager;

public class sqliteConnection
{
	public static Connection dbConnector()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			Connection connect = DriverManager.getConnection("jdbc:sqlite::resource:" + sqliteConnection.class.getResource("/client_application/UserDatabase.sqlite"));
			
			return connect;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
