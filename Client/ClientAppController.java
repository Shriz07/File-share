package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** Class that controlls layout of GUI */

public class ClientAppController 
{
	/** Elements of graphic interface */
	@FXML
	private Menu lblMenuLogin;
	@FXML
	private Menu lblMenuPath;
	@FXML
	private ListView lblLog;
	@FXML
	private TableView<FileFormat> lblTable;
	@FXML
	private TableColumn<FileFormat, String> lblFilename;
	@FXML
	private TableColumn<FileFormat, String> lblModified;
	@FXML
	private TableColumn<FileFormat, String> lblSize;
	@FXML
	private ListView<String> lblUsers;
	@FXML
	private Button lblSendFile;
	
	//All data about client
	private ClientSrc ClientData;
	
	/**
	 * Method that sets basic data and texts in window
	 * @param ClientData class that represents client
	 */
	public void setData(ClientSrc ClientData)
	{
		this.ClientData = ClientData;
		lblMenuLogin.setText("Login: " + ClientData.getLogin());
		lblMenuPath.setText("Path: " + ClientData.getPath());
		lblFilename.setCellValueFactory(new PropertyValueFactory<>("Filename"));
		lblModified.setCellValueFactory(new PropertyValueFactory<>("Modified"));
		lblSize.setCellValueFactory(new PropertyValueFactory<>("Size"));
	}
	
	/**
	 * Method that changes displayed login
	 */
	public void displayLogin()
	{
		lblMenuLogin.setText("Login: " + ClientData.getLogin());
	}
	

	/**
	 * Method that adds log with given message
	 * @param message to be displayed
	 */
	public void LogAdd(String message)
	{
		GregorianCalendar time = new GregorianCalendar();
		String timeString = "[" + time.get(GregorianCalendar.HOUR) + ":" + time.get(GregorianCalendar.MINUTE) + ":" + time.get(GregorianCalendar.SECOND) + "] -";
		String result = timeString + " " + message;
		lblLog.getItems().add(0, result);
	}
	
	/**
	 * Method that adds user with give login to list of users
	 * @param login to be added
	 */
	public void addUserToList(String login)
	{
		lblUsers.getItems().add(login);
	}
	
	/**
	 * Method that delete user with given login from list of users
	 * @param login to be deleted
	 */
	public void deletUserFromList(String login)
	{
		lblUsers.getItems().remove(login);
	}
	
	/**
	 * Method that sends file to selected user when button is pressed
	 * @param event when button is pressed
	 */
	public void SendFileToUser(ActionEvent event)
	{
		String user = lblUsers.getSelectionModel().getSelectedItem();
		FileFormat file = lblTable.getSelectionModel().getSelectedItem();
		if(user == null || file == null)
		{
			LogAdd("Please select user and file !");
			return;
		}
		try
		{
			ClientData.sendFileToUser(user, file.getFilename());
		}
		catch(Exception e)
		{
			LogAdd("Failed to send file");
		}
	}
	
	/**
	 * Method that refreshes current list of files
	 */
	public void refreshFiles()
	{
		ObservableList<FileFormat> files = lblTable.getItems();
		ObservableList<FileFormat> tempDataList = FXCollections.observableArrayList();
		files.clear();
		for(File file : new File(ClientData.getPath()).listFiles())
		{
			long size = file.length();
			String size_text = Long.toString(size);
			String name = file.getName();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String time = sdf.format(file.lastModified());
            
            tempDataList.add(new FileFormat(name, size_text, time));
            
		}
		lblTable.setItems(tempDataList);
	}
	
	/**
	 * Class that represents file
	 */
	public class FileFormat
	{
		private SimpleStringProperty filename;
		private SimpleStringProperty size;
		private SimpleStringProperty modified;
		
		public FileFormat(String name, String size, String modified)
		{
			this.filename = new SimpleStringProperty(name);
			this.size = new SimpleStringProperty(size);
			this.modified = new SimpleStringProperty(modified);
		}
		
		/**
		 * Method that returns filename
		 * @return filename
		 */
		public String getFilename()
		{
			return filename.get();
		}
		
		/**
		 * Method that returns size of file
		 * @return size of file
		 */
		public String getSize()
		{
			return size.get();
		}
		
		/**
		 * Method that returns modified date
		 * @return modified date
		 */
		public String getModified()
		{
			return modified.get();
		}
		
	}
}
