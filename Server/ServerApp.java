package application;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.util.List;

import javafx.application.*;
import javafx.stage.*;

/**
 * 
 * Main server app class
 *
 */

public class ServerApp extends Application
{
	private static ServerSrc serverData; //All data about server
	
	/**
	 * Method that shows graphic interface of server and sets data
	 * @param primaryStage Main stage of GUI
	 * @throws Exception Exception that is returned by JavaFX
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{	
		String path = null;
		
		List<String> args = getParameters().getRaw();
        if(args.size()>=1) 
        {
            path = args.get(0);
        }
        
        if(isPathValid(path) == 0)
		{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Invalid path");
			alert.setContentText("Invalid path entered !");
			alert.showAndWait();
			return;
		}
		
		//Load server layout from fxml file
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("Server.fxml"));
		Parent root = loader.load();
		ServerAppController controller = loader.getController();
		serverData = new ServerSrc(16, controller, path);
		controller.setData(serverData);
		serverData.EnableConnection();
		controller.LogAdd("Server started successfully !");
		
		primaryStage.setTitle("Server App");
		primaryStage.setOnCloseRequest((WindowEvent event) -> serverData.shutDown());
		primaryStage.setScene(new Scene(root, 900, 700));
		primaryStage.show();
	}
	/**
	 * Method that starts JavaFX
	 * @param args path of server directory
	 */
	public static void main(String[] args) 
	{
		launch(args);
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
} 