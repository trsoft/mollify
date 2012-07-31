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
import org.sjarvela.mollify.client.service.request.listener.ResultListenerFactory;

public class ExternalServiceAdapter implements ExternalService {

	private final ExternalService service;
	private final ResultListenerFactory listenerFactory;

	public ExternalServiceAdapter(ExternalService service,
			ResultListenerFactory listenerFactory) {
		this.service = service;
		this.listenerFactory = listenerFactory;
	}

	@Override
	public void get(String path, ResultListener listener) {
		service.get(path, listenerFactory.createListener(listener));
	}

	@Override
	public void put(String path, String data, ResultListener resultListener) {
		service.put(path, data, resultListener);
	}

	@Override
	public void post(String path, String data, ResultListener listener) {
		service.post(path, data, listenerFactory.createListener(listener));
	}

	@Override
	public void del(String path, ResultListener listener) {
		service.del(path, listener);
	}

	@Override
	public void post(String data, ResultListener listener) {
		service.post(data, listenerFactory.createListener(listener));
	}

	@Override
	public String getUrl(String s) {
		return service.getUrl(s);
	}

	@Override
	public String getPluginUrl(String id) {
		return service.getPluginUrl(id);
	}
}