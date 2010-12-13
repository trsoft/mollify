/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.service;

import org.sjarvela.mollify.client.service.request.listener.ResultListener;
import org.sjarvela.mollify.client.session.SessionInfo;

public interface SessionService {

	void getSessionInfo(String protocolVersion,
			ResultListener<SessionInfo> resultListener);

	void authenticate(String userName, String password, String protocolVersion,
			ResultListener<SessionInfo> resultListener);

	void logout(ResultListener<Boolean> resultListener);

}
