package arden.tests;

import org.junit.Assert;
import org.junit.Test;

import arden.MainClass;

/** tests for the command line interface (CLI) */
public class CliTests {
	@Test
	public void basenameTest() throws Exception {
		Assert.assertEquals("basename", MainClass.getFilenameBase("sdvnj/\\$%&../\\//./4e5\\v.s..df.v/basename.bfgb"));
		Assert.assertEquals("", MainClass.getFilenameBase("sdvnj/\\$%&../\\//./4e5\\v.s..df.v/..fg.bfgb\\"));
		Assert.assertEquals(".", MainClass.getFilenameBase(".."));
	}
}
