package application;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;

/** Class that watch for changes in user local directory */

public class ClientWatcher  implements Runnable
{
	private ClientSrc clientData; //Client data
	private String dir; //Directory to be watched
	private ClientAppController controller; //Controller of user GUI
	
	/**
	 * Construtor that sets data
	 * @param clientData Client data
	 * @param path Directory to be watched
	 * @param controller Controller of user GUI
	 */
	public ClientWatcher(ClientSrc clientData, String path, ClientAppController controller)
	{
		this.clientData = clientData;
		dir = path;
		this.controller = controller;
	}
	
	/**
	 * Watches for changes in local directory
	 */
	public void run()
	{
		try(WatchService service = FileSystems.getDefault().newWatchService())
		{
			Map<WatchKey, Path> keyMap = new HashMap<>();
			Path path = Paths.get(dir);
			keyMap.put(path.register(service, 
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY), path);
			WatchKey watchKey;
			do
			{
				watchKey = service.take();
				Path eventDir = keyMap.get(watchKey);
				for(WatchEvent<?> event : watchKey.pollEvents())
				{
					WatchEvent.Kind<?> kind = event.kind();
					Path eventPath = (Path)event.context();
					String context = event.context().toString();
					String text = kind.toString();
					if(text.equals("ENTRY_DELETE"))
					{
						clientData.requestFileDeleteToServer(context);
					}
					else if(text.equals("ENTRY_CREATE") || text.equals("ENTRY_MODIFY"))
					{
						clientData.sendFileToServer(context);
					}
					Platform.runLater(() -> controller.refreshFiles());
				}
				
			} while(watchKey.reset());
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
