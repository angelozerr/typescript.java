package org.eclipse.swt.custom.patch;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.provisional.ILineSpacingProvider;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;

import javassist.util.proxy.ProxyFactory;

public class StyledTextPatcher {

	private static Field RENDERER_FIELD;

	/**
	 * This method should be replaced with
	 * StyledText#setLineSpacingProvider(ILineSpacingProvider
	 * lineSpacingProvider);
	 * 
	 * @param styledText
	 * @param lineSpacingProvider
	 * @throws Exception
	 */
	public static void setLineSpacingProvider(StyledText styledText, ILineSpacingProvider lineSpacingProvider)
			throws Exception {

		// 1) As it's not possible to override StyledTextRenderer#getTextLayout,
		// StyledTextRenderer#drawLine methods,
		// recreate an instance of StyledTextRenderer with Javassist
		Object styledTextRenderer = createStyledTextRenderer(styledText, lineSpacingProvider);

		// 2) Reinitialize the renderer like StyledText constructor does.
		// renderer = new StyledTextRenderer(getDisplay(), this);
		// renderer.setContent(content);
		// renderer.setFont(getFont(), tabLength);
		initialize(styledTextRenderer, styledText);

		// 3) Set the the new renderer
		getRendererField(styledText).set(styledText, styledTextRenderer);
	}

	private static /* org.eclipse.swt.custom.StyledTextRenderer */ Object createStyledTextRenderer(
			StyledText styledText, ILineSpacingProvider lineSpacingProvider) throws Exception {
		// get the org.eclipse.swt.custom.StyledTextRenderer instance of
		// StyledText
		/* org.eclipse.swt.custom.StyledTextRenderer */ Object originalRenderer = getRendererField(styledText)
				.get(styledText);

		// Create a Javassist proxy
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(originalRenderer.getClass());
		StyledTextRenderer renderer = new StyledTextRenderer(styledText);
		renderer.setLineSpacingProvider(lineSpacingProvider);
		factory.setHandler(renderer);
		return factory.create(new Class[] { Device.class, StyledText.class },
				new Object[] { styledText.getDisplay(), styledText });
	}

	private static void initialize(Object styledTextRenderer, StyledText styledText)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// renderer.setContent(content);
		Method m1 = getSetContentMethod(styledTextRenderer);
		m1.invoke(styledTextRenderer, styledText.getContent());
		// renderer.setFont(getFont(), tabLength);
		Method m2 = getSetFontMethod(styledTextRenderer);
		m2.invoke(styledTextRenderer, styledText.getFont(), 4);
	}

	private static Method getSetContentMethod(Object styledTextRenderer) throws NoSuchMethodException {
		// if (SET_CONTENT_METHOD == null) {
		Method SET_CONTENT_METHOD = styledTextRenderer.getClass().getDeclaredMethod("setContent",
				new Class[] { StyledTextContent.class });
		SET_CONTENT_METHOD.setAccessible(true);
		// }
		return SET_CONTENT_METHOD;
	}

	private static Method getSetFontMethod(Object styledTextRenderer) throws NoSuchMethodException {
		// if (SET_FONT_METHOD == null) {
		Method SET_FONT_METHOD = styledTextRenderer.getClass().getDeclaredMethod("setFont",
				new Class[] { Font.class, int.class });
		SET_FONT_METHOD.setAccessible(true);
		// }
		return SET_FONT_METHOD;
	}

	private static Field getRendererField(StyledText styledText) throws Exception {
		if (RENDERER_FIELD == null) {
			RENDERER_FIELD = styledText.getClass().getDeclaredField("renderer");
			RENDERER_FIELD.setAccessible(true);
		}
		return RENDERER_FIELD;
	}
}
