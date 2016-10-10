package ts.internal.client.protocol;

import org.junit.Assert;
import org.junit.Test;

import ts.client.CommandNames;


public class CommandNamesTest {

	@Test
	public void testLess() {
		// SemanticDiagnosticsSync is available only since 2.0.3
		boolean result = CommandNames.SemanticDiagnosticsSync.canSupport("1.8.10");
		Assert.assertFalse(result);
	}
	
	@Test
	public void testEqual() {
		// SemanticDiagnosticsSync is available only since 2.0.3
		boolean result = CommandNames.SemanticDiagnosticsSync.canSupport("2.0.3");
		Assert.assertTrue(result);
	}
	
	@Test
	public void testSup() {
		// SemanticDiagnosticsSync is available only since 2.0.3
		boolean result = CommandNames.SemanticDiagnosticsSync.canSupport("2.0.6");
		Assert.assertTrue(result);
	}
}
