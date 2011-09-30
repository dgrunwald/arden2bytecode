package arden.tests;

import org.junit.Test;
import org.junit.Assert;

import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;

public class RuntimeTests {
	@Test
	public void GetStringFromValue() throws Exception {
		ArdenString string = new ArdenString("str");
		Assert.assertEquals("str", ArdenString.getStringFromValue(string));
		ArdenNumber number = new ArdenNumber(1.0);
		Assert.assertEquals(null, ArdenString.getStringFromValue(number));
	}
}
