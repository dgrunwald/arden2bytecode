package arden.compiler;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import arden.compiler.analysis.DepthFirstAdapter;
import arden.compiler.lexer.Lexer;
import arden.compiler.lexer.LexerException;
import arden.compiler.node.AMlm;
import arden.compiler.node.Start;
import arden.compiler.parser.Parser;
import arden.compiler.parser.ParserException;
import arden.runtime.MedicalLogicModule;

/**
 * The main class of the compiler.
 * 
 * @author Daniel Grunwald
 */
public class Compiler {
	public MedicalLogicModule compileMlm(Reader input) throws CompilerException, IOException {
		List<MedicalLogicModule> output = compile(input);
		if (output.size() != 1)
			throw new CompilerException("Expected only a single MLM per file");
		return output.get(0);
	}

	public List<MedicalLogicModule> compile(Reader input) throws CompilerException, IOException {
		Lexer lexer = new Lexer(new PushbackReader(input, 1024));
		Parser parser = new Parser(lexer);
		Start syntaxTree;
		try {
			syntaxTree = parser.parse();
		} catch (ParserException e) {
			throw new CompilerException(e);
		} catch (LexerException e) {
			throw new CompilerException(e);
		}
		return compile(syntaxTree);
	}

	public List<MedicalLogicModule> compile(Start syntaxTree) {
		final ArrayList<MedicalLogicModule> output = new ArrayList<MedicalLogicModule>();
		// find all AMlm nodes and compile each individually
		syntaxTree.apply(new DepthFirstAdapter() {
			@Override
			public void caseAMlm(AMlm node) {
				output.add(compileMlm(node));
			}
		});
		return output;
	}

	public MedicalLogicModule compileMlm(AMlm node) {
		System.out.println(node.toString());
		node.apply(new PrintTreeVisitor(System.out));
		
		
		return null;
	}
}
