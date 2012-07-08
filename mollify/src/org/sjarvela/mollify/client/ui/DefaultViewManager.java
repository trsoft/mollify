/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sjarvela.mollify.client.App;
import org.sjarvela.mollify.client.js.JsObj;
import org.sjarvela.mollify.client.service.ServiceError;
import org.sjarvela.mollify.client.util.JsUtil;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultViewManager implements ViewManager {
	private static Logger logger = Logger.getLogger(DefaultViewManager.class
			.getName());

	private static final String MOLLIFY_HIDDEN_PANEL_ID = "mollify-hidden-panel";
	private static final String FILEMANAGER_DOWNLOAD_FRAME_ID = "mollify-download-frame";

	private JsObj viewHandlers = null;

	private final RootPanel rootPanel;
	private final Panel hiddenPanel;

	@Inject
	public DefaultViewManager() {
		this.rootPanel = RootPanel.get(App.MOLLIFY_PANEL_ID);
		if (this.rootPanel == null)
			throw new RuntimeException("No placeholder found for Mollify");
		this.rootPanel.getElement().getStyle()
				.setProperty("position", "relative");
		this.hiddenPanel = createHiddenFrame();
	}

	@Override
	public void setViewHandlers(JavaScriptObject handlers) {
		this.viewHandlers = handlers.cast();
	}

	@Override
	public JsObj getViewHandler(String name) {
		if (!viewHandlers.hasValue(name))
			return null;
		return viewHandlers.getJsObj(name);
	}

	@Override
	public void render(ViewHandler view) {
		view.getView().call("render", "mollify");
	}

	public RootPanel getRootPanel() {
		return rootPanel;
	}

	public Panel getHiddenPanel() {
		return hiddenPanel;
	}

	public void openView(Widget view) {
		empty();
		hiddenPanel.clear();

		createDownloadFrame(hiddenPanel);
		rootPanel.add(hiddenPanel);
		rootPanel.insert(view, 0);
	}

	public void empty() {
		rootPanel.clear();
		rootPanel.getElement().setInnerHTML("");
	}

	private Panel createHiddenFrame() {
		Panel panel = new FlowPanel();
		panel.getElement().setId(MOLLIFY_HIDDEN_PANEL_ID);
		panel.getElement()
				.setAttribute("style",
						"visibility:collapse; width: 0px; height: 0px; overflow: hidden;");
		return panel;
	}

	private void createDownloadFrame(Widget panel) {
		Element downloadFrame = DOM.createIFrame();
		downloadFrame
				.setAttribute("style", "visibility:collapse; height: 0px;");
		downloadFrame.setId(FILEMANAGER_DOWNLOAD_FRAME_ID);
		panel.getElement().appendChild(downloadFrame);
	}

	public void openDownloadUrl(String url) {
		if (isMobile()) {
			logger.log(Level.INFO, "Downloading for mobile browser");
			openUrlInNewWindow(url + (url.indexOf("?") >= 0 ? "&" : "?")
					+ "m=1");
		} else
			setFrameUrl(FILEMANAGER_DOWNLOAD_FRAME_ID, url);
	}

	private native boolean isMobile() /*-{
		if (navigator.userAgent.match(/Android/i)
				|| navigator.userAgent.match(/webOS/i)
				|| navigator.userAgent.match(/iPhone/i)
				|| navigator.userAgent.match(/iPod/i)
				|| navigator.userAgent.match(/iPad/i)
				|| navigator.userAgent.match(/Opera Mobi/i))
			return true;
		return false;
	}-*/;

	public void openUrlInNewWindow(String url) {
		Window.open(url, "_blank", "");
	}

	public void showPlainError(String error) {
		empty();
		rootPanel.add(new HTML(error));
	}

	public void showErrorInMainView(String title, ServiceError error) {
		empty();

		StringBuilder errorHtml = new StringBuilder();
		errorHtml
				.append("<span class='mollify-app-error'><p class='title'><b>");
		if (error.getError() != null) {
			errorHtml.append(error.getError().getError());
		} else {
			errorHtml.append(title);
		}
		errorHtml.append("</b></p>");
		errorHtml.append("<p class='details'>").append(error.getDetails())
				.append("</p>");
		if (error.getError() != null
				&& error.getError().getDebugInfo().length() > 0) {
			errorHtml.append("<p class='debug-info'>");
			for (String d : JsUtil.asList(error.getError().getDebugInfo()))
				errorHtml.append(d).append("<br/>");
			errorHtml.append("</p>");
		}
		errorHtml.append("</span>");

		rootPanel.add(new HTML(errorHtml.toString()));
	}

	// @Override
	// public void align(Dialog dialog, Widget p) {
	// if (LogConfiguration.loggingIsEnabled())
	// logger.log(
	// Level.INFO,
	// "Align: p=[" + p.getAbsoluteTop() + ","
	// + p.getAbsoluteLeft() + "/" + p.getOffsetWidth()
	// + "x" + p.getOffsetHeight() + "] root=["
	// + rootPanel.getAbsoluteTop() + ","
	// + rootPanel.getAbsoluteLeft() + "/"
	// + rootPanel.getOffsetWidth() + "x"
	// + rootPanel.getOffsetHeight() + "/"
	// + rootPanel.getElement().getClientWidth() + "x"
	// + rootPanel.getElement().getClientHeight() + "]");
	// int top = (p.getAbsoluteTop() + p.getOffsetHeight() / 2)
	// - (int) (dialog.getOffsetHeight() * 0.75d);
	// top = Math.max(40, top);
	//
	// int maxBottom = rootPanel.getAbsoluteTop()
	// + rootPanel.getElement().getClientHeight() - 40;
	// if (maxBottom > 0 && top + dialog.getOffsetHeight() > maxBottom) {
	// top = Math.max(40, maxBottom - dialog.getOffsetHeight());
	// }
	// dialog.setPopupPosition(dialog.getAbsoluteLeft(), top);
	// }

	/* UTILITIES */

	private native void setFrameUrl(String id, String url) /*-{
		$doc.getElementById(id).src = url;
	}-*/;

}
