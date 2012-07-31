/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.filesystem;

/*import java.util.ArrayList;
import java.util.List;

import org.sjarvela.mollify.client.filesystem.js.JsFolder;
import org.sjarvela.mollify.client.filesystem.js.JsRootFolder;

public class VirtualGroupFolder extends JsFolder {
	List<JsFolder> children = new ArrayList();

	public VirtualGroupFolder(String name, String path) {
		super(null, null, name, path, null);
	}

	@Override
	public boolean isRoot() {
		return true;
	}

	public void add(JsRootFolder f) {
		int level = this.getLevel();
		if (f.getGroupParts().size() > level) {
			String next = f.getGroupParts().get(level);
			VirtualGroupFolder groupFolder = new VirtualGroupFolder(next,
					this.path + "/" + next);
			groupFolder.add(f);
			children.add(groupFolder);
		} else {
			children.add(f);
		}
	}

	private int getLevel() {
		int i = -1;
		int count = 0;
		while (true) {
			i = this.path.indexOf("/", i + 1);
			if (i < 0)
				break;
			count++;
		}
		return count + 1;
	}

	public List<Folder> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof VirtualGroupFolder))
			return false;

		return path.equals(((VirtualGroupFolder) obj).path);
	}
}*/