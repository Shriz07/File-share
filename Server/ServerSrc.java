package application;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/** //Class that represents all data associated with server */

public class ServerSrc 
{
	private ServerSocket serverSocket; //Socket on which server is running
	private int UsersNum; //Number of clients connected
	private Map<String, ClientMenager> clients = new HashMap<>();//Hashmap with client login and their client managers
	private String path; //Path of server directory
	private Thread clientConnectionThread; //Trhead to accept connections
	private ServerAppController controller; //Controller of server GUI
	/**
	 * Variable that tells if server is shutting down
	 */
	public int serverShutDown = 0;
	
	/**
	 * Constructor that sets basic data
	 * @param port Number of socket
	 * @param controller Controller of server GUI
	 * @param path Path of server directory
	 * @throws IOException Exception when socket failed to open
	 */
	public ServerSrc(int port, ServerAppController controller, String path) throws IOException
	{
		this.controller = controller;
		this.UsersNum = 0;
		this.path = path;
		this.serverSocket = new ServerSocket(port);
	}
	
	/**
	 * Method that creates thread to accept incomming connections
	 */
	public void EnableConnection()
	{
		clientConnectionThread = new Thread(new AcceptConnection(this, serverSocket, controller));
		clientConnectionThread.start();
	}
	
	/**
	 * Method that returns map of clients
	 * @return Method that return map of clients
	 */
	public Map<String, ClientMenager> getClients()
	{
		return clients;
	}
	
	/**
	 * Method that checks if user with given login already exists
	 * @param login Login of user to be checked
	 * @return 1 if yes, 0 if not
	 */
	public int checkIfLoginExists(String login)
	{
		int result = 0;
		for(Map.Entry<String, ClientMenager> entry : clients.entrySet())
		{
			if(entry.getKey().equals(login))
			{
				result = 1;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Method that adds users with given login and manager to map
	 * @param login Login of user
	 * @param menager Manager of user
	 */
	public void addUser(String login, ClientMenager menager)
	{
		UsersNum++;
		clients.put(login, menager);
	}
	
	/**
	 * Method that deletes user with given login from map
	 * @param login Login of user to be deleted
	 */
	public void deleteUser(String login)
	{
		clients.remove(login);
	}
	
	/**
	 * Method that sends given login to every user except user with the same login
	 * @param login Login of source client
	 */
	public void sendLoginToEveryone(String login)
	{
		for(Map.Entry<String, ClientMenager> entry : clients.entrySet())
		{
			if(!(entry.getKey().equals(login)))
			{
				ClientMenager client = entry.getValue();
				client.sendLoginToUser(login);
			}
		}
	}
	
	/**
	 * Method that sends information to all clients that user with given login logged out
	 * @param login Login of user that logged out
	 */
	public void sendDeleteLoginToEveryone(String login)
	{
		for(Map.Entry<String, ClientMenager> entry : clients.entrySet())
		{
			if(!(entry.getKey().equals(login)))
			{
				ClientMenager client = entry.getValue();
				client.sendLogoutToUser(login);
			}
		}
	}
	
	/**
	 * Method that returns data output stream of user with given login
	 * @param login Login of user
	 * @return Data output stream of given user
	 */
	public DataOutputStream reciveUserOutputStream(String login)
	{
		for(Map.Entry<String, ClientMenager> entry : clients.entrySet())
		{
			if(entry.getKey().equals(login))
			{
				ClientMenager client = entry.getValue();
				return client.dataOut;
			}
		}
		return null;
	}
	
	/**
	 * Method that returns data input stream of user with given login
	 * @param login Login of user
	 * @return Data input stream
	 */
	public DataInputStream reciveUserInputStream(String login)
	{
		for(Map.Entry<String, ClientMenager> entry : clients.entrySet())
		{
			if(entry.getKey().equals(login))
			{
				ClientMenager client = entry.getValue();
				return client.dataIn;
			}
		}
		return null;
	}
	
	/**
	 * Method that returns number of users
	 * @return Number of users
	 */
	public int getUsers()
	{
		return UsersNum;
	}
	
	/**
	 * Method that returns path of server directory
	 * @return Path of server directory
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Method that  substract one user form user counter
	 */
	public void deleteUser()
	{
		UsersNum--;
	}
	
	/**
	 * Method that shuts down server
	 */
	public void shutDown()
	{
		try
		{
			serverSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		while(serverShutDown == 0);
		clientConnectionThread.interrupt();
		try
		{
			serverSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
