/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.session.user;

public class UserGroup extends UserBase {
	public static UserGroup create(String id, String name) {
		UserGroup result = UserGroup.createObject().cast();
		result.putValues(id, name);
		return result;
	}

	protected UserGroup() {
	}
}
