package arden.compiler;

import java.lang.reflect.Modifier;
import java.util.LinkedList;

import arden.codegenerator.FieldReference;
import arden.codegenerator.MethodWriter;
import arden.compiler.node.AIdentObjectAttributeList;
import arden.compiler.node.AListObjectAttributeList;
import arden.compiler.node.PObjectAttributeList;
import arden.compiler.node.TIdentifier;
import arden.runtime.ObjectType;

/**
 * OBJECT Variable.
 * 
 * A static field of type ObjectType is stored in the MLM implementation class.
 * It is set in the static constructor and used for NEW statements.
 */
public class ObjectTypeVariable extends Variable {
	final FieldReference field;

	public ObjectTypeVariable(TIdentifier name, FieldReference field) {
		super(name);
		this.field = field;
	}

	/**
	 * Gets the ObjectTypeVariable for the LHSR, or creates it on demand.
	 */
	public static void create(CodeGenerator codeGen, LeftHandSideResult lhs, PObjectAttributeList attributeList) {
		if (!(lhs instanceof LeftHandSideIdentifier))
			throw new RuntimeCompilerException(lhs.getPosition(), "OBJECT variables must be simple identifiers");
		TIdentifier ident = ((LeftHandSideIdentifier) lhs).identifier;

		FieldReference mlmField = codeGen.createField(ident.getText(), ObjectType.class, Modifier.PRIVATE
				| Modifier.STATIC | Modifier.FINAL);
		codeGen.addVariable(new ObjectTypeVariable(ident, mlmField));

		MethodWriter init = codeGen.getStaticInitializer();
		LinkedList<String> list = getAttributeNames(attributeList);

		// emit: mlmField = new ObjectType(name, new String[] { fieldNames });
		init.newObject(ObjectType.class);
		init.dup();
		init.loadStringConstant(ident.getText());
		init.loadIntegerConstant(list.size());
		init.newArray(String.class);
		int index = 0;
		for (String memberName : list) {
			init.dup();
			init.loadIntegerConstant(index);
			init.loadStringConstant(memberName);
			init.storeObjectToArray();
			index++;
		}
		try {
			init.invokeConstructor(ObjectType.class.getConstructor(String.class, String[].class));
		} catch (NoSuchMethodException e) {
			throw new Error(e);
		}
		init.storeStaticField(mlmField);
	}

	private static LinkedList<String> getAttributeNames(PObjectAttributeList attributeList) {
		// object_attribute_list =
		// {ident} identifier
		// | {list} identifier comma object_attribute_list;
		if (attributeList instanceof AIdentObjectAttributeList) {
			LinkedList<String> list = new LinkedList<String>();
			list.add(((AIdentObjectAttributeList) attributeList).getIdentifier().getText());
			return list;
		} else {
			AListObjectAttributeList attrList = (AListObjectAttributeList) attributeList;
			LinkedList<String> list = getAttributeNames(attrList.getObjectAttributeList());
			String newIdent = attrList.getIdentifier().getText();
			for (String existing : list) {
				if (existing.equalsIgnoreCase(newIdent))
					throw new RuntimeCompilerException(attrList.getIdentifier(), "Duplicate member name: " + newIdent);
			}
			list.addFirst(newIdent);
			return list;
		}
	}
}
