/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sjarvela.mollify.client.filesystem.FileSystemItemProvider;
import org.sjarvela.mollify.client.filesystem.Folder;
import org.sjarvela.mollify.client.filesystem.FolderInfo;
import org.sjarvela.mollify.client.js.JsObj;
import org.sjarvela.mollify.client.service.FileSystemService;
import org.sjarvela.mollify.client.service.ServiceError;
import org.sjarvela.mollify.client.service.environment.ServiceEnvironment;
import org.sjarvela.mollify.client.service.request.listener.ResultListener;
import org.sjarvela.mollify.client.session.file.FilePermission;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultFileSystemItemProvider implements FileSystemItemProvider {
	private static Logger logger = Logger
			.getLogger(DefaultFileSystemItemProvider.class.getName());

	private final FileSystemService fileSystemService;
	private List<Folder> roots = new ArrayList();
	private Map<String, JsObj> quotas = Collections.EMPTY_MAP;

	@Inject
	public DefaultFileSystemItemProvider(SessionManager sessionManager,
			ServiceEnvironment env) {
		sessionManager.addSessionListener(new SessionListener() {
			public void onSessionStarted(SessionInfo session) {
				updateRootFolders(session);
			}

			public void onSessionEnded() {
				roots.clear();
			}
		});

		this.fileSystemService = env.getFileSystemService();
	}

	protected void updateRootFolders(SessionInfo session) {
		this.roots = session.getRootFolders();
		this.quotas = new HashMap();
		for (Folder f : roots)
			quotas.put(f.getId(), session.getRootQuota(f.getId()));
	}

	protected void updateQuota(String rootId, int quotaVal, int quotaUsed) {
		logger.log(Level.INFO, "Updating quota for " + rootId + " to "
				+ quotaUsed + "/" + quotaVal);
		Folder root = getRootFolder(rootId);
		JsObj quota = quotas.get(root.getId());
		if (quota == null)
			quota = JsObj.createObject().cast();
		quota.setInt("quota", quotaVal);
		quota.setInt("used", quotaUsed);
		quotas.put(rootId, quota);
	}

	@Override
	public long getQuotaForRoot(String rootId) {
		Folder root = getRootFolder(rootId);
		JsObj quota = quotas.get(root.getId());
		if (quota == null)
			return 0;
		int quotaVal = quota.getInt("quota");
		logger.log(Level.INFO, "Stored quota " + quotaVal);
		return quotaVal;
	}

	@Override
	public long getUsedQuotaForRoot(String rootId) {
		Folder root = getRootFolder(rootId);
		JsObj quota = quotas.get(root.getId());
		if (quota == null)
			return 0;
		int quotaVal = quota.getInt("used");
		logger.log(Level.INFO, "Stored quota used " + quotaVal);
		return quotaVal;
	}

	@Override
	public void getFolders(Folder parent, ResultListener<List<Folder>> listener) {
		if (parent.isEmpty())
			listener.onSuccess(roots);
		else
			fileSystemService.getFolders(parent, listener);
	}

	@Override
	public List<Folder> getRootFolders() {
		return roots;
	}

	@Override
	public Folder getRootFolder(String id) {
		for (Folder f : roots)
			if (f.getId().equals(id))
				return f;
		return null;
	}

	@Override
	public void getFilesAndFolders(final Folder parent,
			final ResultListener<FolderInfo> listener) {
		if (parent.isEmpty())
			listener.onSuccess(new FolderInfo(FilePermission.None, roots,
					Collections.EMPTY_LIST, 0, 0, new ArrayList(),
					new ArrayList()));
		else
			fileSystemService.getInfo(parent, new ResultListener<FolderInfo>() {
				@Override
				public void onSuccess(FolderInfo result) {
					updateQuota(parent.getRootId(), result.getQuota(),
							result.getQuotaUsed());
					listener.onSuccess(result);
				}

				@Override
				public void onFail(ServiceError error) {
					listener.onFail(error);
				}
			});
	}

}
