package application;
	
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.application.*;
import javafx.stage.*;
import java.io.*;
import java.util.*;

/**
 * 
 * Main client App class
 *
 */
public class ClientApp extends Application 
{	
	/** All data about client */
	private static ClientSrc clientData;
	
	/**
	 * Method that shows graphic interface of client and sets data
	 * @param primaryStage Main stage of GUI
	 * @throws Exception Exception that is returned by JavaFX
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		String login = null;
		String path = null;
		

		login = getLogin("Enter your login:"); //Ask user for login
		path = getPath(); //Ask user for path

		if(isPathValid(path) == 0)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Invalid path");
			alert.setContentText("Invalid path entered !");
			alert.showAndWait();
			return;
		}
		
		//Load client layout from fxml file
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("ClientApp.fxml"));
		Parent root = loader.load();
		ClientAppController controller = loader.getController();
		clientData = new ClientSrc(login, path, controller, "localhost", 16); //Create new client with given login and path
		controller.setData(clientData); //Set Login and path on GUI
		
		int connected = 0;
		while(connected != 1)
		{
			connected = clientData.StartConnection();
			if(connected == 0) //Login already exists
			{
				login = getLogin("Please choose another login");
				clientData.changeLogin(login);
			}
			else if(connected == 2) //Server not available
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Server not found");
				alert.setContentText("Server is not running, unable to connect.");
				alert.showAndWait();
				return;
			}
		}
		
		primaryStage.setTitle("Client App");
		primaryStage.setOnCloseRequest((WindowEvent event) -> clientData.StopConnection());
		primaryStage.setScene(new Scene(root, 900, 700));
		primaryStage.show();
	}
	
	/**
	 * Method that returns String with login name
	 * @param message Text that is being displayed on login window
	 * @return String with login of user
	 */
	private String getLogin(String message) 
	{
        TextInputDialog LoginWindow = new TextInputDialog("Login");

        LoginWindow.setTitle("Login:");
        LoginWindow.setHeaderText(message);
        LoginWindow.setContentText("Login:");

        Optional<String> text = LoginWindow.showAndWait();
        if(text.isPresent())
            return text.get();
        else
            return null;
    }
	
	/**
	 * Method that returns String with path
	 * @return path
	 */
	private String getPath() 
	{
        TextInputDialog PathWindow = new TextInputDialog("Path");

        PathWindow.setTitle("Path");
        PathWindow.setHeaderText("Enter path:");
        PathWindow.setContentText("Path:");

        Optional<String> text = PathWindow.showAndWait();
        if(text.isPresent())
            return text.get();
        else
            return null;
    }
	
	/**
	 * Method that checks if given path exists
	 * @param path Selected path
	 * @return 1 if exists and 0 if not
	 */
	private int isPathValid(String path)
	{
		if (new File(path).exists())
		{
			return 1;
		}
		return 0;
	}
	
	/**
	 * Method that starts JavaFX
	 * @param args arguments given on start
	 */
	public static void main(String[] args) 
	{
		launch(args);
	}
}
