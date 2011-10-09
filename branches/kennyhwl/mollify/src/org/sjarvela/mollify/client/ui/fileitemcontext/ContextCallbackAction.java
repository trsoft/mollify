/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.fileitemcontext;

public abstract class ContextCallbackAction implements ContextActionItem,
		ContextCallback {
	String title;

	public ContextCallbackAction(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

}