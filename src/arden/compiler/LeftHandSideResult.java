package arden.compiler;

import arden.compiler.node.TIdentifier;
import arden.compiler.node.TNow;
import arden.compiler.node.Token;

/**
 * Represents the result of a LeftHandSideAnalyzer-run.
 * 
 * @author Daniel Grunwald
 * 
 */
abstract class LeftHandSideResult {
	public abstract Token getPosition();
}

final class LeftHandSideIdentifier extends LeftHandSideResult {
	public final TIdentifier identifier;

	public LeftHandSideIdentifier(TIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Token getPosition() {
		return identifier;
	}
}

final class LeftHandSideTimeOfIdentifier extends LeftHandSideResult {
	public final TIdentifier identifier;

	public LeftHandSideTimeOfIdentifier(TIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public Token getPosition() {
		return identifier;
	}
}

final class LeftHandSideNow extends LeftHandSideResult {
	private final TNow now;

	public LeftHandSideNow(TNow now) {
		this.now = now;
	}

	@Override
	public Token getPosition() {
		return now;
	}
}

//class LeftHandSideTimeOfIdentifier extends LeftHandSideResult {

//}
