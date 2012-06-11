package arden.runtime;

import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arden.CommandLineOptions;

public class StdIOExecutionContext extends ExecutionContext {
	@SuppressWarnings("unused")
	private CommandLineOptions options;
	private static final Pattern ARDEN_NUMBER_PATTERN = 
			Pattern.compile("(?:[0-9]+(?:\\.[0-9]*)?)|(?:\\.[0-9]+)");	
	
	public StdIOExecutionContext(CommandLineOptions options) {
		this.options = options;
	}
	
	public DatabaseQuery createQuery(String mapping) {
		System.out.print("Query \"" + mapping + "\". Enter result: ");
		Scanner sc = new Scanner(System.in);
		String line = null;
		if (sc.hasNext()) {
			line = sc.nextLine();
		}
		Matcher m = ARDEN_NUMBER_PATTERN.matcher(line);
		if (m.matches()) {
			ArdenValue[] val = new ArdenValue[] {
					new ArdenNumber(
							Double.parseDouble(
									line.trim()))
			};
			return new MemoryQuery(val);
		} else {
			// TODO: implement better parser for values that are non-numbers
			ArdenValue[] val = new ArdenValue[]{
					new ArdenString(line)
			};
			return new MemoryQuery(val);
		}
	}
	
	public ArdenValue getMessage(String mapping) {
		System.out.println("Message, mapping: " + mapping);
		return new ArdenString(mapping);
	}
	
	public void write(ArdenValue message, String destination) {
		if ("stdout".equalsIgnoreCase(destination)) {
			// just print string
			if (message instanceof ArdenString) {
				System.out.println(ArdenString.getStringFromValue(message));
			} else {
				System.out.println(message);
			}
		} else {
			// prepend destination to printed string
			System.out.print("[");
			System.out.print(destination);
			System.out.print("]: ");
			if (message instanceof ArdenString) {
				System.out.println(ArdenString.getStringFromValue(message));
			} else {
				System.out.println(message);
			}
		}
	}
	
	public ArdenRunnable findModule(String name, String institution) {
		throw new RuntimeException("findModule not implemented");
	}
	
	public ArdenRunnable findInterface(String mapping) {
		throw new RuntimeException("findInterface not implemented");
	}
	
	public void callWithDelay(ArdenRunnable mlm, ArdenValue[] arguments, ArdenValue delay) {
		throw new RuntimeException("callWithDelay not implemented");
	}
	
	private ArdenTime eventtime = new ArdenTime(new Date());
	
	public ArdenTime getEventTime() {
		return eventtime;
	}

	public ArdenTime getTriggerTime() {
		return eventtime;
	}

	public ArdenTime getCurrentTime() {
		return new ArdenTime(new Date());
	}
}
