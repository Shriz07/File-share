package application;

import java.io.*;
import java.net.*;

import javafx.application.Platform;


/** Class that represents client and all needed data */

public class ClientSrc 
{
	/**
	 * Login of user
	 */
	private String login;
	/**
	 * Path selected by user
	 */
	public String path;
	/**
	 * Name of host
	 */
	private String host;
	/**
	 * Number of port
	 */
	private int port;
	/**
	 * Socket used to communicate
	 */
	private Socket clSocket;
	/**
	 * Controls GUI
	 */
	ClientAppController controller;
	/**
	 * Watches for changes in directory
	 */
	private Thread clientWatcher;
	/**
	 * Thread to recive data from server
	 */
	private static ReciveDataThread reciveDataThread;
	/**
	 * Data input Stream
	 */
	DataInputStream dataIn;
	/**
	 * Data output Stream
	 */
	DataOutputStream dataOut;
	
	/**
	 * Constructor of client data
	 * @param login Name of user
	 * @param path Path selected
	 * @param controller Controller of GUI
	 * @param host Name of host
	 * @param port Number of port on which connection is running
	 */
	public ClientSrc(String login, String path, ClientAppController controller, String host, int port)
	{
		this.login = login;
		this.path = path;
		this.controller = controller;
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Method that returns login
	 * @return login of user
	 */
	public String getLogin()
	{
		return login;
	}
	
	/**
	 * Method that returns path
	 * @return user path
	 */
	public String getPath()
	{
		return path;
	}
	
	/**
	 * Method that changes login
	 * @param login Login of user which wants to change login
	 */
	public void changeLogin(String login)
	{
		this.login = login;
		controller.displayLogin();
	}
	
	//Method that starts connection with server, returns 1 on success and 0 on fail
	/**
	 * Method that starts connection with server
	 * @return 1 on success and 0 on fail
	 */
	public int StartConnection()
	{
		controller.LogAdd("Trying to connect...");
		int result = 0;
		try
		{
			clSocket = new Socket(host, port);
			DataInputStream dataIn = new DataInputStream(clSocket.getInputStream());
			DataOutputStream dataOut = new DataOutputStream(clSocket.getOutputStream());
			this.dataIn = dataIn;
			this.dataOut = dataOut;
			
			dataOut.writeUTF(login);
			String check = dataIn.readUTF();
			if(check.equals("loginError"))
			{
				controller.LogAdd("User with given login already exists");
			}
			else if(check.equals("correctLogin"))
			{
				controller.LogAdd("Connected !");
				controller.refreshFiles();
				controller.LogAdd("File list refreshed");
				controller.LogAdd("SYNCHRONIZING FILES WITH SERVER");
				syncFiles(dataOut);
				controller.LogAdd("SYNCHRONIZATION ENDED");
				
				
				reciveDataThread = new ReciveDataThread(dataIn);
				reciveDataThread.start();
				clientWatcher = new Thread(new ClientWatcher(this, path, controller));
				clientWatcher.start();
				result = 1;
			}
			else
			{
				result = 2;
			}
		}
		catch(ConnectException e)
		{
			controller.LogAdd("Server not found");
			result = 2;
		}
		catch(Exception e)
		{
			controller.LogAdd("Undefined error");
		}
		return result;
	}
	
	/**
	 * Method that synchronizes files with server
	 * @param dataOut Stream to send data
	 * @throws IOException Exception related to communication with server
	 */
	public void syncFiles(DataOutputStream dataOut) throws IOException
	{
		for(File file : new File(this.getPath()).listFiles())
		{
			long size = file.length();
			long modified = file.lastModified();
			String name = file.getName();
			
			dataOut.writeUTF("nextFile");
			dataOut.writeUTF(name);
			dataOut.writeLong(modified);
			dataOut.writeLong(size);
			controller.LogAdd("Sending file with name: " + name + " to server");
			
			FileInputStream stream = new FileInputStream(file);
			for(long i = 0; i < size; i++)
			{
				int data = stream.read();
				dataOut.write(data);
				dataOut.flush();
			}
			stream.close();
		}
		dataOut.writeUTF("end");
	}
	
	/**
	 * Method that sends file to server
	 * @param name Name of file to be sent
	 * @throws IOException Exception related to communication with server
	 */
	public void sendFileToServer(String name) throws IOException
	{
		File file = new File(this.getPath() + '/' + name);
		long size = file.length();
		long modified = file.lastModified();
		
		dataOut.writeUTF("addFile");
		dataOut.writeUTF(name);
		dataOut.writeLong(modified);
		dataOut.writeLong(size);
		Platform.runLater(() -> controller.LogAdd("Sending file with name: " + name + " to server"));
		
		FileInputStream stream = new FileInputStream(file);
		for(long i = 0; i < size; i++)
		{
			int data = stream.read();
			dataOut.write(data);
			dataOut.flush();
		}
		stream.close();
	}
	
	/**
	 * Method that sends file to selected user
	 * @param login Login of user that will recive file
	 * @param fileName Name of file to be sent
	 * @throws IOException Exception related to communication with server
	 */
	public void sendFileToUser(String login, String fileName) throws IOException
	{
			Platform.runLater(() -> controller.LogAdd("Sending file with name: " + fileName + " to user with login: " + login));
			dataOut.writeUTF("sendFile");
			dataOut.writeUTF(login);
			dataOut.writeUTF(fileName);
	}
	
	/**
	 * Method that informs server about file being deleted
	 * @param name Name of file that was deleted
	 */
	public void requestFileDeleteToServer(String name)
	{
		try
		{
			Platform.runLater(() -> controller.LogAdd("Sending information to server that file: " + name + " was deleted"));
			dataOut.writeUTF("deleteFile");
			dataOut.writeUTF(name);
		}
		catch(Exception e)
		{
			Platform.runLater(() -> controller.LogAdd("Failed to send information"));
		}
	}
	
	/**
	 * Method that stops connection with server
	 */
	public void StopConnection()
	{
		if(clSocket.isConnected() == true)
		{
			try
			{
				dataOut.writeUTF("logout");
				dataOut.writeUTF(login);
			}
			catch(Exception  e)
			{
				System.out.println("Server is not available");
			}
		}
		try
		{
			clientWatcher.interrupt();
			reciveDataThread.interrupt();
		}
		catch(Exception e)
		{
			System.out.println("Thread is not running");
		}
	}
	
	/**
	 * Thread that listen to server and react to recived commands
	 */
	private class ReciveDataThread extends Thread
	{
		private DataInputStream dataIn; //Data input stream
		
		/**
		 * Constructor that sets input stream
		 * @param dataIn Data input stream
		 */
		public ReciveDataThread(DataInputStream dataIn)
		{
			this.dataIn = dataIn;
		}
		
		@Override
		public void run()
		{
			String command = "";
			while(!(command.equals("endConnection")))
			{
				try
				{
					command = dataIn.readUTF();
					if(command.equals("addUser")) // Command that add user to list
					{
						String login = dataIn.readUTF();
						Platform.runLater(() -> controller.addUserToList(login));
					}
					else if(command.equals("deleteUser")) // Command that delete user from list
					{
						String login = dataIn.readUTF();
						Platform.runLater(() -> controller.deletUserFromList(login));
					}
					else if(command.equals("reciveFile")) // Receving new file 
					{
						try
						{
							Platform.runLater(() -> controller.LogAdd("Reciving file..."));
							String name = dataIn.readUTF();
							long modified = dataIn.readLong();
							long size = dataIn.readLong();
							File file = new File(path + "/" + name);
							if(file.exists())
								file.delete();
							file.createNewFile();
							FileOutputStream stream = new FileOutputStream(file);
							for(long i = 0; i < size; i++)
							{
								int data = dataIn.read();
								stream.write(data);
							}
							file.setLastModified(modified);
							stream.close();
							Platform.runLater(() -> controller.refreshFiles());
							Platform.runLater(() -> controller.LogAdd("File recived !"));
						}
						catch(Exception e)
						{
							Platform.runLater(() -> controller.LogAdd("Failed to recive file"));
						}
					}
				}
				catch(Exception e)
				{
					Platform.runLater(() -> controller.LogAdd("~~~~~Server disconnected !~~~~~"));
					Platform.runLater(() -> controller.LogAdd("~~~~~Please close app !~~~~~"));
					break;
				}
				
			}
		}
	}
}
