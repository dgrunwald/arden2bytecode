package arden.compiler;

import java.io.PrintStream;

import arden.compiler.analysis.DepthFirstAdapter;
import arden.compiler.node.Node;

/**
 * Used to print the syntax tree for debugging purposes.
 * 
 * @author Daniel Grunwald
 */
public final class PrintTreeVisitor extends DepthFirstAdapter {
	PrintStream out;
	int indentation;
	
	public PrintTreeVisitor(PrintStream out) {
		if (out == null)
			throw new IllegalArgumentException();
		this.out = out;
	}

	private void indent() {
		for (int i = 0; i < indentation; i++)
			out.print('\t');
	}

	@Override
	public void defaultIn(Node node) {
		indent();
		out.println(node.getClass().getName());
		indentation++;
	}

	@Override
	public void defaultOut(Node node) {
		indentation--;
	}
}
