/*
 * IndicatorTableModel.java
 *
 * Created on June 17, 2007, 4:43 PM
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Copyright (C) 2007 Cheok YanCheng <yccheok@yahoo.com>
 */

package org.yccheok.jstock.gui;

import java.util.*;
import javax.swing.table.*;
import org.yccheok.jstock.engine.*;
import org.yccheok.jstock.analysis.*;

/**
 *
 * @author yccheok
 */
public class IndicatorTableModel extends AbstractTableModelWithMemory {
    
    /** Creates a new instance of IndicatorTableModel */
    public IndicatorTableModel() {
        for(int i = 0; i < columnNames.length; i++) {
            columnNameMapping.put(columnNames[i], i);
        }        
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }
    
    public int getRowCount()
    {
        return tableModel.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        List<Object> indicatorInfo = tableModel.get(rowIndex);
        return indicatorInfo.get(columnIndex);        
    }
    
    public Object getOldValueAt(int rowIndex, int columnIndex) {
        List<Object> indicatorInfo = oldTableModel.get(rowIndex);
        
        if(null == indicatorInfo) return null;
        
        return indicatorInfo.get(columnIndex);        
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Class getColumnClass(int c) {
        return columnClasses[c];
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        throw new java.lang.UnsupportedOperationException();
    }
    
    public void addIndicator(Indicator indicator) {
        Integer row = rowIndicatorMapping.get(getIndicatorKey(indicator));
        
        if(row == null) {
            tableModel.add(indicatorToList(indicator));
            oldTableModel.add(null);
            indicators.add(indicator);
            final int rowIndex = tableModel.size() - 1;
            rowIndicatorMapping.put(getIndicatorKey(indicator), rowIndex);
            fireTableRowsInserted(rowIndex, rowIndex);
        }
        else {
            oldTableModel.set(row, tableModel.get(row));
            tableModel.set(row, indicatorToList(indicator));
            indicators.set(row, indicator);
            this.fireTableRowsUpdated(row, row);            
        }
    }
    
    public void removeIndicator(Indicator indicator) {
        Integer row = rowIndicatorMapping.get(getIndicatorKey(indicator));
        
        if(row != null) {
            tableModel.remove(row);
            oldTableModel.remove(row);
            indicators.remove(row);
            rowIndicatorMapping.remove(getIndicatorKey(indicator));
            
            int size = indicators.size();
            for(int i=row; i<size; i++) {
                Indicator otherIndicator = indicators.get(i);
                rowIndicatorMapping.put(getIndicatorKey(otherIndicator), i);
            }
            
            this.fireTableRowsDeleted(row, row);
        }
    }
    
    private List<Object> indicatorToList(Indicator indicator) {
        List<Object> list = new ArrayList<Object>();
        list.add(indicator.toString());
        
        final Stock stock = indicator.getStock();
        assert(stock != null);
        
        list.add(stock.getCode());
        list.add(stock.getSymbol());
        list.add(stock.getOpenPrice());
        list.add(stock.getLastPrice());
        list.add(stock.getHighPrice());
        list.add(stock.getLowPrice());
        list.add(stock.getVolume());
        list.add(stock.getChangePrice());
        list.add(stock.getChangePricePercentage());
        list.add(stock.getLastVolume());
        list.add(stock.getBuyPrice());
        list.add(stock.getBuyQuantity());
        list.add(stock.getSellPrice());
        list.add(stock.getSellQuantity());
        
        if(stock instanceof StockEx) {
            StockEx stockEx = (StockEx)stock;
            list.add(stockEx.getMarketCapital());
            list.add(stockEx.getSharesIssued());
        }
        else {
            list.add(0);
            list.add(0);
        }
        
        return list;
    }
    
    public Indicator getIndicator(int row) {
        return indicators.get(row);
    }
    
    public void removeAll() {
        final int size = tableModel.size();
        
        if(size == 0) return;
        
        tableModel.clear();
        oldTableModel.clear();
        indicators.clear();
        rowIndicatorMapping.clear();
        this.fireTableRowsDeleted(0, size - 1);
    }
    
    public void removeRow(int row) {
        List<Object> list = tableModel.remove(row);
        oldTableModel.remove(row);
        indicators.remove(row);        
        // 0 is indicator, 1 is stock code.
        rowIndicatorMapping.remove(list.get(0).toString() + list.get(1).toString());

        int size = indicators.size();
        for(int i=row; i<size; i++) {
            Indicator indicator = indicators.get(i);
            rowIndicatorMapping.put(getIndicatorKey(indicator), i);
        }
        
        this.fireTableRowsDeleted(row, row);
    }
    
    private String getIndicatorKey(Indicator indicator) {
        // Stock shouldn't be null.
        assert(indicator.getStock() != null);
        
        return indicator.toString() + indicator.getStock().getCode();
    }
    
    public int findColumn(String columnName) {
        return columnNameMapping.get(columnName);
    }

    public int findRow(Indicator indicator) {
        Integer row = rowIndicatorMapping.get(getIndicatorKey(indicator));
        if(row != null) return row;
        
        return -1;
    }
    
    private List<List<Object>> tableModel = new ArrayList<List<Object>>();
    private List<List<Object>> oldTableModel = new ArrayList<List<Object>>();
    private List<Indicator> indicators = new ArrayList<Indicator>();
    // Used to get column by Name in fast way.
    private Map<String, Integer> columnNameMapping = new HashMap<String, Integer>();
    // Used to get row by Stock in fast way.
    private Map<String, Integer> rowIndicatorMapping = new HashMap<String, Integer>();
    private String[] columnNames =  {"Indicator",   "Code",        "Symbol",       "Open",     "Last",        "High",         "Low",      "Vol",          "Chg",      "Chg (%)",      "L.Vol",        "Buy",      "B.Qty",        "Sell",         "S.Qty",  "M.Capital", "S.Issued"};
    private Class[] columnClasses = {String.class, Code.class, Symbol.class, Double.class, Double.class, Double.class, Double.class, Integer.class, Double.class, Double.class, Integer.class, Double.class, Integer.class, Double.class, Integer.class, Long.class, Long.class};    
}
