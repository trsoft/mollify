/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.service.environment.php;

import org.sjarvela.mollify.client.service.SessionService;
import org.sjarvela.mollify.client.service.environment.php.PhpService.RequestType;
import org.sjarvela.mollify.client.service.request.JSONStringBuilder;
import org.sjarvela.mollify.client.service.request.listener.ResultListener;
import org.sjarvela.mollify.client.util.MD5;

import com.allen_sauer.gwt.log.client.Log;

public class PhpSessionService extends ServiceBase implements SessionService {
	enum SessionAction implements ActionId {
		authenticate, info, logout
	}

	public PhpSessionService(PhpService service) {
		super(service, RequestType.session);
	}

	public void getSessionInfo(String protocolVersion,
			ResultListener resultListener) {
		if (Log.isDebugEnabled())
			Log.debug("Requesting session info (protocol version '"
					+ protocolVersion + "')");

		request().url(
				serviceUrl().action(SessionAction.info).item(protocolVersion))
				.listener(resultListener).get();
	}

	public void authenticate(String userName, String password,
			String protocolVersion, final ResultListener resultListener) {
		if (Log.isDebugEnabled())
			Log.debug("Authenticating '" + userName + "'");

		String data = new JSONStringBuilder("username", userName).add(
				"password", MD5.generate(password)).add("protocol_version",
				protocolVersion).toString();

		request().url(serviceUrl().action(SessionAction.authenticate)).data(
				data).listener(resultListener).post();
	}

	public void logout(ResultListener resultListener) {
		if (Log.isDebugEnabled())
			Log.debug("Logout");

		request().url(serviceUrl().action(SessionAction.logout)).listener(
				resultListener).post();
	}

}
