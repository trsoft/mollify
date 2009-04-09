/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.dialog.configuration.folders;

import org.sjarvela.mollify.client.ResourceId;
import org.sjarvela.mollify.client.localization.TextProvider;
import org.sjarvela.mollify.client.ui.ActionListener;
import org.sjarvela.mollify.client.ui.StyleConstants;
import org.sjarvela.mollify.client.ui.common.ActionButton;
import org.sjarvela.mollify.client.ui.dialog.configuration.ConfigurationSettingsView;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class ConfigurationSettingsFoldersView extends ConfigurationSettingsView {
	private final DirectoryList list;
	private final ActionButton addFolderButton;
	private final ActionButton removeFolderButton;

	public enum Actions implements ResourceId {
		addFolder, removeFolder
	}

	public ConfigurationSettingsFoldersView(TextProvider textProvider,
			ActionListener actionListener) {
		super(textProvider, StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS);

		list = new DirectoryList(textProvider,
				StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS_LIST);

		addFolderButton = new ActionButton(textProvider.getStrings()
				.configurationDialogSettingFoldersAdd(),
				StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS_ACTION_ADD,
				StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS_ACTION);
		addFolderButton.setAction(actionListener, Actions.addFolder);

		removeFolderButton = new ActionButton(textProvider.getStrings()
				.configurationDialogSettingFoldersRemove(),
				StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS_ACTION_REMOVE,
				StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS_ACTION);
		removeFolderButton.setAction(actionListener, Actions.removeFolder);

		add(createList());
		add(createButtons());
	}

	private Widget createList() {
		Panel panel = new FlowPanel();
		panel
				.setStyleName(StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS_LIST_PANEL);
		panel.add(list);
		return panel;
	}

	private Widget createButtons() {
		Panel userActions = new FlowPanel();
		userActions
				.setStyleName(StyleConstants.CONFIGURATION_DIALOG_VIEW_FOLDERS_ACTIONS);
		userActions.add(addFolderButton);
		userActions.add(removeFolderButton);
		return userActions;
	}

	@Override
	public String getTitle() {
		return textProvider.getStrings()
				.configurationDialogSettingFoldersViewTitle();
	}

	public DirectoryList list() {
		return list;
	}

	public ActionButton addFolderButton() {
		return addFolderButton;
	}

	public ActionButton removeFolderButton() {
		return removeFolderButton;
	}

}
