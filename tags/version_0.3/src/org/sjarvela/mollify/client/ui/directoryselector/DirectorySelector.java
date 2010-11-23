/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.directoryselector;

import java.util.ListIterator;

import org.sjarvela.mollify.client.DirectoryController;
import org.sjarvela.mollify.client.DirectoryProvider;
import org.sjarvela.mollify.client.data.Directory;
import org.sjarvela.mollify.client.localization.Localizator;
import org.sjarvela.mollify.client.ui.StyleConstants;
import org.sjarvela.mollify.client.ui.filemanager.FileManagerModel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class DirectorySelector extends HorizontalPanel {
	private FileManagerModel model;
	private DirectoryController directoryController;
	private DirectoryProvider directoryProvider;
	private Localizator localizator;

	public DirectorySelector(FileManagerModel model, Localizator localizator) {
		this.model = model;
		this.localizator = localizator;

		this.setStyleName(StyleConstants.DIRECTORY_SELECTOR);
	}

	public void setDirectoryController(DirectoryController directoryController) {
		this.directoryController = directoryController;
	}

	public void setDirectoryProvider(DirectoryProvider directoryProvider) {
		this.directoryProvider = directoryProvider;
	}

	public void refresh() {
		this.clear();

		ListIterator<Directory> list = model.getDirectoryModel()
				.getDirectoryList();
		int level = 0;
		Directory parent = Directory.Empty();

		while (list.hasNext()) {
			Directory current = list.next();
			this.add(new DirectoryListItem(current, level, parent,
					directoryController, directoryProvider, localizator));
			if (list.hasNext())
				addSeparator();

			level++;
			parent = current;
		}
	}

	private void addSeparator() {
		Label separator = new Label(localizator.getStrings().directorySelectorSeparatorLabel());
		separator.setStyleName(StyleConstants.DIRECTORY_SELECTOR_SEPARATOR);
		this.add(separator);
	}
}