/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.viewer.impl;

import org.sjarvela.mollify.client.filesystem.File;
import org.sjarvela.mollify.client.filesystem.JsObj;
import org.sjarvela.mollify.client.localization.TextProvider;
import org.sjarvela.mollify.client.ui.ViewManager;
import org.sjarvela.mollify.client.ui.viewer.FileViewerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultFileViewerFactory implements FileViewerFactory {
	private final TextProvider textProvider;
	private final ViewManager viewManager;

	@Inject
	public DefaultFileViewerFactory(TextProvider textProvider,
			ViewManager viewManager) {
		this.textProvider = textProvider;
		this.viewManager = viewManager;
	}

	@Override
	public void openFileViewer(File file, JsObj viewParams) {
		JsObj embedded = viewParams.getJsObj("embedded");
		String fullUrl = viewParams.getString("full");

		if (embedded != null) {
			String elementId = viewParams.getString("element_id");
			String size = viewParams.getString("size");

			int w = 600;
			int h = 400;
			if (size != null) {
				String[] s = size.split(";");
				w = Integer.parseInt(s[0]);
				h = Integer.parseInt(s[1]);
			}
			new FileViewer(textProvider, viewManager, file.getName(), embedded
					.getString("url"), elementId, w, h, fullUrl).center();
		} else if (fullUrl != null) {
			viewManager.openUrlInNewWindow(fullUrl);
		}

	}
}
