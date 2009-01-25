package org.sjarvela.mollify.client.ui;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.Widget;

public class MouseEventDecorator {
	private static final String HOVER = "hover";
	private static final String PRESSED = "pressed";

	private static MouseListenerAdapter listener;

	static MouseListenerAdapter getListener() {
		if (listener == null) {
			listener = new MouseListenerAdapter() {
				@Override
				public void onMouseDown(Widget sender, int x, int y) {
					sender.addStyleDependentName(PRESSED);
				}

				@Override
				public void onMouseUp(Widget sender, int x, int y) {
					sender.removeStyleDependentName(PRESSED);
				}

				@Override
				public void onMouseEnter(Widget sender) {
					sender.addStyleDependentName(HOVER);
				}

				@Override
				public void onMouseLeave(Widget sender) {
					sender.removeStyleDependentName(HOVER);
				}
			};
		}
		return listener;
	}

	public static void decorate(Label decorated) {
		decorated.addMouseListener(getListener());
	}

}
