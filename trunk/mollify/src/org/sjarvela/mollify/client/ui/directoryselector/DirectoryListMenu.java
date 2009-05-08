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

import java.util.List;

import org.sjarvela.mollify.client.ResourceId;
import org.sjarvela.mollify.client.filesystem.Directory;
import org.sjarvela.mollify.client.filesystem.directorymodel.DirectoryProvider;
import org.sjarvela.mollify.client.localization.TextProvider;
import org.sjarvela.mollify.client.service.ServiceError;
import org.sjarvela.mollify.client.service.request.listener.ResultListener;
import org.sjarvela.mollify.client.ui.StyleConstants;
import org.sjarvela.mollify.client.ui.common.popup.DropdownPopupMenu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;

public class DirectoryListMenu extends DropdownPopupMenu<Directory> implements
		ResultListener<List<Directory>> {
	private final int level;
	private final Directory currentDirectory;
	private final DirectoryProvider directoryProvider;
	private final DirectoryListener listener;
	private final TextProvider textProvider;

	boolean initialized = false;
	boolean dataRequested = false;

	public DirectoryListMenu(String itemStyle, Directory currentDirectory,
			int level, DirectoryProvider directoryProvider,
			DirectoryListener listener, TextProvider textProvider,
			Element parent, Element opener) {
		super(null, parent, opener, null);

		this.level = level;
		this.directoryProvider = directoryProvider;
		this.currentDirectory = currentDirectory;
		this.listener = listener;
		this.textProvider = textProvider;

		this.setStylePrimaryName(StyleConstants.DIRECTORY_LIST_MENU);
		if (itemStyle != null)
			this.addStyleDependentName(itemStyle);
		addItem(createWaitLabel());
	}

	private Label createWaitLabel() {
		Label waitLabel = new Label(textProvider.getStrings()
				.directorySelectorMenuPleaseWait());
		waitLabel.setStyleName(StyleConstants.DIRECTORY_LIST_MENU_WAIT);
		return waitLabel;
	}

	@Override
	protected void onShow() {
		if (!initialized && !dataRequested)
			requestData();
	}

	private void requestData() {
		directoryProvider.getDirectories(currentDirectory, this);
		dataRequested = true;
	}

	public void onFail(ServiceError error) {
		initialized = true;
		removeAllMenuItems();

		Label failedLabel = new Label(error.getType().getMessage(textProvider));
		failedLabel.setStyleName(StyleConstants.DIRECTORY_LIST_MENU_ERROR);
		addItem(failedLabel);
	}

	public void onSuccess(List<Directory> directories) {
		initialized = true;
		removeAllMenuItems();

		int count = 0;
		for (Directory dir : directories) {
			if (dir.getId().equals(this.currentDirectory.getId()))
				continue;
			addMenuAction(null, dir);
			count++;
		}

		if (count == 0)
			addNoDirectoriesLabel();
	}

	private void addNoDirectoriesLabel() {
		Label label = new Label(textProvider.getStrings()
				.directorySelectorMenuNoItemsText());
		label.setStyleName(StyleConstants.DIRECTORY_LIST_MENU_ITEM_NONE);
		addItem(label);
	}

	@Override
	protected Label createMenuItemWidget(final ResourceId action,
			final Directory item) {
		Label label = createMenuItemWidget(item.getName());
		label.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				listener.onChangeToDirectory(level, item);
			}
		});
		return label;
	}
}
