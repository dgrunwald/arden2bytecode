package arden.runtime;

import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arden.CommandLineOptions;
import arden.constants.ConstantParser;
import arden.constants.ConstantParser.ConstantParserException;

public class StdIOExecutionContext extends ExecutionContext {
	@SuppressWarnings("unused")
	private CommandLineOptions options;
	
	public StdIOExecutionContext(CommandLineOptions options) {
		this.options = options;
	}
	
	public DatabaseQuery createQuery(String mapping) {
		System.out.println("Query mapping: \"" + mapping + "\". Enter result as " +
				"constant Arden Syntax expression (Strings in quotes)");
		System.out.print(" >");
		Scanner sc = new Scanner(System.in);
		String line = null;
		if (sc.hasNext()){
			line = sc.nextLine();
		}
		ArdenValue[] val = null;
		try {
			val = new ArdenValue[] {
				ConstantParser.parse(line)
			};
		} catch (ConstantParserException e) {
			System.out.println("Error parsing at char: " + e.getPos());
			System.out.println("Message: " + e.getMessage());
		}
		return new MemoryQuery(val);
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
			System.out.print("Destination: \"");
			System.out.print(destination);
			System.out.print("\" Message: ");
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
