/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.localization;

import com.google.gwt.core.client.GWT;
import com.google.inject.Singleton;

@Singleton
public class DefaultTextProvider implements TextProvider {
	private LanguageConstants languageConstants;
	private MessageConstants messageConstants;

	public DefaultTextProvider() {
		languageConstants = GWT.create(LanguageConstants.class);
		messageConstants = GWT.create(MessageConstants.class);
	}

	public LanguageConstants getStrings() {
		return languageConstants;
	}

	public MessageConstants getMessages() {
		return messageConstants;
	}

	public String getSizeText(long bytes) {
		if (bytes < 1024l) {
			return (bytes == 1 ? getMessages().sizeOneByte() : getMessages()
					.sizeInBytes(bytes));
		}

		if (bytes < (1024l * 1024l)) {
			double kilobytes = (double) bytes / (double) 1024;
			return (kilobytes == 1 ? getMessages().sizeOneKilobyte()
					: getMessages().sizeInKilobytes(kilobytes));
		}

		if (bytes < (1024l * 1024l * 1024l)) {
			double megabytes = (double) bytes / (double) (1024 * 1024);
			return getMessages().sizeInMegabytes(megabytes);
		}

		double gigabytes = (double) bytes / (double) (1024 * 1024 * 1024);
		return getMessages().sizeInGigabytes(gigabytes);
	}
}