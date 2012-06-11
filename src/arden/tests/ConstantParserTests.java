package arden.tests;

import junit.framework.Assert;

import org.junit.Test;

import arden.constants.ConstantParser;
import arden.runtime.ArdenList;
import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenValue;

public class ConstantParserTests {
	@Test
	public void testConstantParser() throws Exception {
		Assert.assertEquals(ArdenList.EMPTY, ConstantParser.parse("()"));
		Assert.assertEquals(ArdenNumber.create(5.0, ArdenValue.NOPRIMARYTIME), ConstantParser.parse("5.0"));
		Assert.assertFalse(ConstantParser.parse("5.0").equals(ArdenNumber.create(4.0, ArdenValue.NOPRIMARYTIME)));
		Assert.assertEquals(new ArdenList(new ArdenValue[]{
				ArdenNumber.create(1.0, ArdenValue.NOPRIMARYTIME)}), 
				ConstantParser.parse("(,1)"));
		Assert.assertEquals(new ArdenList(new ArdenValue[]{
				ArdenNumber.create(1.0, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(2.0, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(3.0, ArdenValue.NOPRIMARYTIME)
				}), 
				ConstantParser.parse("(1,2,3)"));
		Assert.assertEquals(new ArdenList(new ArdenValue[]{
				ArdenNumber.create(1.0, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(2.1, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(2.2, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(2.3, ArdenValue.NOPRIMARYTIME),
				ArdenNumber.create(3.0, ArdenValue.NOPRIMARYTIME)
				}), 
				ConstantParser.parse("(1,(2.1,2.2,2.3),3)"));
		Assert.assertEquals(new ArdenString("vfs\"dkj"), ConstantParser.parse("\"vfs\"\"dkj\""));
	}
}
