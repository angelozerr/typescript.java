package ts.eclipse.ide.ui.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.Bundle;

import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.ImageResource;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.jface.text.HoverLocationListener;
import ts.utils.StringUtils;

public class HTMLTypeScriptPrinter {

	/**
	 * Style sheets content.
	 */
	private static String fgStyleSheet;

	private static RGB colorInfoBackround = null;
	private static RGB colorInfoForeground = null;

	public static void setColorInfoBackround(RGB colorInfoBackround) {
		HTMLTypeScriptPrinter.colorInfoBackround = colorInfoBackround;
	}

	public static void setColorInfoForeground(RGB colorInfoForeground) {
		HTMLTypeScriptPrinter.colorInfoForeground = colorInfoForeground;
	}

	public static String getQuickInfo(String kind, String kindModifiers, String displayString, String documentation) {
		StringBuffer info = new StringBuffer();
		ImageDescriptor descriptor = null; // TypeScriptImagesRegistry.getTypeScriptImageDescriptor(kind,
											// kindModifiers, null);
		startPage(info, null, descriptor);
		if (!StringUtils.isEmpty(displayString)) {
			info.append("<pre class=\"displayString\">");
			info.append(displayString);
			info.append("</pre>");
		}
		if (!StringUtils.isEmpty(documentation)) {
			HTMLPrinter.addParagraph(info, documentation);
		}
		endPage(info);
		return info.toString();
	}

	public static String getError(String message) {
		StringBuffer info = new StringBuffer();
		ImageDescriptor descriptor = null;
		startPage(info, "", descriptor);
		if (!StringUtils.isEmpty(message)) {
			info.append(message);
		}
		endPage(info);
		return info.toString();
	}

	public static void endPage(StringBuffer buffer) {
		HTMLPrinter.insertPageProlog(buffer, 0, colorInfoForeground, colorInfoBackround,
				HTMLTypeScriptPrinter.getStyleSheet());
		HTMLPrinter.addPageEpilog(buffer);
	}

	/**
	 * Returns the Javadoc hover style sheet with the current Javadoc font from
	 * the preferences.
	 * 
	 * @return the updated style sheet
	 * @since 3.4
	 */
	private static String getStyleSheet() {
		if (fgStyleSheet == null) {
			fgStyleSheet = loadStyleSheet("/css/TypeScriptHoverStyleSheet.css"); //$NON-NLS-1$
		}
		String css = fgStyleSheet;
		if (css != null) {
			FontData fontData = JFaceResources.getFontRegistry().getFontData(JFaceResources.DIALOG_FONT)[0];
			css = HTMLPrinter.convertTopLevelFont(css, fontData);
		}

		return css;
	}

	/**
	 * Loads and returns the style sheet associated with either Javadoc hover or
	 * the view.
	 * 
	 * @param styleSheetName
	 *            the style sheet name of either the Javadoc hover or the view
	 * @return the style sheet, or <code>null</code> if unable to load
	 * @since 3.4
	 */
	private static String loadStyleSheet(String styleSheetName) {
		Bundle bundle = Platform.getBundle(TypeScriptUIPlugin.PLUGIN_ID);
		URL styleSheetURL = bundle.getEntry(styleSheetName);
		if (styleSheetURL == null)
			return null;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(styleSheetURL.openStream()));
			StringBuffer buffer = new StringBuffer(1500);
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append('\n');
				line = reader.readLine();
			}

			FontData fontData = JFaceResources.getFontRegistry().getFontData(JFaceResources.DIALOG_FONT)[0];
			return HTMLPrinter.convertTopLevelFont(buffer.toString(), fontData);
		} catch (IOException ex) {
			TypeScriptUIPlugin.log("Error while loading style sheets", ex);
			return ""; //$NON-NLS-1$
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void startPage(StringBuffer buf, String title, ImageDescriptor descriptor) {
		boolean hasTitle = !StringUtils.isEmpty(title);
		String imageSrcPath = getImageURL(descriptor);
		if (!hasTitle && StringUtils.isEmpty(imageSrcPath)) {
			return;
		}
		int imageWidth = 16;
		int imageHeight = 16;
		int labelLeft = 20;
		int labelTop = 2;

		buf.append("<div style='word-wrap: break-word; position: relative; "); //$NON-NLS-1$

		if (imageSrcPath != null) {
			buf.append("margin-left: ").append(labelLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append("padding-top: ").append(labelTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		buf.append("'>"); //$NON-NLS-1$
		if (imageSrcPath != null) {

			String uri = HoverLocationListener.TERN_DEFINITION_PROTOCOL;
			buf.append("<a href=\"");
			buf.append(uri);
			buf.append("\" >");

			StringBuffer imageStyle = new StringBuffer("border:none; position: absolute; "); //$NON-NLS-1$
			imageStyle.append("width: ").append(imageWidth).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("height: ").append(imageHeight).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("left: ").append(-labelLeft - 1).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$

			// hack for broken transparent PNG support in IE 6, see
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=223900 :
			buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n"); //$NON-NLS-1$
			String tooltip = "alt='" + TypeScriptUIMessages.TypeScriptHover_openDeclaration + "' "; //$NON-NLS-1$ //$NON-NLS-2$
																									// $NON-NLS-1$
																									// $NON-NLS-1$
																									// $NON-NLS-1$
																									// $NON-NLS-1$
																									//$NON-NLS-1$ //$NON-NLS-3$
			buf.append("<span ").append(tooltip).append("style=\"").append(imageStyle). //$NON-NLS-1$ //$NON-NLS-2$
					append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='").append(imageSrcPath) //$NON-NLS-1$
					.append("')\"></span>\n"); //$NON-NLS-1$
			buf.append("<![endif]><![endif]-->\n"); //$NON-NLS-1$

			buf.append("<!--[if !IE]>-->\n"); //$NON-NLS-1$
			buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.append(imageSrcPath).append("'/>\n"); //$NON-NLS-1$
			buf.append("<!--<![endif]-->\n"); //$NON-NLS-1$
			buf.append("<!--[if gte IE 7]>\n"); //$NON-NLS-1$
			buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.append(imageSrcPath).append("'/>\n"); //$NON-NLS-1$
			buf.append("<![endif]-->\n"); //$NON-NLS-1$
			// if (element != null) {

			buf.append("</a>"); //$NON-NLS-1$

			// }
		}
		if (hasTitle) {
			buf.append(title);
		}
		buf.append("</div>"); //$NON-NLS-1$
		buf.append("<hr />");
	}

	private static String getImageURL(ImageDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		String imageName = null;
		URL imageUrl = ImageResource.getImageURL(descriptor);
		if (imageUrl != null) {
			imageName = imageUrl.toExternalForm();
		}
		return imageName;
	}
}
