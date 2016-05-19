package ts.cmd.tslint;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import ts.client.Location;
import ts.cmd.ITypeScriptLinterHandler;
import ts.cmd.Severity;

public class TslintHelperTest {

	class TslintError {

		public final String name;
		public final Location startLoc;
		public final Location endLoc;
		public final Severity severity;
		public final String ruleName;
		public final String failure;

		public TslintError(String name, Location startLoc, Location endLoc, Severity severity, String ruleName,
				String failure) {
			this.name = name;
			this.startLoc = startLoc;
			this.endLoc = endLoc;
			this.severity = severity;
			this.ruleName = ruleName;
			this.failure = failure;
		}

	}

	class TslintErrors extends ArrayList<TslintError> implements ITypeScriptLinterHandler {

		public TslintErrors() {
		}

		@Override
		public void addError(String file, Location startLoc, Location endLoc, Severity severity, String code,
				String message) {
			super.add(new TslintError(file, startLoc, endLoc, severity, code, message));
		}

	}

	@Test
	public void tslintVerboseFormat() throws Exception {
		TslintErrors errors = new TslintErrors();
		TslintHelper.processVerboseMessage(
				"(no-var-keyword) sample.ts[1, 1]: forbidden 'var' keyword, use 'let' or 'const' instead", errors);
		Assert.assertEquals(errors.size(), 1);
		TslintError error = errors.get(0);
		Assert.assertEquals(error.ruleName, "no-var-keyword");
		Assert.assertEquals(error.name, "sample.ts");
		Assert.assertEquals(error.startLoc.getLine(), 1);
		Assert.assertEquals(error.startLoc.getOffset(), 1);
		Assert.assertEquals(error.failure, "forbidden 'var' keyword, use 'let' or 'const' instead");
	}

	@Test
	public void tslintJsonFormat() throws Exception {
		TslintErrors errors = new TslintErrors();
		TslintHelper.processJsonMessage("[{\"endPosition\":{\"character\":3,\"line\":0,\"position\":3},"
				+ "\"failure\":\"forbidden 'var' keyword, use 'let' or 'const' instead\"," + "\"name\":\"sample.ts\","
				+ "\"ruleName\":\"no-var-keyword\","
				+ "\"startPosition\":{\"character\":0,\"line\":0,\"position\":0}}]", errors);
		Assert.assertEquals(errors.size(), 1);
		TslintError error = errors.get(0);
		Assert.assertEquals(error.ruleName, "no-var-keyword");
		Assert.assertEquals(error.name, "sample.ts");
		Assert.assertEquals(error.failure, "forbidden 'var' keyword, use 'let' or 'const' instead");
		Assert.assertNotNull(error.startLoc);
		Assert.assertEquals(error.startLoc.getLine(), 0);
		Assert.assertEquals(error.startLoc.getOffset(), 0);
		Assert.assertEquals(error.startLoc.getPosition(), 0);
		Assert.assertNotNull(error.endLoc);
		Assert.assertEquals(error.endLoc.getLine(), 0);
		Assert.assertEquals(error.endLoc.getOffset(), 3);
		Assert.assertEquals(error.endLoc.getPosition(), 3);
	}
}
