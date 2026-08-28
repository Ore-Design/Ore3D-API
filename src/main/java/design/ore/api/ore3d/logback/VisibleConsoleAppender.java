package design.ore.api.ore3d.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import design.ore.api.ore3d.Registry;
import design.ore.api.ore3d.Util;
import design.ore.api.ore3d.Util.Log;
import design.ore.api.ore3d.data.core.Build;
import design.ore.api.ore3d.data.specs.Spec;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VisibleConsoleAppender extends UnsynchronizedAppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent>
{
	private final List<String> previousCommands = new ArrayList<>();
	List<Appender<ILoggingEvent>> appenders = new ArrayList<>();
	private TextArea console;
	private TextField input;
	private int stackIndex = -1;

	@Getter private static VisibleConsoleAppender instance;
	
	public VisibleConsoleAppender()
	{
		if(instance != null) throw new RuntimeException("VisibleConsoleAppender has already been created!");
		this.setName("ConsoleAppender");
		instance = this;
	}
	
	public void bindConsole(TextArea console, TextField input)
	{
		this.console = console;
		this.input = input;
		this.input.setOnKeyReleased(e ->
		{
			String inputText = input.getText();
			if(e.getCode() == KeyCode.ENTER && inputText != null && !inputText.equals(""))
			{
				parseConsoleCommand(input.getText());
				input.clear();
			}
			else if(e.getCode() == KeyCode.UP)
			{
				if(stackIndex < previousCommands.size() - 1) stackIndex++;
				input.setText(previousCommands.get(stackIndex));
			}
			else if(e.getCode() == KeyCode.DOWN)
			{
				if(stackIndex > -1) stackIndex--;
				
				if(stackIndex == -1) input.setText("");
				else input.setText(previousCommands.get(stackIndex));
			}
			else resetPreviousStack();
		});
	}
	
	public void resetPreviousStack()
	{
		stackIndex = -1;
	}

	@Override
	public void addAppender(Appender<ILoggingEvent> newAppender) { appenders.add(newAppender); }

	@Override
	public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() { return appenders.iterator(); }

	@Override
	public Appender<ILoggingEvent> getAppender(String name)
	{
		for(Appender<ILoggingEvent> appender : appenders) { if(appender.getName().equals(name)) return appender; }
		return null;
	}

	@Override
	public boolean isAttached(Appender<ILoggingEvent> appender) { return appenders.contains(appender); }

	@Override
	public void detachAndStopAllAppenders()
	{
		for(Appender<ILoggingEvent> appender : appenders) { appender.stop(); }
		appenders.clear();
	}

	@Override
	public boolean detachAppender(Appender<ILoggingEvent> appender) { return appenders.remove(appender); }

	@Override
	public boolean detachAppender(String name)
	{
		for(Appender<ILoggingEvent> appender : appenders)
		{
			if(appender.getName().equals(name))
			{
				appenders.remove(appender);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void append(ILoggingEvent eventObject)
	{
		for(Appender<ILoggingEvent> appender : appenders) appender.doAppend(eventObject);

		if(eventObject.getLevel().toInteger() >= 20000 && console != null)
		{
			Platform.runLater(() -> console.appendText(eventObject.getFormattedMessage() + "\n"));
		}
	}

	public void parseConsoleCommand(String command)
	{
		resetPreviousStack();
		previousCommands.add(0, command);
		
//		boolean editIsOpen = Main.getCoreNav().getMenuIndex() == Navigation.EDIT;
		
//		if(command.equalsIgnoreCase("dumprecord"))
//		{
//			if(editIsOpen)
//			{
//				Util.Log.getLogger().info("Dumping record into appdata folder! This may take a minute...");
//
//				String recordJson;
//				Transaction tran = ((Edit)Main.getCoreNav().getCurrentMenu()).getTransaction();
//
//				try { recordJson = Util.Mapper.getMapper().writeValueAsString(tran); }
//				catch (JsonProcessingException e)
//				{
//					Util.Log.getLogger().warn("An error occurred while converting transaction to Json Data!", e);
//					return;
//				}
//
//				File transactionDump = new File(Main.appDataDir(), tran.getCustomer().getDisplayName() + "-" + tran.getId() + "-Dump.txt");
//				try {
//					transactionDump.mkdirs();
//					transactionDump.createNewFile();
//					FileUtils.writeStringToFile(transactionDump, recordJson, "UTF-8");
//				}
//				catch (IOException e)
//				{
//					Util.Log.getLogger().warn("Error creating and writing to file!", e);
//					return;
//				}
//
//				Util.Log.getLogger().info("Record dump succesful!");
//			}
//			else Util.Log.getLogger().info("No order is open on main window! Load an order before dumping record data!");
//		}
//		else if(command.equalsIgnoreCase("dumprecordnopricing"))
//		{
//			if(editIsOpen)
//			{
//				Util.Log.getLogger().info("Dumping record without pricing into appdata folder! This may take a minute...");
//
//				String recordJson;
//				Transaction tran = null;
//				try
//				{
//
//		        	byte[] jsonString = Util.Mapper.getMapper().writeValueAsBytes(((Edit)Main.getCoreNav().getCurrentMenu()).getTransaction());
//					tran = Util.Mapper.getMapper().readValue(new String(jsonString, StandardCharsets.UTF_8), Transaction.class);
//				}
//				catch (Exception e) { Util.Log.getLogger().warn("Error dumping record with no pricing due to copy error!", e); }
//				tran.setPricing(null);
//
//				try
//				{
//		        	byte[] jsonString = Util.Mapper.getMapper().writeValueAsBytes(((Edit)Main.getCoreNav().getCurrentMenu()).getTransaction());
//		        	recordJson = new String(jsonString, StandardCharsets.UTF_8);
//				}
//				catch (JsonProcessingException e)
//				{
//					Util.Log.getLogger().warn("An error occurred while converting transaction to Json Data!", e);
//					return;
//				}
//
//				File transactionDump = new File(Main.appDataDir(), tran.getCustomer().getDisplayName() + "-" + tran.getId() + "-NoPricing-Dump.txt");
//				try {
//					transactionDump.createNewFile();
//					FileUtils.writeStringToFile(transactionDump, recordJson, "UTF-8");
//				}
//				catch (IOException e)
//				{
//					Util.Log.getLogger().warn("Error creating and writing to file!", e);
//					return;
//				}
//
//				Util.Log.getLogger().info("Record dump succesful!");
//			}
//			else Util.Log.getLogger().info("No order is open on main window! Load an order before dumping record data!");
//		}
//		else if(command.equalsIgnoreCase("printselfupdatingspecs"))
//		{
//			if(editIsOpen)
//			{
//				Util.Log.getLogger().info("Builds with Self-Updating Specs:");
//
//				Transaction tran = ((Edit)Main.getCoreNav().getCurrentMenu()).getTransaction();
//				for(Build b : tran.getBuilds()) printSelfUpdatingSpecs(b, "");
//			}
//			else Util.Log.getLogger().info("No order is open on main window! Load an order before dumping record data!");
//		}
//		else if(command.equalsIgnoreCase("dumpBuilds"))
//		{
//			if(editIsOpen)
//			{
//				String dump = "All Builds:";
//				for(Build b : ((Edit)Main.getCoreNav().getCurrentMenu()).getTransaction().getAllBuildsIncludingChildren()) { dump += "\n" + b.toString(); }
//				Log.getLogger().info(dump);
//			}
//			else Util.Log.getLogger().info("No order is open on main window! Load an order before dumping buil UIDs!");
//		}
//		else if(command.equalsIgnoreCase("dumpConflicts"))
//		{
//			if(editIsOpen)
//			{
//				String dump = "All Build Conflicts:";
//				for(Conflict c : ((Edit)Main.getCoreNav().getCurrentMenu()).getTransaction().getConflictsReadOnly())
//				{
//					dump += "\n" + c.toString();
//				}
//				Log.getLogger().info(dump);
//			}
//			else Util.Log.getLogger().info("No order is open on main window! Load an order before dumping buil UIDs!");
//		}
//		else if(command.equalsIgnoreCase("loadrecordfromfile"))
//		{
//			if(editIsOpen) Util.Log.getLogger().info("An order cannot already be opened on the main window when trying to load an order from file");
//			else
//			{
//				Util.Log.getLogger().info("Opening file chooser...");
//
//				FileChooser choose = new FileChooser();
//				choose.setTitle("Open Record File");
//				choose.getExtensionFilters().addAll(
//						new ExtensionFilter("Text Files", "*.txt"),
//						new ExtensionFilter("Json Files", "*.json"),
//						new ExtensionFilter("DZNTS Files", "*.dznts"));
//				File selected = choose.showOpenDialog(Main.getCoreNav().getStage());
//
//				try
//				{
//					Transaction record = Util.Mapper.getMapper().readValue(FileUtils.readFileToString(selected, "UTF-8"), Transaction.class);
//					Main.getCoreNav().navigate(Navigation.EDIT, record);
//					Util.Log.getLogger().info("File is valid! Opening...");
//				}
//				catch (Exception e) { Util.Log.getLogger().warn("Error parsing transaction from file!", e); }
//			}
//		}
//		else
		if(Registry.getRegisteredCommands().containsKey(command.toLowerCase()))
		{
			Runnable commandAction = Registry.getRegisteredCommands().get(command.toLowerCase());
			if(commandAction != null)
			{
				Thread thread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try { commandAction.run(); }
						catch (Exception e)
						{
                            Log.getLogger().warn("An error occurred while running registered command {}!", command, e); }
					}
				});
				thread.start();
			}
			else Util.Log.getLogger().info("Unknown console command " + command + "!");
		}
		else Util.Log.getLogger().info("Unknown console command " + command + "!");
	}
	
	private void printSelfUpdatingSpecs(Build b, String indent)
	{
		Util.Log.getLogger().info(indent + b.getTitleProperty().getValue() + ":");
		for(Spec<?> s : b.getSpecs())
		{
			if(s.getCalculateOnDirty() != null) Util.Log.getLogger().info(indent + "\tSelf-Updating Spec: " + s.getId());
		}
		
		for(Build cb : b.getChildBuilds()) printSelfUpdatingSpecs(cb, indent + "\t");
	}
}
