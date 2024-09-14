package testpluginproject;

/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */

import java.awt.MenuItem;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.ConsoleHandler;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
//import org.eclipse.swt.internal.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.ViewCategory;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogEntry;
import org.eclipse.ui.internal.views.log.LogView;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.StatusManager.INotificationListener;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.framework.BundleContext;


import com.google.gson.Gson;
import com.google.gson.JsonElement;

import testpluginproject.handlers.ClientServerConnectionHandlers;
import testpluginproject.handlers.listener.KeyBoardClickListener;
import testpluginproject.handlers.listener.LoggerResourceChangeListener;
import testpluginproject.handlers.listener.MouseClickListener;
//import testpluginproject.handlers.listener.ViewSelectionListener;
import testpluginproject.handlers.listener.WindowClickListener;
import testpluginproject.model.UserActionData;
import testpluginproject.model.WorkSpaceLog;
import testpluginproject.model.jsonModel.EventDataJsonObject;
import testpluginproject.model.jsonModel.SequentialEventData;
import testpluginproject.utils.EventTimeComparator;
import testpluginproject.utils.Utils;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup, ISelectionListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "TestPluginProject"; //$NON-NLS-1$
	private static final String KEY_NAME = "DeveloperID";
	private static final String CLIENT_KEY = "ClientKey";
	private static final String CLIENT_SECRET = "CLientSecret";
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static ClientServerConnectionHandlers obClientConn;
	private static final String FILE_PATH = "keys.properties";

	private Date lastIneretedErrorLogDateTime = null;
	private static IEclipsePreferences preferences;
//	public StringBuilder keyBoardClickEvents;
	List<WorkSpaceLog> errorLogList;
	private List<SequentialEventData> listSequntialevents;
	MouseClickListener mouseClickListener;
	KeyBoardClickListener keyBoardClickListener;
	// The shared instance
	private static Activator plugin;
	
	//watchservice for checking resource change outside eclipse
	private WatchService watchService;
    private Thread watchThread;

	/**
	 * The constructor
	 */
	public Activator() {
//		keyBoardClickEvents = new StringBuilder();
		errorLogList = new ArrayList<>();
		listSequntialevents = new ArrayList<>();		
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		retriveKey();
		plugin = this;
		
		LoggerResourceChangeListener listener = new LoggerResourceChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
        
     // Initialize and start the file system watcher
        startFileSystemWatcher();
	}



	@Override
	public void stop(BundleContext context) throws Exception {
		
		 // Stop the file system watcher
        stopFileSystemWatcher();
        
		plugin = null;
		super.stop(context);
	}

	private void startFileSystemWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            java.nio.file.Path path = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
            
            System.out.println(path.toString());
            // Register the path to watch for changes
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            watchThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                WatchEvent<java.nio.file.Path> ev = (WatchEvent<java.nio.file.Path>) event;
                                System.out.println(ev.toString());
                                java.nio.file.Path newPath = ev.context();
                                System.out.println("New file system resource added: " + newPath.toString());
                                SequentialEventData sev = new SequentialEventData("External File Change", newPath.toString());
                                listSequntialevents.add(sev);
                                // Log the event or take appropriate action
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            watchThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void stopFileSystemWatcher() {
        try {
            if (watchService != null) {
                watchService.close();
            }
            if (watchThread != null) {
                watchThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	This method is for saving the keys, client information within workspace. 
	Right now we only focused on saving the information in internal memory.**/
	
	public static String retriveKey() {
		preferences = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
//		preferences.remove(KEY_NAME);
		String storekey = getKeyFromFile(KEY_NAME);
		if(storekey==null) {
			try {
				obClientConn = new ClientServerConnectionHandlers();
				String[] listStr = obClientConn.connectToServer();
				saveKeysToFile(listStr);
				preferences.put(KEY_NAME, listStr[0]);
				preferences.put(CLIENT_KEY, listStr[1]);
				preferences.put(CLIENT_SECRET, listStr[2]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else {
			preferences.put(KEY_NAME, getKeyFromFile(KEY_NAME));
			preferences.put(CLIENT_KEY, getKeyFromFile(CLIENT_KEY));
			preferences.put(CLIENT_SECRET, getKeyFromFile(CLIENT_SECRET));
		}
		return storekey;
	}
//	public static String retriveKey() {
//		preferences = ConfigurationScope.INSTANCE.getNode(PLUGIN_ID);
//		//preferences.remove(KEY_NAME);
//		String storekey = preferences.get(KEY_NAME, null);
//		System.out.println("Key:"+preferences.get(KEY_NAME, null));
//		if(storekey==null) {
//			try {
//				obClientConn = new ClientServerConnectionHandlers();
//				String[] listStr = obClientConn.connectToServer();
//				preferences.put(KEY_NAME, listStr[0]);
//				preferences.put(CLIENT_KEY, listStr[1]);
//				preferences.put(CLIENT_SECRET, listStr[2]);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//		return storekey;
//	}
	

	private static String getKeyFromFile(String key) {
//		IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		String userHome = System.getProperty("user.home");	
		IPath filePath = new Path(FILE_PATH);
		IPath userHomePath = new Path(userHome);
//		IPath absolutePath = workspaceLocation.append(filePath);
		IPath absolutePath = userHomePath.append(filePath);
		String absolutePathString = absolutePath.toOSString();
		if (!absolutePath.toFile().exists()) {
			return null;
		}
        try (InputStream input = new FileInputStream(absolutePathString)) {
            Properties prop = new Properties();

            // Load properties from file
            prop.load(input);

            // Retrieve the value associated with the specified key
            return prop.getProperty(key);
        } catch (Exception  io) {
            io.printStackTrace();
            return null;
        }
    }
	
	private static void saveKeysToFile(String[] keys) {
//		IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		String userHome = System.getProperty("user.home");	
		IPath filePath = new Path(FILE_PATH);
		IPath userHomePath = new Path(userHome);
//		IPath absolutePath = workspaceLocation.append(filePath);
		IPath absolutePath = userHomePath.append(filePath);
		String absolutePathString = absolutePath.toOSString();
		try (OutputStream output = new FileOutputStream(absolutePathString)) {
			Properties prop = new Properties();

			// Store key-value pairs
			prop.setProperty(KEY_NAME, keys[0]);
			prop.setProperty(CLIENT_KEY, keys[1]);
			prop.setProperty(CLIENT_SECRET, keys[2]);

			// Save properties to file
			prop.store(output, null);

			System.out.println("Keys saved to " + absolutePathString);
		} catch (Exception io) {
			System.out.println("Exception Happened due to: " + io.getMessage());
		}
	}
	
	public void getWorkSpceErrorLog() throws PartInitException{
		//Error Log Collections
		errorLogList = new ArrayList<>();
		IViewPart viewPart;
		try {
			PlatformUI.getWorkbench().getWorkbenchWindows()[0].getPages()[0].showView("org.eclipse.pde.runtime.LogView");
			viewPart = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getPages()[0].findView("org.eclipse.pde.runtime.LogView");
		} catch (Exception e) {
			return;
		}
//		if(lastIneretedErrorLogDateTime ==null) {
//			Calendar calendar = Calendar.getInstance();
//	        Date currDate = calendar.getTime();
//	        lastIneretedErrorLogDateTime = currDate;
//		}
		
//		System.out.println(viewPart.getTitle());
		LogView logview = (LogView) viewPart;
//	        System.out.println(logview.getContentDescription());
//	        System.out.println(logview.getTitle());
//	        System.out.println(logview.getPartName());

		AbstractEntry[] logs = logview.getElements();
		
		for (AbstractEntry entry : logs) {
			String severity = entry.toString();
			String message = entry.getAdapter(LogEntry.class).getMessage();
			String pluginId = entry.getAdapter(LogEntry.class).getPluginId();
			Date dateErrorLog = null;
			try {
				dateErrorLog = dateFormat.parse(entry.getAdapter(LogEntry.class).getFormattedDate());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String sessionData = entry.getAdapter(LogEntry.class).getSession().getSessionData();
			if(lastIneretedErrorLogDateTime == null) {
				WorkSpaceLog wsl = new WorkSpaceLog(dateErrorLog, severity, pluginId, message, sessionData);
				errorLogList.add(wsl);
			}
			else if(lastIneretedErrorLogDateTime.before(dateErrorLog)) { //Note: note considering  any log from previous sessions.// || lastIneretedErrorLogDateTime.equals(dateErrorLog)) {
				WorkSpaceLog wsl = new WorkSpaceLog(dateErrorLog, severity, pluginId, message, sessionData);
				errorLogList.add(wsl);
			}
			
		}
		Collections.sort(errorLogList);
		try {
			if(errorLogList.size()>0) {
				lastIneretedErrorLogDateTime = dateFormat.parse(errorLogList.get(errorLogList.size() -1).getDate());
				System.out.println("The last date for the error log is:"+lastIneretedErrorLogDateTime.toString());
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return errorLogList;
	}
	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		System.out.println("Plugin Started automatically");
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		System.out.println("DESCRIPTION: " + workspace.getRoot().getLocation().toOSString());
		workbench.addWindowListener(new IWindowListener() {

			@Override
			public void windowOpened(IWorkbenchWindow window) {
				// TODO Auto-generated method stub
				addSelectionListener(window);

			}

			@Override
			public void windowDeactivated(IWorkbenchWindow arg0) {
				// TODO Auto-generated method stub
				System.out.println("window got deactivated at time." + new Date().toString());
				//could capture the event of idle So we can calculate that

			}

			@Override
			public void windowClosed(IWorkbenchWindow window) {
				// TODO Auto-generated method stub
				window.getWorkbench().removeWindowListener(this);
				saveEventDataAndSendtoServer(workbench);
				removeSelectionListener(window);
//				window.
//				PlatformUI.getWorkbench().close();
			}

			@Override
			public void windowActivated(IWorkbenchWindow arg0) {
				// TODO Auto-generated method stub
				System.out.println("Time to get activated the window. "+new Date().toString());

			}
		});
		
		// ConsoleHandler ch = new ConsoleHandler();

		ICommandService commandService = workbench.getService(ICommandService.class);
//		IHandlerService handlerService = workbench.getService(IHandlerService.class);
		// Example: Record a user action on startup
		// recordUserAction("org.eclipse.ui.file.save",commandService,handlerService);

		// Example: Record a user action when a command is executed
		commandService.addExecutionListener(new IExecutionListener() {

			@Override
			public void preExecute(String pr1, ExecutionEvent event) {
				// TODO Auto-generated method stub
				Command command = event.getCommand();
				System.out.println("inside the addExecutionListener");
				String CommandId = command.getId();
				recordUserAction(CommandId, commandService, workbench);

		}

			@Override
			public void postExecuteSuccess(String arg0, Object arg1) {
				// TODO Auto-generated method stub
				System.out.println("Post execution success.");

			}

			@Override
			public void postExecuteFailure(String arg0, ExecutionException arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void notHandled(String arg0, NotHandledException arg1) {
				// TODO Auto-generated method stub

			}
		});

		// getting error logs
//		IViewCategory wp = workbench.getViewRegistry().getCategories()[7];
//		IViewDescriptor [] descriptor =  wp.getViews();
//		
//		for(IViewDescriptor des: descriptor) {
//			System.out.println(des.getId());
//			if(des.getId().equals("org.eclipse.pde.runtime.LogView")) {
//				LogEntry le = des.getAdapter(LogEntry.class);
//				System.out.println(des.getId());
//			}
//			
//		}


		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(new IConsoleListener() {

			@Override
			public void consolesRemoved(IConsole[] consoles) {
				// TODO Auto-generated method stub

				for (IConsole console : consoles) {
					if (console instanceof TextConsole) {
						System.out.println("remove console from this.");
						// ((TextConsole) console).removePatternMatchListener(this);
					}
				}
			}

			@Override
			public void consolesAdded(IConsole[] consoles) {
				// TODO Auto-generated method stub
				for (IConsole console : consoles) {
					System.out.println("console output:" + console.getName());
					if (console instanceof TextConsole) {

						TextConsole textConsole = (TextConsole) console;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						IDocument document = textConsole.getDocument();
						String consoleContents = document.get();
						System.out.println("Console Contents:");
						System.out.println(consoleContents);
						SequentialEventData sed = new SequentialEventData("ConsoleOutputEvent", consoleContents);
						listSequntialevents.add(sed);
					}
				}
			}
		});

		
		//create a Time instance
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				saveEventDataAndSendtoServer(workbench);
			}
		};

		timer.schedule(task, 0,600000);
		//timer.schedule(task, 0,10000);
	}

	public void saveEventDataAndSendtoServer(IWorkbench workbench) {
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					getWorkSpceErrorLog();
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
					addSelectionListener(window);
				}
			}
		});
		
		//Getting OS Version
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		
		//Getting Java Version
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		
		//Getting Eclipse Version
		String eclipseVersion = Platform.getBundle("org.eclipse.core.runtime").getVersion().toString();
		
		List<SequentialEventData> mousEventList = new ArrayList<>();
		List<SequentialEventData> keyEvents = new ArrayList<>();
		if(mouseClickListener!=null) {
			mousEventList = mouseClickListener.getMouseClickData();
			mouseClickListener.setMouseClickData(new ArrayList<>());
		}
		if(keyBoardClickListener!=null) {
			keyEvents = keyBoardClickListener.getKeboardClickData();
			keyBoardClickListener.setKeboardClickData(new ArrayList<>());
		}
		
		listSequntialevents.addAll(mousEventList);
		listSequntialevents.addAll(keyEvents);
		
		//Grab from global
		listSequntialevents.addAll(GlobalVars.listSequentialEvents);
		
		
		
		//sort based on the event time
		Collections.sort(listSequntialevents, new EventTimeComparator());
		
		EventDataJsonObject edjo = new EventDataJsonObject(listSequntialevents,errorLogList);
//		System.out.println("EDJO"+edjo);
		edjo.setIPAddress(Utils.getIpAddress());
		edjo.setMACAddress(Utils.getMacAddress());
		edjo.setPluginVersion("V1.0.1");
		edjo.setOSInfo(osName, osVersion, osArch);
		edjo.setJavaInfo(javaVersion, javaVendor);
		edjo.setEclipseInfo(eclipseVersion);
		System.out.println("retrived key would be: ");
		//System.out.println(retriveKey());
		//Save as json
//		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		//mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		try {
//			String jsonString = mapper.writeValueAsString(edjo);
			JsonElement jsonString = gson.toJsonTree(edjo);
			//file save code
//			String workingDir = Paths.get(System.getProperty("user.dir"),"Data").toAbsolutePath().toString();
//			String workingDir = "H:\\NCSU Semesters\\Research_Eclipse_Plugin\\Data\\test_another.json";
			IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			String workingDir = new Date().getTime()+".json";
			IPath filePath = new Path(workingDir);
			IPath absolutePath = workspaceLocation.append(filePath);
			String absolutePathString = absolutePath.toOSString();
			
			System.out.println("SAVED NEW DATA");
			FileWriter fileWriter = new FileWriter(absolutePathString);
			fileWriter.write(jsonString.toString());
			fileWriter.close();
			/**File send to Server code from Activator**/
			try {
				if(obClientConn!=null) {
					System.out.println("Key:"+preferences.get(KEY_NAME, null));
					obClientConn.sendencryptedMessage(absolutePathString, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
					
				}
				else {
					retriveKey();
					System.out.println("Key:"+preferences.get(KEY_NAME, null));
					obClientConn = new ClientServerConnectionHandlers();
					obClientConn.sendencryptedMessage(absolutePathString, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
				}
				checkanyRemainingFilestoSend(workspaceLocation.toOSString());
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Exception Happened in sending data to server due to :"+e.getMessage());		
			
			}
			/**End of File send to Server**/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception happened. "+e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("Timer Task has been called.");
		System.out.println("The saved Data is: ");
		System.out.println(edjo.toString());
		//resetVariable
		reset(workbench.getActiveWorkbenchWindow());
	}
	private void checkanyRemainingFilestoSend(String absolutePathString) {
		// TODO Auto-generated method stub
//		IContainer workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		File directory = new File(absolutePathString);

        // Filename filter for .json files
        FilenameFilter jsonFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".json");
            }
        };

        // List all the JSON files in the directory
        File[] files = directory.listFiles(jsonFilter);
        if (files != null) {
            for (File file : files) {
                // Process each file (e.g., print the file name)
//            	System.out.println("Found JSON file: " + file.getName());
//            	IFile filetoSend = (IFile) file;
                String absolutePath = file.getAbsolutePath();
                
                if(obClientConn!=null) {
					obClientConn.sendencryptedMessage(absolutePath, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
					
				}
				else {
					retriveKey();
					obClientConn.sendencryptedMessage(absolutePath, preferences.get(KEY_NAME, null), 
							preferences.get(CLIENT_SECRET, null), preferences.get(CLIENT_KEY, null));
				}
                // Add your code here to read or process the JSON file
            }
        } else {
            System.out.println("Directory does not exist or is not a directory");
        }
	}

	
	private void recordUserAction(String commandId, ICommandService commandService, IWorkbench workbench) {
		try {
			//System.out.println("Key Board Data: "+keyBoardClickEvents.getContents());
			//System.out.println("Mouse Click Data: "+mouseClickData);
			//System.out.println("Console Output Data: "+consoleOutput);
			//System.out.println("MenubarClick Data: "+MenuBarClickActions);
//			System.out.println("WorkSpaceErrorLog Data: "+errorLogList);
			Map<String, String> parameters = new HashMap<>();
			String [] commandStr = commandId.split("\\.");
			String activePart = commandStr[commandStr.length-2];
			
			// parameters.put(commandId, pr1)
			Command command = commandService.getCommand(commandId);
			SequentialEventData menuSED = new  SequentialEventData("MenuBarClickEvent", commandId);
			listSequntialevents.add(menuSED);
//			MenuBarClickActions.add(new MenuBarClickData(commandId));
			System.out.println("Recorded action: " + commandId);
//			if(activePart.equals("views")) {
//				IViewPart viewPart = workbench.getActiveWorkbenchWindow().getActivePage().findView(commandId);
//				viewPart.getViewSite().getPage().addSelectionListener(new ViewSelectionListener());
//			}
			if (commandId.contains("copy")) {
				System.out.println("This is a copy event:");
				Display display = workbench.getDisplay();
				display.timerExec(200, new Runnable() { // adding delay on the code to get the current copy event

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Clipboard clipboard = new Clipboard(display);
						Object contents = clipboard.getContents(TextTransfer.getInstance());
						clipboard.dispose();
						if (contents instanceof String) {
//							String key = "copy Event " + copyCounter.toString();
							UserActionData uad = new UserActionData(contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
							SequentialEventData menuCopy = new  SequentialEventData("CopyEvent", uad);
							listSequntialevents.add(menuCopy);
//							cutcopyPasteEvents.add(new CutCopyPasteEvent("Copy", contents.toString()));
							System.out.println(uad);
						}
					}
				});
			} else if (commandId.contains("cut")) {
				System.out.println("This is a cut event:");
				Display display = workbench.getDisplay();
				display.timerExec(200, new Runnable() { // adding delay on the code to get the current copy event

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Clipboard clipboard = new Clipboard(display);
						Object contents = clipboard.getContents(TextTransfer.getInstance());
						clipboard.dispose();

						if (contents instanceof String) {
							UserActionData uad = new UserActionData(contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
							SequentialEventData menuCut = new  SequentialEventData("CutEvent", uad);
							listSequntialevents.add(menuCut);
//							cutcopyPasteEvents.add(new CutCopyPasteEvent("Cut", contents.toString()));
							System.out.println(uad);
						}
					}
				});
			} else if (commandId.contains("paste")) {
				System.out.println("This is a paste event:");
				Display display = workbench.getDisplay();
				display.timerExec(200, new Runnable() { // adding delay on the code to get the current copy event

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Clipboard clipboard = new Clipboard(display);
						Object contents = clipboard.getContents(TextTransfer.getInstance());
						clipboard.dispose();

						if (contents instanceof String) {
							UserActionData uad = new UserActionData(contents.toString(), GlobalVars.lastOpenFile, GlobalVars.activeProject);
							SequentialEventData menuPaste = new  SequentialEventData("PasteEvent", uad);
							listSequntialevents.add(menuPaste);
//							cutcopyPasteEvents.add(new CutCopyPasteEvent("Paste", contents.toString()));
							System.out.println(uad);
						}
					}
				});
			} else if (commandId.contains("selectAll")) {
				System.out.println("This is a Select All event:");
				ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				String selectedText = document.get();
				System.out.println("Selected Text: " + selectedText);
				// Now you can use 'selectedText' as the contents of the select all action
				UserActionData uad = new UserActionData(selectedText, GlobalVars.lastOpenFile,
						GlobalVars.activeProject);
				SequentialEventData menuSelectAll = new SequentialEventData("selectAll", uad);
				listSequntialevents.add(menuSelectAll);
				System.out.println(uad);
			} else if (commandId.contains("delete")) {
				System.out.println("This is a delete event:");
				ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				String deletedText = document.get();
				System.out.println("Selected Text: " + deletedText);
				UserActionData uad = new UserActionData(deletedText, GlobalVars.lastOpenFile,
						GlobalVars.activeProject);
				SequentialEventData menuDelete= new SequentialEventData("selectAll", uad);
				listSequntialevents.add(menuDelete);
				System.out.println(uad);
			}

		} catch (Exception e) {
			showErrorDialog("Error recording user action", e);
		}
	}

	private void showErrorDialog(String message, Throwable throwable) {
		IStatus status = new Status(IStatus.ERROR, getBundle().getSymbolicName(), message, throwable);
		ErrorDialog.openError(null, "Error", null, status);
	}

	private void addSelectionListener(IWorkbenchWindow window) {
		
		if (window != null) {
			mouseClickListener = new MouseClickListener(window);
			keyBoardClickListener = new KeyBoardClickListener();
			window.getShell().getDisplay().addFilter(org.eclipse.swt.SWT.MouseDown, mouseClickListener);
			window.getShell().getDisplay().addFilter(org.eclipse.swt.SWT.MouseDoubleClick,mouseClickListener);
			window.getShell().getDisplay().addFilter(org.eclipse.swt.SWT.KeyDown, keyBoardClickListener);			
			window.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(new WindowClickListener(listSequntialevents,window));

		}

	}

	private void removeSelectionListener(IWorkbenchWindow window) {
		System.out.println("Inside the remove selection listeners");
		try {
	        if (window != null) {
	        	if(window.getWorkbench()!=null) {
	        		window.getWorkbench().getDisplay().removeFilter(org.eclipse.swt.SWT.MouseDown, mouseClickListener);
	        		window.getWorkbench().getDisplay().removeFilter(org.eclipse.swt.SWT.MouseDoubleClick,mouseClickListener);
	        		window.getWorkbench().getDisplay().removeFilter(org.eclipse.swt.SWT.KeyDown, keyBoardClickListener);
//	        		window.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener();
	        		
	        	}
//	        	window.getShell().getDisplay().removeFilter(org.eclipse.swt.SWT.MouseDown, mouseClickListener);
//	        	window.getShell().getDisplay().removeFilter(org.eclipse.swt.SWT.MouseDoubleClick,mouseClickListener);
//	            window.getShell().getDisplay().removeFilter(org.eclipse.swt.SWT.KeyDown, new KeyBoardClickListener(keyBoardClickEvents));
//	            window.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(new MenuBarClickListener());
	            //window.getWorkbench().removeWorkbenchListener(this);
	        }
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception happened here."+e.getMessage());
		}

	}

	public void reset(IWorkbenchWindow window) {

//		keyBoardClickListener = new KeyBoardClickListener();
//		mouseClickListener = new MouseClickListener(window);
		GlobalVars.listSequentialEvents.clear();
		listSequntialevents = new ArrayList<>();
	}

	@Override
	public void selectionChanged(IWorkbenchPart arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		System.out.println("selection changed");
	}

}