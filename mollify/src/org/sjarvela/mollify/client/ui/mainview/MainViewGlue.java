/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.mainview;

import org.sjarvela.mollify.client.data.Directory;
import org.sjarvela.mollify.client.data.File;
import org.sjarvela.mollify.client.ui.filelist.Column;
import org.sjarvela.mollify.client.ui.filelist.SimpleFileListListener;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

public class MainViewGlue implements SimpleFileListListener {
	private final MainView view;
	private final MainViewPresenter presenter;

	public MainViewGlue(MainView view, MainViewPresenter presenter) {
		this.view = view;
		this.presenter = presenter;

		view.addFileListListener(this);
		initializeActions();
	}

	private void initializeActions() {
		view.getRefreshButton().addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				presenter.refresh();
			}
		});

		view.getParentDirButton().addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				presenter.moveToParentDirectory();
			}
		});

		view.getUploadFileButton().addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				presenter.openUploadDialog();
			}
		});
	}

	public void onDirectoryRowClicked(Directory directory, Column column) {
		if (column.equals(Column.NAME)) {
			presenter.changeDirectory(directory);
		}
	}

	public void onDirectoryUpRowClicked(Column column) {
		presenter.moveToParentDirectory();
	}

	public void onFileRowClicked(File file, Column column) {
		if (column.equals(Column.NAME)) {
			view.showFileDetails(file);
		}
	}
}
