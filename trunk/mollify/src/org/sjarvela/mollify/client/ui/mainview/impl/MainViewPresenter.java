/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.mainview.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sjarvela.mollify.client.Callback;
import org.sjarvela.mollify.client.event.EventDispatcher;
import org.sjarvela.mollify.client.filesystem.File;
import org.sjarvela.mollify.client.filesystem.FileSystemAction;
import org.sjarvela.mollify.client.filesystem.FileSystemItem;
import org.sjarvela.mollify.client.filesystem.Folder;
import org.sjarvela.mollify.client.filesystem.FolderInfo;
import org.sjarvela.mollify.client.filesystem.SearchResult;
import org.sjarvela.mollify.client.filesystem.handler.FileSystemActionHandler;
import org.sjarvela.mollify.client.filesystem.handler.FolderHandler;
import org.sjarvela.mollify.client.localization.TextProvider;
import org.sjarvela.mollify.client.localization.Texts;
import org.sjarvela.mollify.client.service.ConfigurationService;
import org.sjarvela.mollify.client.service.FileSystemService;
import org.sjarvela.mollify.client.service.ServiceError;
import org.sjarvela.mollify.client.service.ServiceErrorType;
import org.sjarvela.mollify.client.service.SessionService;
import org.sjarvela.mollify.client.service.request.listener.ResultListener;
import org.sjarvela.mollify.client.session.SessionManager;
import org.sjarvela.mollify.client.session.user.PasswordHandler;
import org.sjarvela.mollify.client.ui.ViewManager;
import org.sjarvela.mollify.client.ui.common.grid.GridComparator;
import org.sjarvela.mollify.client.ui.common.grid.SelectController;
import org.sjarvela.mollify.client.ui.common.grid.Sort;
import org.sjarvela.mollify.client.ui.dialog.CreateFolderDialogFactory;
import org.sjarvela.mollify.client.ui.dialog.DialogManager;
import org.sjarvela.mollify.client.ui.dialog.InputListener;
import org.sjarvela.mollify.client.ui.dialog.WaitDialog;
import org.sjarvela.mollify.client.ui.dnd.DragDataProvider;
import org.sjarvela.mollify.client.ui.dropbox.DropBox;
import org.sjarvela.mollify.client.ui.filelist.DefaultFileItemComparator;
import org.sjarvela.mollify.client.ui.filelist.FileList;
import org.sjarvela.mollify.client.ui.fileupload.FileUploadDialogFactory;
import org.sjarvela.mollify.client.ui.folderselector.FolderListener;
import org.sjarvela.mollify.client.ui.password.PasswordDialogFactory;
import org.sjarvela.mollify.client.ui.permissions.PermissionEditorViewFactory;
import org.sjarvela.mollify.client.ui.searchresult.SearchResultDialogFactory;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class MainViewPresenter implements FolderListener, PasswordHandler,
		DragDataProvider<FileSystemItem>, SearchListener {
	private static Logger logger = Logger.getLogger(MainViewPresenter.class
			.getName());

	private final MainViewModel model;
	private final DefaultMainView view;
	private final DialogManager dialogManager;
	private final SessionManager sessionManager;
	private final SessionService sessionService;

	private final FileSystemService fileSystemService;
	private final ConfigurationService configurationService;
	private final FileSystemActionHandler fileSystemActionHandler;
	private final TextProvider textProvider;
	private final PermissionEditorViewFactory permissionEditorViewFactory;
	private final PasswordDialogFactory passwordDialogFactory;
	private final FileUploadDialogFactory fileUploadDialogFactory;
	private final CreateFolderDialogFactory createFolderDialogFactory;
	private final ViewManager viewManager;
	private final DropBox dropBox;
	private final EventDispatcher eventDispatcher;
	private final SearchResultDialogFactory searchResultDialogFactory;

	private final boolean exposeFileUrls;

	public MainViewPresenter(DialogManager dialogManager,
			ViewManager viewManager, SessionManager sessionManager,
			MainViewModel model, DefaultMainView view,
			ConfigurationService configurationService,
			FileSystemService fileSystemService, TextProvider textProvider,
			FileSystemActionHandler fileSystemActionHandler,
			PermissionEditorViewFactory permissionEditorViewFactory,
			PasswordDialogFactory passwordDialogFactory,
			FileUploadDialogFactory fileUploadDialogFactory,
			CreateFolderDialogFactory createFolderDialogFactory,
			DropBox dropBox, boolean exposeFileUrls,
			SessionService sessionService, EventDispatcher eventDispatcher,
			SearchResultDialogFactory searchResultDialogFactory) {
		this.dialogManager = dialogManager;
		this.viewManager = viewManager;
		this.sessionManager = sessionManager;
		this.configurationService = configurationService;
		this.fileSystemService = fileSystemService;
		this.sessionService = sessionService;

		this.model = model;
		this.view = view;
		this.textProvider = textProvider;
		this.fileSystemActionHandler = fileSystemActionHandler;
		this.permissionEditorViewFactory = permissionEditorViewFactory;
		this.passwordDialogFactory = passwordDialogFactory;
		this.fileUploadDialogFactory = fileUploadDialogFactory;
		this.createFolderDialogFactory = createFolderDialogFactory;
		this.dropBox = dropBox;
		this.exposeFileUrls = exposeFileUrls;
		this.eventDispatcher = eventDispatcher;
		this.searchResultDialogFactory = searchResultDialogFactory;

		this.view.getFileContext().setActionHandler(fileSystemActionHandler);

		this.view.getFolderSelector().addListener(this);
		this.view
				.setListSelectController(new SelectController<FileSystemItem>() {
					@Override
					public boolean isSelectable(FileSystemItem t) {
						if (t.isFile())
							return true;
						return !Folder.Parent.equals(t)
								&& !((Folder) t).isRoot();
					}

				});
		this.setListOrder(FileList.COLUMN_ID_NAME, Sort.asc);

		if (model.getSession().isAuthenticationRequired())
			view.getUsername().setText(model.getSession().getLoggedUser());

		view.addSearchListener(this);
	}

	public void initialize() {
		if (exposeFileUrls)
			viewManager.getHiddenPanel().add(view.createFileUrlContainer());

		changeToRootFolder(model.getRootFolders().size() == 1 ? model
				.getRootFolders().get(0) : null);
		if (model.getRootFolders().size() == 0)
			view.hideButtons();
	}

	public void onFileSystemItemSelected(FileSystemItem item, String columnId) {
		if (columnId.equals(FileList.COLUMN_ID_NAME)) {
			if (item.isFile()) {
				view.showFileContext((File) item);
			} else {
				view.showProgress();

				Folder folder = (Folder) item;

				if (folder == Folder.Parent)
					onMoveToParentFolder();
				else
					changeToFolder(folder);
			}
		}
	}

	public void changeToRootFolder(Folder root) {
		view.showProgress();
		model.changeToRootFolder(root, createFolderChangeListener());
	}

	public void changeToFolder(Folder folder) {
		view.showProgress();
		model.changeToSubfolder(folder, createFolderChangeListener());
	}

	public void reset() {
		view.clear();
	}

	public void reload() {
		view.showProgress();

		model.refreshData(new ResultListener<FolderInfo>() {
			public void onFail(ServiceError error) {
				view.hideProgress();
				onError(error, false);
			}

			public void onSuccess(FolderInfo result) {
				refreshView();
				view.hideProgress();
			}
		});
	}

	private void refreshView() {
		List<FileSystemItem> allFileItems = model.getAllItems();
		if (model.getFolderModel().canAscend())
			allFileItems.add(0, Folder.Parent);

		view.getList().setContent(allFileItems);
		view.setAddButtonVisible(model.getFolderPermission().canWrite());
		view.refresh();
		if (exposeFileUrls)
			refreshFileUrls(model.getFiles());
	}

	private void refreshFileUrls(List<File> files) {
		String sessionId = sessionManager.getSession().getSessionId();
		Map<String, String> urls = new HashMap();
		for (File f : files)
			urls.put(f.getName(),
					fileSystemService.getDownloadUrl(f, sessionId));
		view.refreshFileUrls(urls);
	}

	@Override
	public void onMoveToParentFolder() {
		if (!model.getFolderModel().canAscend())
			return;
		view.showProgress();
		model.moveToParentFolder(createFolderChangeListener());
	}

	@Override
	public void onChangeToFolder(int level, Folder folder) {
		view.showProgress();
		model.changeToFolder(level, folder, createFolderChangeListener());
	}

	public void onError(ServiceError error, boolean reload) {
		dialogManager.showError(error);

		if (reload)
			reload();
		else
			reset();
	}

	public void openUploadDialog() {
		if (!model.hasFolder() || model.getCurrentFolder().isEmpty())
			return;

		fileUploadDialogFactory.openFileUploadDialog(model.getCurrentFolder(),
				createReloadListener("Upload"));
	}

	public void openNewFolderDialog() {
		if (!model.hasFolder() || model.getCurrentFolder().isEmpty())
			return;

		createFolderDialogFactory.openCreateFolderDialog(
				model.getCurrentFolder(), new FolderHandler() {
					public void createFolder(Folder parentFolder,
							String folderName) {
						fileSystemService.createFolder(parentFolder,
								folderName,
								createReloadListener("Create folder"));
					}
				});
	}

	public void retrieveFromUrl() {
		if (!model.hasFolder() || model.getCurrentFolder().isEmpty())
			return;

		dialogManager.showInputDialog(
				textProvider.getText(Texts.retrieveUrlTitle),
				textProvider.getText(Texts.retrieveUrlMessage), "",
				new InputListener() {
					@Override
					public void onInput(String url) {
						retrieveUrl(url);
					}

					@Override
					public boolean isInputAcceptable(String input) {
						return input.length() > 0
								&& input.toLowerCase().startsWith("http");
					}
				});
	}

	private void retrieveUrl(final String url) {
		final WaitDialog waitDialog = dialogManager.openWaitDialog("",
				textProvider.getText(Texts.pleaseWait));

		fileSystemService.retrieveUrl(model.getCurrentFolder(), url,
				new ResultListener() {
					@Override
					public void onSuccess(Object result) {
						waitDialog.close();
						logger.log(Level.INFO, "URL retrieve complete");
						reload();
					}

					@Override
					public void onFail(ServiceError error) {
						waitDialog.close();

						if (error.getError().getCode() == 301)
							dialogManager.showInfo(textProvider
									.getText(Texts.retrieveUrlTitle),
									textProvider.getText(
											Texts.retrieveUrlNotFound, url));
						else if (error.getError().getCode() == 302)
							dialogManager.showInfo(
									textProvider
											.getText(Texts.retrieveUrlTitle),
									textProvider
											.getText(
													Texts.retrieveUrlNotAuthorized,
													url));
						else if (ServiceErrorType.REQUEST_FAILED.equals(error
								.getType()))
							dialogManager.showInfo(textProvider
									.getText(Texts.retrieveUrlTitle),
									textProvider
											.getText(Texts.retrieveUrlFailed),
									error.getDetails());
						else
							dialogManager.showError(error);
					}
				});
	}

	private ResultListener createReloadListener(final String operation) {
		return createListener(new Callback() {
			public void onCallback() {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						logger.log(Level.INFO, operation + " complete");
						reload();
					}
				});
			}
		});
	}

	private ResultListener createFolderChangeListener() {
		return createListener(createRefreshCallback(),
				createCurrentFolderChangedEventCallback());
	}

	private Callback createCurrentFolderChangedEventCallback() {
		return new Callback() {
			@Override
			public void onCallback() {
				eventDispatcher.onEvent(MainViewEvent
						.onCurrentFolderChanged(model.getCurrentFolder()));
			}
		};
	}

	private Callback createRefreshCallback() {
		return new Callback() {
			public void onCallback() {
				refreshView();
				view.hideProgress();
			}
		};
	}

	private ResultListener createListener(final Callback... callbacks) {
		return new ResultListener<Object>() {
			public void onFail(ServiceError error) {
				onError(error, true);
			}

			public void onSuccess(Object result) {
				for (Callback callback : callbacks)
					callback.onCallback();
			}
		};
	}

	public void logout() {
		sessionService.logout(new ResultListener<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				dropBox.close();
				sessionManager.endSession();
			}

			@Override
			public void onFail(ServiceError error) {
				onError(error, false);
			}
		});
	}

	public void changePassword() {
		passwordDialogFactory.openPasswordDialog(this);
	}

	public void changePassword(String oldPassword, String newPassword) {
		configurationService.changePassword(oldPassword, newPassword,
				new ResultListener() {
					public void onFail(ServiceError error) {
						if (ServiceErrorType.AUTHENTICATION_FAILED.equals(error
								.getType())) {
							dialogManager.showInfo(
									textProvider
											.getText(Texts.passwordDialogTitle),
									textProvider
											.getText(Texts.passwordDialogOldPasswordIncorrect));
						} else {
							onError(error, false);
						}
					}

					public void onSuccess(Object result) {
						dialogManager.showInfo(
								textProvider.getText(Texts.passwordDialogTitle),
								textProvider
										.getText(Texts.passwordDialogPasswordChangedSuccessfully));
					}
				});
	}

	public void setListOrder(String columnId, Sort sort) {
		view.getList().setComparator(createComparator(columnId, sort));
	}

	private GridComparator<FileSystemItem> createComparator(String columnId,
			Sort sort) {
		return new DefaultFileItemComparator(columnId, sort);
	}

	public void onEditItemPermissions() {
		permissionEditorViewFactory.openPermissionEditor(null);
	}

	public void onOpenAdministration() {
		viewManager.openUrlInNewWindow(configurationService
				.getAdministrationUrl());
	}

	public void onToggleSelectMode() {
		view.setSelectMode(view.selectModeButton().isDown());
	}

	public void onFileSystemItemSelectionChanged(List<FileSystemItem> selected) {
		model.setSelected(selected);
		view.updateFileSelection(selected);
	}

	public void onSelectAll() {
		view.selectAll();
	}

	public void onSelectNone() {
		view.selectNone();
	}

	public void onCopySelected() {
		fileSystemActionHandler.onAction(model.getSelectedItems(),
				FileSystemAction.copy, null, null, new Callback() {
					@Override
					public void onCallback() {
						view.selectNone();
					}
				});
	}

	public void onMoveSelected() {
		fileSystemActionHandler.onAction(model.getSelectedItems(),
				FileSystemAction.move, null, null, new Callback() {
					@Override
					public void onCallback() {
						view.selectNone();
					}
				});
	}

	public void onDeleteSelected() {
		fileSystemActionHandler.onAction(model.getSelectedItems(),
				FileSystemAction.delete, null, null, new Callback() {
					@Override
					public void onCallback() {
						view.selectNone();
					}
				});
	}

	@Override
	public void onSearch(final String text) {
		view.showProgress();

		fileSystemService.search(model.getCurrentFolder(), text,
				new ResultListener<SearchResult>() {
					@Override
					public void onSuccess(SearchResult result) {
						view.clearSearchField();
						view.hideProgress();
						onShowSearchResult(text, result);
					}

					@Override
					public void onFail(ServiceError error) {
						view.hideProgress();
						dialogManager.showError(error);
					}
				});
	}

	protected void onShowSearchResult(String criteria, SearchResult result) {
		if (result.getMatchCount() == 0)
			dialogManager.showInfo(
					textProvider.getText(Texts.searchResultsDialogTitle),
					textProvider.getText(Texts.searchResultsNoMatchesFound));
		else
			searchResultDialogFactory.show(dropBox, criteria, result);
	}

	public void onAddSelectedToDropbox() {
		dropBox.addItems(getSelectedItems());
		view.selectNone();
	}

	public void onToggleDropBox() {
		dropBox.toggle(view.getDropboxLocation());
	}

	@Override
	public List<FileSystemItem> getSelectedItems() {
		return model.getSelectedItems();
	}

}