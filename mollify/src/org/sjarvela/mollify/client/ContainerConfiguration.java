/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client;

import org.sjarvela.mollify.client.filesystem.directorymodel.FileSystemItemProvider;
import org.sjarvela.mollify.client.localization.DefaultTextProvider;
import org.sjarvela.mollify.client.localization.TextProvider;
import org.sjarvela.mollify.client.service.environment.ServiceEnvironment;
import org.sjarvela.mollify.client.session.ClientSettings;
import org.sjarvela.mollify.client.session.DefaultSessionManager;
import org.sjarvela.mollify.client.session.ParameterParser;
import org.sjarvela.mollify.client.session.SessionManager;
import org.sjarvela.mollify.client.session.SessionProvider;
import org.sjarvela.mollify.client.session.file.DefaultFileSystemItemProvider;
import org.sjarvela.mollify.client.session.user.DefaultPasswordGenerator;
import org.sjarvela.mollify.client.session.user.PasswordGenerator;
import org.sjarvela.mollify.client.ui.DefaultViewManager;
import org.sjarvela.mollify.client.ui.ViewManager;
import org.sjarvela.mollify.client.ui.configuration.ConfigurationDialogFactory;
import org.sjarvela.mollify.client.ui.configuration.DefaultConfigurationDialogFactory;
import org.sjarvela.mollify.client.ui.dialog.DefaultDialogManager;
import org.sjarvela.mollify.client.ui.dialog.DialogManager;
import org.sjarvela.mollify.client.ui.fileupload.FileUploadDialogFactory;
import org.sjarvela.mollify.client.ui.fileupload.flash.FlashFileUploadDialogFactory;
import org.sjarvela.mollify.client.ui.fileupload.http.HttpFileUploadDialogFactory;
import org.sjarvela.mollify.client.ui.itemselector.DefaultItemSelectorFactory;
import org.sjarvela.mollify.client.ui.itemselector.ItemSelectorFactory;
import org.sjarvela.mollify.client.ui.login.DefaultUiSessionManager;
import org.sjarvela.mollify.client.ui.login.UiSessionManager;
import org.sjarvela.mollify.client.ui.mainview.MainViewFactory;
import org.sjarvela.mollify.client.ui.mainview.impl.DefaultMainViewFactory;
import org.sjarvela.mollify.client.ui.password.DefaultPasswordDialogFactory;
import org.sjarvela.mollify.client.ui.password.PasswordDialogFactory;
import org.sjarvela.mollify.client.ui.permissions.DefaultPermissionEditorViewFactory;
import org.sjarvela.mollify.client.ui.permissions.PermissionEditorViewFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ContainerConfiguration extends AbstractGinModule {
	static final String META_PROPERTY = "mollify:property";

	static final String PARAM_FILE_UPLOADER = "file-uploader";
	static final String VALUE_FILE_UPLOADER_FLASH = "flash";
	static final String PARAM_FLASH_UPLOADER_SRC = "flash-uploader-src";

	@Override
	protected void configure() {
		bind(TextProvider.class).to(DefaultTextProvider.class);
		bind(ViewManager.class).to(DefaultViewManager.class);
		bind(MainViewFactory.class).to(DefaultMainViewFactory.class);
		bind(DialogManager.class).to(DefaultDialogManager.class);
		bind(ItemSelectorFactory.class).to(DefaultItemSelectorFactory.class);
		bind(ConfigurationDialogFactory.class).to(
				DefaultConfigurationDialogFactory.class);
		bind(PasswordDialogFactory.class)
				.to(DefaultPasswordDialogFactory.class);
		bind(FileSystemItemProvider.class).to(
				DefaultFileSystemItemProvider.class);
		bind(PermissionEditorViewFactory.class).to(
				DefaultPermissionEditorViewFactory.class);
		bind(SessionManager.class).to(DefaultSessionManager.class);
		bind(PasswordGenerator.class).to(DefaultPasswordGenerator.class);
		bind(UiSessionManager.class).to(DefaultUiSessionManager.class);
		bind(Client.class).to(MollifyClient.class);
	}

	@Provides
	@Singleton
	SessionProvider getSessionProvider(SessionManager sessionManager) {
		return sessionManager;
	}

	@Provides
	@Singleton
	ClientSettings getClientSettings() {
		return new ClientSettings(new ParameterParser(META_PROPERTY));
	}

	@Provides
	@Singleton
	ServiceEnvironment getEnvironment(ClientSettings clientSettings) {
		ServiceEnvironment env = GWT.create(ServiceEnvironment.class);
		env.initialize(clientSettings);
		return env;
	}

	@Provides
	@Singleton
	FileUploadDialogFactory getFileUploadDialogFactory(ServiceEnvironment env,
			ClientSettings settings, TextProvider textProvider,
			SessionProvider sessionProvider, DialogManager dialogManager) {
		if (VALUE_FILE_UPLOADER_FLASH.equalsIgnoreCase(settings
				.getString(PARAM_FILE_UPLOADER)))
			return new FlashFileUploadDialogFactory(textProvider, env
					.getFileUploadService(), sessionProvider, settings
					.getString(PARAM_FLASH_UPLOADER_SRC));

		return new HttpFileUploadDialogFactory(env, textProvider, env
				.getFileUploadService(), sessionProvider, dialogManager);
	}

}
