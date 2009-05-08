package org.sjarvela.mollify.client.ui.dialog.configuration.folders;

import org.sjarvela.mollify.client.Callback;
import org.sjarvela.mollify.client.filesystem.DirectoryInfo;

public interface FolderHandler {

	void addFolder(String name, String path, Callback successCallback);

	void editFolder(DirectoryInfo folder, String name, String path,
			Callback successCallback);

}
