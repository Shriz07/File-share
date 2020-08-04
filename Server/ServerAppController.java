package application;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import java.io.*;
import java.util.*;

/** Class that controlls GUI of server */

public class ServerAppController 
{
	/** Elements of graphic interface */
	@FXML
	private Menu lblMenuUsers;
	@FXML
	private ListView lblLog;
	@FXML
	private TabPane lblTabPane;
	
	
	private ServerSrc serverData; //All data about server
	private LinkedList<TabController> tabControllers = new LinkedList<>(); //Linked list with tab controllers of users
	
	/**
	 * Method that sets basic data
	 * @param ServerData Data of server
	 */
	public void setData(ServerSrc ServerData)
	{
		this.serverData = ServerData;
		lblMenuUsers.setText("Registered Users: " + Integer.toString(ServerData.getUsers()));
	}
	
	/**
	 * Method that adds log with given message
	 * @param message Message to be displayed in log
	 */
	public void LogAdd(String message)
	{
		GregorianCalendar time = new GregorianCalendar();
		String timeString = "[" + time.get(GregorianCalendar.HOUR) + ":" + time.get(GregorianCalendar.MINUTE) + ":" + time.get(GregorianCalendar.SECOND) + "] -";
		String result = timeString + " " + message;
		lblLog.getItems().add(0, result);
	}
	
	/**
	 * Method that adds a new table when new user occurs
	 * @param login Login of new user
	 */
	public void addNewtab(String login)
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("newTab.fxml"));
		Tab table = null;
		try
		{
			table = loader.load();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		TabController tabController = loader.getController();
		lblTabPane.getTabs().add(table);
		tabControllers.add(tabController);
		tabController.setData(login, serverData.getPath() + "/" + login);
		tabController.setTabTitle();
		new File(serverData.getPath() + "/" + login).mkdirs();
		tabController.refreshFiles();
	}
	
	/**
	 * Method that refreshes displayed files of given user
	 * @param login Login of user whose files needs refresh
	 */
	public void refreshDisplayedFiles(String login)
	{
		for(TabController controller : tabControllers)
		{
			if(controller.getLogin().equals(login))
			{
				controller.refreshFiles();
				break;
			}
		}
	}
	
	/**
	 * Method that delete tab when user log out
	 * @param login Login of user that logged out
	 */
	public void deleteTab(String login)
	{
		ObservableList<Tab> tables = lblTabPane.getTabs();
		int f = 0;
		for(Tab table : lblTabPane.getTabs())
		{
			if(table.getText().equals(login))
			{
				f = 1;
				tables.remove(table);
				break;
			}
		}
		if(f == 1)
		{
			for(TabController controller : tabControllers)
			{
				if(controller.getLogin().equals(login))
				{
					tabControllers.remove(controller);
					serverData.deleteUser();
					break;
				}
			}
		}
	}
	
	/**
	 * Method that sets number of logged users on GUI
	 */
	public void setUsers()
	{
		lblMenuUsers.setText("Registered Users: " + serverData.getUsers());
	}
}
