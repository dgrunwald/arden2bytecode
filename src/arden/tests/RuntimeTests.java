package arden.tests;

import java.util.Comparator;

import org.junit.Test;
import org.junit.Assert;

import arden.runtime.ArdenNumber;
import arden.runtime.ArdenString;
import arden.runtime.ArdenTime;

public class RuntimeTests {
	@Test
	public void GetStringFromValue() throws Exception {
		ArdenString string = new ArdenString("str");
		Assert.assertEquals("str", ArdenString.getStringFromValue(string));
		ArdenNumber number = new ArdenNumber(1.0);
		Assert.assertEquals(null, ArdenString.getStringFromValue(number));
	}
	
	@Test
	public void ArdenTimeComparatorTest() throws Exception {
		ArdenTime t1 = new ArdenTime(2406808345L);
		ArdenTime t2 = new ArdenTime(7575475677346L);
		Assert.assertTrue(t1.compareTo(null) < 0);
		Comparator<ArdenTime> c = new ArdenTime.NaturalComparator();
		Assert.assertTrue(c.compare(t1, t2) < 0);
		Assert.assertTrue(c.compare(t2, t1) > 0);
		Assert.assertTrue(c.compare(t2, null) < 0);
		Assert.assertTrue(c.compare(null, t2) > 0);
	}
}
