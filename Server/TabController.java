package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.text.SimpleDateFormat;

/** Class that controlls single tab of a user*/

public class TabController 
{
	/** Elements of graphic interface */
	@FXML
    private Tab lblTab;
	@FXML
    private TableView<FileFormat> lblTable;
	@FXML
	private TableColumn<FileFormat, String> lblFilename;
	@FXML
	private TableColumn<FileFormat, String> lblModified;
	@FXML
	private TableColumn<FileFormat, String> lblSize;
	
	private String login; //Login of user
	private String path; //Selected path
	
	/**
	 * Method that sets basic data
	 * @param login Login of user
	 * @param path Selected path
	 */
	public void setData(String login, String path)
	{
		this.login = login;
		this.path = path;
		lblFilename.setCellValueFactory(new PropertyValueFactory<>("Filename"));
		lblModified.setCellValueFactory(new PropertyValueFactory<>("Modified"));
		lblSize.setCellValueFactory(new PropertyValueFactory<>("Size"));
	}
	
	/**
	 * Method that sets tab title to a login of a user
	 */
	public void setTabTitle()
	{
		lblTab.setText(login);
	}
	
	/**
	 * Method that returns login
	 * @return Login
	 */
	public String getLogin()
	{
		return login;
	}
	
	/**Method that refreshes displayed files in tab
	 * 
	 */
	public void refreshFiles()
	{
		ObservableList<FileFormat> files = lblTable.getItems();
		ObservableList<FileFormat> tempDataList = FXCollections.observableArrayList();
		files.clear();
		for(File file : new File(path).listFiles())
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
	 * Class that represents single file
	 *
	 */
	public class FileFormat
	{
		private SimpleStringProperty filename; //Filename
		private SimpleStringProperty size; //Size of file
		private SimpleStringProperty modified; //Data of modification
		
		public FileFormat(String name, String size, String modified)
		{
			this.filename = new SimpleStringProperty(name);
			this.size = new SimpleStringProperty(size);
			this.modified = new SimpleStringProperty(modified);
		}
		
		/**
		 * Returns filename
		 * @return Filename
		 */
		public String getFilename()
		{
			return filename.get();
		}
		/**
		 * Returns size of File
		 * @return Size of file
		 */
		public String getSize()
		{
			return size.get();
		}
		/**
		 * Returns modification date
		 * @return Modification date
		 */
		public String getModified()
		{
			return modified.get();
		}
	}
}
