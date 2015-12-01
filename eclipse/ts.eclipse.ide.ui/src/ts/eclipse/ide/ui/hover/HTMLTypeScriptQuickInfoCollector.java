package ts.eclipse.ide.ui.hover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.osgi.framework.Bundle;

import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.server.quickinfo.ITypeScriptQuickInfoCollector;
import ts.utils.StringUtils;

public class HTMLTypeScriptQuickInfoCollector implements ITypeScriptQuickInfoCollector {

	private static String cssStyle;
	private final StringBuffer info;

	public HTMLTypeScriptQuickInfoCollector() {
		this.info = new StringBuffer();
	}

	@Override
	public void setInfo(String kind, String kindModifiers, int startLine, int startOffset, int endLine, int endOffset,
			String displayString, String documentation) {
		HTMLPrinter.insertPageProlog(info, 0, getCSSStyles());
		if (!StringUtils.isEmpty(displayString)) {
			info.append("<h5>");
			info.append(displayString);
			info.append("<h5>");
		}
		if (!StringUtils.isEmpty(documentation)) {
			HTMLPrinter.addParagraph(info, documentation);
		}
		HTMLPrinter.addPageEpilog(info);
	}

	public String getInfo() {
		return info.toString();
	}

	protected String getCSSStyles() {
		if (cssStyle == null) {
			Bundle bundle = Platform.getBundle(TypeScriptUIPlugin.PLUGIN_ID);
			URL url = bundle.getEntry("/css/JavadocHoverStyleSheet.css"); //$NON-NLS-1$
			if (url != null) {
				BufferedReader reader = null;
				try {
					url = FileLocator.toFileURL(url);
					reader = new BufferedReader(new InputStreamReader(url.openStream()));
					StringBuffer buffer = new StringBuffer(200);
					String line = reader.readLine();
					while (line != null) {
						buffer.append(line);
						buffer.append('\n');
						line = reader.readLine();
					}
					cssStyle = buffer.toString();
				} catch (IOException ex) {
				} finally {
					try {
						if (reader != null)
							reader.close();
					} catch (IOException e) {
					}
				}

			}
		}
		String css = cssStyle;
		// if (css != null) {
		// FontData fontData = JFaceResources.getFontRegistry()
		// .getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
		// css = HTMLPrinter.convertTopLevelFont(css, fontData);
		// }
		return css;
	}

}
