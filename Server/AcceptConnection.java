package application;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import javafx.application.Platform;

/** Class that accepts incomming connections */

public class AcceptConnection implements Runnable
{
	private ServerSrc ServerData; //Data of server
	private ServerSocket serverSocket; //Socket on which server is running
	private ServerAppController controller; //Controller of server GUI
	private ExecutorService clientThreads = Executors.newCachedThreadPool(); // Thread pool of clients

	/**
	 * Constructor that sets data
	 * @param ServerData Data of server
	 * @param serverSocket Socket on which server is running
	 * @param controller Controller of server GUI
	 */
	public AcceptConnection(ServerSrc ServerData, ServerSocket serverSocket, ServerAppController controller)
	{
		this.ServerData = ServerData;
		this.controller = controller;
		this.serverSocket = serverSocket;
	}
	
	/**
	 * Accepts incomming connections
	 */
	public void run()
	{
		while(true)
		{
			try
			{
				Socket clSocket = serverSocket.accept();
				clientThreads.execute(new ClientMenager(ServerData, clSocket, new DataInputStream(clSocket.getInputStream()), new DataOutputStream(clSocket.getOutputStream()), controller));
				Platform.runLater(() -> controller.LogAdd("~~~~~~New client connected - " + clSocket + "~~~~~~"));
			}
			catch(IOException e) //When error occurs, server shut down
			{
				clientThreads.shutdownNow();
				try
				{
					clientThreads.awaitTermination(1, TimeUnit.NANOSECONDS);
				}
				catch(Exception e2)
				{
					System.out.println("Thread already closed");
				}
				ServerData.serverShutDown = 1;
				break;
			}
		}
	}
}
