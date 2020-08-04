package application;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.nio.file.*; 

import javafx.application.Platform;

/** Class that represents single user and all his data */

class ClientMenager implements Runnable
{
	private ServerSrc serverData; //Data of server
	private String userLogin; //Login of user
	public Socket clSocket; //Socket on which communication is estabilished
	public DataInputStream dataIn = null; // Data input steram
    public DataOutputStream dataOut = null; //Data output stream
    private ServerAppController controller; //Controller of server GUI
    
    private static ReciveDataThread reciveDataThread; //Thread that recives data from client
	
    /**
     * Constructor that constructs client with given data
     * @param serverData Data of server
     * @param clSocket Socket on which communication is estabilished
     * @param dataIn Data input steram
     * @param dataOut Data output stream
     * @param controller Controller of server GUI
     */
	public ClientMenager(ServerSrc serverData, Socket clSocket, DataInputStream dataIn, DataOutputStream dataOut, ServerAppController controller)
	{
		this.serverData = serverData;
		this.clSocket = clSocket;
		this.dataIn = dataIn;
		this.dataOut = dataOut;
		this.controller = controller;
	}
	
	/**
	 * Exchanges information between Server and Client
	 */
    public void run()
    {
		try
		{
			String login = dataIn.readUTF();
			
			int check = serverData.checkIfLoginExists(login);
			if(check == 1) //Login already exists in database
			{
				Platform.runLater(() -> controller.LogAdd("Client with that login already extists."));
				dataOut.writeUTF("loginError");
				clSocket.close();
				return;
			}
			else //Login is correct
			{
				userLogin = login;
				dataOut.writeUTF("correctLogin");
				serverData.addUser(login, this);
				Platform.runLater(() -> controller.setUsers());
				Platform.runLater(() -> controller.addNewtab(userLogin));
				Platform.runLater(() -> controller.LogAdd("New User login is: " + userLogin));
				Platform.runLater(() -> controller.LogAdd("SYNCHRONIZING FILES WITH USER - " + userLogin));
				File new_dir = new File(serverData.getPath() + '/' + userLogin);
				//Creating new user catalog if it doesn't exists
				if(!(new_dir.exists()))
					new_dir.mkdir();
				syncFiles();
				Platform.runLater(() -> controller.refreshDisplayedFiles(userLogin));
				Platform.runLater(() -> controller.LogAdd("SYNCHRONIZATION ENDED"));
				
				//Send list of active users
				for(Map.Entry<String, ClientMenager> entry : serverData.getClients().entrySet())
				{
					String nextLogin = entry.getKey();
					if(!(nextLogin.equals(userLogin)))
					{
						dataOut.writeUTF("addUser");
						dataOut.writeUTF(nextLogin);
					}
				}
				//Send user login to everyone
				Platform.runLater(() -> controller.LogAdd("Sending new user login to another users"));
				serverData.sendLoginToEveryone(userLogin);
			}
		}
		catch(Exception e)
		{
			Platform.runLater(() -> controller.LogAdd("Error - " + e));
		}
		//Creating thread to recive data from client
		reciveDataThread = new ReciveDataThread(serverData, userLogin, dataIn, dataOut, controller);
		reciveDataThread.start();
		try
		{
			reciveDataThread.join();
			clSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		Thread.currentThread().interrupt();
    }
    
    /**
     * Method that sends information to a client to add new user with given login
     * @param login Login of user to add
     */
    public void sendLoginToUser(String login)
    {
    	try
    	{
    		dataOut.writeUTF("addUser");
    		dataOut.writeUTF(login);
    	}
    	catch(Exception e)
    	{
    		Platform.runLater(() -> controller.LogAdd("Failed to send data"));
    	}
    }
    
    /**
     * Method that sends information to a client that user logged out with given login
     * @param login Login of user to delete
     */
    public void sendLogoutToUser(String login)
    {
    	try
    	{
    		dataOut.writeUTF("deleteUser");
    		dataOut.writeUTF(login);
    	}
    	catch(Exception e)
    	{
    		Platform.runLater(() -> controller.LogAdd("Failed to send data"));
    	}
    }
    
    /**
     * Method that synchronizes files with client
     */
    public void syncFiles()
    {
    	try
    	{
	    	String recived = "nextFile";
	    	ArrayList<String> files_send = new ArrayList<String>();
	    	//Files given by user
	    	while(recived.equals("nextFile"))
	    	{
	    		recived = dataIn.readUTF();
	    		if(recived.equals("nextFile"))
	    		{
	    			String name = dataIn.readUTF();
	    			long modified = dataIn.readLong();
	    			long size = dataIn.readLong();
	    			files_send.add(name);
	    			Platform.runLater(() -> controller.LogAdd("Recived file - " + name));
	    			File file = new File(serverData.getPath() + "/" + userLogin + "/" + name);
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
	    		}
	    	}
	    	//Deleting additional files that doesn't exists in user  local directory
	    	for(File file : new File(serverData.getPath()+ "/" + userLogin).listFiles())
	    	{
	    		String name = file.getName();
	    		int delete = 1;
	    		for(int i = 0; i < files_send.size(); i++)
	    		{
	    			if(files_send.get(i).equals(name))
	    			{
	    				delete = 0;
	    				break;
	    			}
	    		}
	    		if(delete == 1)
	    		{
	    			File n_file = new File(serverData.getPath()+ "/" + userLogin + "/" + name);
	    			n_file.delete();
	    		}
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		Platform.runLater(() -> controller.LogAdd("Error while synchronizing files"));
    	}
    }
    
    /**
     * 
     * Thread that listens to a client and responds to commands
     *
     */
    private class ReciveDataThread extends Thread
    {
    	private ServerSrc serverData; //Server data
    	private String userLogin; //User login
    	public DataInputStream dataIn = null; //Data input stream
        public DataOutputStream dataOut = null; //Data output stream
        private ServerAppController controller;//Controller of server GUI
    	
        /**
         * Constructor that sets data 
         * @param serverData Server data
         * @param userLogin User login
         * @param dataIn Data input stream
         * @param dataOut Data output stream
         * @param controller Controller of server GUI
         */
    	public ReciveDataThread(ServerSrc serverData, String userLogin, DataInputStream dataIn, DataOutputStream dataOut, ServerAppController controller)
    	{
    		this.serverData = serverData;
    		this.userLogin = userLogin;
    		this.dataIn = dataIn;
    		this.dataOut = dataOut;
    		this.controller = controller;
    	}
    	
    	/**
    	 * Listen and react to user commands
    	 */
    	@Override
    	public void run()
    	{
    		while(!Thread.currentThread().isInterrupted())
            {
            	String command = "";
            	try
            	{
            		command = dataIn.readUTF();
            	}
            	catch(Exception e)
            	{
            		Platform.runLater(() -> controller.LogAdd("Error - " + e));
            	}
            	
            	if(command.equals("logout")) //User logged out, deleting all data refered to him
            	{
            		serverData.sendDeleteLoginToEveryone(userLogin);
            		serverData.deleteUser(userLogin);
            		Platform.runLater(() -> controller.deleteTab(userLogin));
            		Platform.runLater(() -> controller.setUsers());
            		Platform.runLater(() -> controller.LogAdd("User logged out with login - " + userLogin));
            		Thread.currentThread().interrupt();
            	}
            	else if(command.equals("deleteFile")) //File was deleted, so deleting it 
            	{
            		String name = "";
            		try
            		{
            			name = dataIn.readUTF();
            		}
            		catch(Exception e)
            		{
            		}
            		final String tmp = name;
            		Platform.runLater(() -> controller.LogAdd("Recived file delete request from user - " + userLogin + " File name - " + tmp));
            		String path = serverData.getPath() + "/" + userLogin + "/" + tmp;
            		System.out.println(path);
            		try
            		{
            			Files.deleteIfExists(Paths.get(path));
            		}
                    catch(Exception e) 
                    { 
                        e.printStackTrace();
                    } 
            		Platform.runLater(() -> controller.refreshDisplayedFiles(userLogin));
            	}
            	else if(command.equals("addFile")) //New or modified file occured
            	{
            		String name = "";
            		try
            		{
            			name = dataIn.readUTF();
    	        		final String tmp = name;
    	        		Platform.runLater(() -> controller.LogAdd("Recived new or modified file from user - " + userLogin + " File name - " + tmp));
    	        		long modified = dataIn.readLong();
    	    			long size = dataIn.readLong();
    	    			File file = new File(serverData.getPath() + "/" + userLogin + "/" + name);
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
    	    			Platform.runLater(() -> controller.refreshDisplayedFiles(userLogin));
            		}
            		catch(Exception e)
            		{
            			Platform.runLater(() -> controller.LogAdd("Failed to recive file"));
            		}
            	}
            	else if(command.equals("sendFile")) //Users wants to send file to another user
            	{
            		try
            		{
            			String login = "";
            			String fileName = "";
            			login = dataIn.readUTF();
            			fileName = dataIn.readUTF();
            			final String destinationLogin = login;
            			Platform.runLater(() -> controller.LogAdd("Sending file from user: " + userLogin + " to user: " + destinationLogin));
            			DataOutputStream destinationDataOut = serverData.reciveUserOutputStream(login);
            			File file = new File(serverData.getPath() + "/" + userLogin + "/" + fileName);
            			long size = file.length();
            			long modified = file.lastModified();
            			destinationDataOut.writeUTF("reciveFile");
            			destinationDataOut.writeUTF(fileName);
            			destinationDataOut.writeLong(modified);
            			destinationDataOut.writeLong(size);
            			
            			FileInputStream stream = new FileInputStream(file);
            			for(long i = 0; i < size; i++)
            			{
            				int data = stream.read();
            				destinationDataOut.write(data);
            				dataOut.flush();
            			}
            			stream.close();
            			Platform.runLater(() -> controller.LogAdd("Sending complete"));
            		}
            		catch(Exception e)
            		{
            			e.printStackTrace();
            			Platform.runLater(() -> controller.LogAdd("Failed to send file"));
            		}
            	}
            }
    	}
    }
}	
