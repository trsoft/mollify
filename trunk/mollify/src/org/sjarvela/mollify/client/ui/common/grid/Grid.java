/**
 * Copyright (c) 2008- Samuli Järvelä
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

package org.sjarvela.mollify.client.ui.common.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sjarvela.mollify.client.ui.StyleConstants;
import org.sjarvela.mollify.client.ui.common.Coords;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class Grid<T> extends FlexTable {
	private final String headerCss;
	private final List<GridColumn> columns;
	private final Map<GridColumn, GridColumnSortButton> sortButtons = new HashMap();
	private final List<GridListener> listeners = new ArrayList<GridListener>();
	private final List<String> rowStyles = new ArrayList();

	private final Element head;
	private final Element headerRow;

	private List<T> content = new ArrayList();
	private GridDataProvider<T> dataProvider = null;
	private GridComparator<T> comparator = null;

	public Grid(String headerCss, List<GridColumn> columns) {
		super();
		this.headerCss = headerCss;
		this.columns = columns;

		// create elements
		head = DOM.createTHead();
		headerRow = DOM.createTR();

		// insert into DOM
		DOM.insertChild(getElement(), head, 0);
		DOM.insertChild(head, headerRow, 0);
		DOM.setElementAttribute(getBodyElement(), "style",
				"overflow:auto;text-align: left;");
		DOM.setElementAttribute(head, "style", "text-align: left;");

		assureColumnCount(columns.size());
		int i = 0;
		for (GridColumn column : columns) {
			initializeColumn(i, column);
			i++;
		}

		sinkEvents(Event.ONMOUSEOVER);
		sinkEvents(Event.ONMOUSEOUT);
	}

	private void initializeColumn(int index, GridColumn column) {
		Element th = DOM.getChild(headerRow, index);
		th.setAttribute("class", headerCss + "-th");
		th.setId(headerCss + "-th-" + column.getId());

		Widget headerElement = createHeaderWidget(column);
		headerElement.setStyleName(headerCss);
		headerElement.getElement().setId(headerCss + "-" + column.getId());

		th.appendChild(headerElement.getElement());
	}

	private Widget createHeaderWidget(GridColumn column) {
		if (!column.isSortable()) {
			return new Label(column.getTitle());
		}

		Label label = new Label(column.getTitle());
		label.setStyleName(headerCss + "-text");
		label.getElement().setId(headerCss + "-text-" + column.getId());

		Panel panel = new FlowPanel();
		panel.add(label);
		panel.add(createSortButton(column));

		return panel;
	}

	private GridColumnSortButton createSortButton(final GridColumn column) {
		final GridColumnSortButton sortButton = new GridColumnSortButton(
				column, StyleConstants.FILE_LIST_COLUMN_PREFIX + "sort");
		sortButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				Grid.this.onColumnSortClick(column, sortButton.toggle());
			}
		});
		sortButtons.put(column, sortButton);
		return sortButton;
	}

	protected void onColumnSortClick(GridColumn column, Sort sort) {
		for (GridListener listener : listeners)
			listener.onColumnSorted(column, sort);
	}

	public int getColumnIndex(GridColumn column) {
		return columns.indexOf(column);
	}

	private void assureColumnCount(int column) {
		int cellCount = DOM.getChildCount(headerRow);
		int required = column + 1 - cellCount;
		if (required > 0)
			addHeaderCells(head, required);
	}

	private native void addHeaderCells(Element table, int count) /*-{ 
		var row = table.rows[0];
		 
		for(var i = 0; i < count; i++){ 
			var cell = $doc.createElement("th"); 
			row.appendChild(cell);   
		} 
	}-*/;

	public void addListener(GridListener listener) {
		listeners.add(listener);
	}

	public void setDataProvider(GridDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	public void setComparator(GridComparator<T> comparator) {
		this.comparator = comparator;
		if (dataProvider == null)
			return;
		sort();
	}

	public void setContent(List<T> list) {
		if (dataProvider == null)
			throw new RuntimeException("No data provider");

		this.content = new ArrayList(list);
		sort();
	}

	private void sort() {
		if (comparator != null)
			Collections.sort(content, comparator);

		for (GridColumn column : columns) {
			if (sortButtons.containsKey(column)) {
				Sort sort = comparator == null ? Sort.none : (column
						.equals(comparator.getColumn()) ? comparator.getSort()
						: Sort.none);
				sortButtons.get(column).setSort(sort);
			}
		}
		refresh();
	}

	public void refresh() {
		removeAllRows();

		int row = 0;
		for (T t : content) {
			int column = 0;

			for (GridColumn col : columns) {
				dataProvider.getData(t, col).applyTo(row, column, this);
				getCellFormatter().addStyleName(row, column,
						dataProvider.getColumnStyle(col));
				column++;
			}

			List<String> styles = dataProvider.getRowStyles(t);
			for (String style : styles)
				getRowFormatter().addStyleName(row, style);

			if (styles.size() > 0)
				rowStyles.add(styles.get(0));
			else
				rowStyles.add("grid-row");

			row++;
		}
	}

	public int getRowIndex(T t) {
		return content.indexOf(t);
	}

	protected void onClick(int row, GridColumn column) {
		onClick(content.get(row), column);
	}

	protected void onClick(T t, GridColumn column) {
		for (GridListener listener : listeners)
			listener.onColumnClicked(t, column);
	}

	public void removeAllRows() {
		int count = getRowCount();
		if (count > 0) {
			for (int i = 0; i < count; i++)
				removeRow(0);
		}
		rowStyles.clear();
	}

	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEOVER: {
			int row = getEventRowNumber(event);
			if (row < 0)
				return;

			this.getRowFormatter().addStyleName(row,
					rowStyles.get(row) + "-" + StyleConstants.HOVER);
			break;
		}

		case Event.ONMOUSEOUT: {
			int row = getEventRowNumber(event);
			if (row < 0)
				return;

			this.getRowFormatter().removeStyleName(row,
					rowStyles.get(row) + "-" + StyleConstants.HOVER);
			break;
		}
		}

		super.onBrowserEvent(event);
	}

	private int getEventRowNumber(Event event) {
		Element cell = getEventTargetCell(event);
		if (cell == null)
			return -1;

		Element row = DOM.getParent(cell);
		if (row == null)
			return -1;
		return DOM.getChildIndex(getBodyElement(), row);
	}

	public Widget getWidget(T t, GridColumn column) {
		int row = content.indexOf(t);
		return this.getWidget(row, getColumnIndex(column));
	}

	public Coords getWidgetCoords(T t, GridColumn column) {
		Widget cell = getWidget(t, column);
		return new Coords(cell.getAbsoluteLeft(), cell.getAbsoluteTop(), cell
				.getOffsetWidth(), cell.getOffsetHeight());
	}
}