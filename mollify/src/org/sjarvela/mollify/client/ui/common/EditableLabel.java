///**
// * Copyright (c) 2008- Samuli Järvelä
// *
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
// * this entire header must remain intact.
// */
//
//package org.sjarvela.mollify.client.ui.common;
//
//import org.sjarvela.mollify.client.ui.StyleConstants;
//import org.sjarvela.mollify.client.util.Html;
//
//import com.google.gwt.core.client.Scheduler;
//import com.google.gwt.core.client.Scheduler.ScheduledCommand;
//import com.google.gwt.user.client.ui.Composite;
//import com.google.gwt.user.client.ui.FlowPanel;
//import com.google.gwt.user.client.ui.HTML;
//import com.google.gwt.user.client.ui.Panel;
//import com.google.gwt.user.client.ui.TextArea;
//import com.google.gwt.user.client.ui.Widget;
//
//public class EditableLabel extends Composite {
//	private HTML label;
//	private TextArea editor;
//	private final boolean html;
//
//	public EditableLabel(String style, boolean html) {
//		super();
//
//		this.html = html;
//		initWidget(createContent(style));
//
//		this.setStylePrimaryName(StyleConstants.EDITABLE_LABEL);
//		if (style != null)
//			this.addStyleDependentName(style);
//
//		setEditable(false);
//	}
//
//	private Widget createContent(String style) {
//		Panel panel = new FlowPanel();
//		panel.setStyleName(StyleConstants.EDITABLE_LABEL_PANEL);
//
//		label = new HTML();
//		label.setStylePrimaryName(StyleConstants.EDITABLE_LABEL + "-label");
//		if (style != null)
//			label.addStyleDependentName(style);
//		panel.add(label);
//
//		editor = new TextArea();
//		editor.setStylePrimaryName(StyleConstants.EDITABLE_LABEL + "-editor");
//		if (style != null)
//			editor.addStyleDependentName(style);
//		panel.add(editor);
//
//		return panel;
//	}
//
//	public void setText(String text) {
//		if (html)
//			label.setHTML(Html.encodeSafeHtml(text));
//		else
//			label.setText(text);
//		editor.setText(text);
//	}
//
//	public void setEditable(final boolean isEditable) {
//		editor.setVisible(isEditable);
//		label.setVisible(!isEditable);
//
//		if (isEditable)
//			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//				@Override
//				public void execute() {
//					editor.setFocus(true);
//				}
//			});
//	}
//
//	public String getText() {
//		return editor.getText();
//	}
//}
