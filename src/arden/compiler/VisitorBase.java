package arden.compiler;

import arden.compiler.analysis.AnalysisAdapter;
import arden.compiler.node.Node;

/** 
 * Base class for visitors. Throws an exception for unknown nodes.
 * 
 * @author Daniel Grunwald
 *
 */
class VisitorBase extends AnalysisAdapter {

	@Override
	public void defaultCase(Node node) {
		throw new RuntimeException("Unsupported node: " + node.getClass().getName());
	}
}
