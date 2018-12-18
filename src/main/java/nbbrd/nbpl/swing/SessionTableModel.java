/*
 * Copyright 2018 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package nbbrd.nbpl.swing;

import nbbrd.nbpl.core.Config;
import nbbrd.nbpl.core.App;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Philippe Charles
 */
public final class SessionTableModel extends AbstractTableModel {

    private final List<Session> list = new ArrayList<>();

    public void add(Session session) {
        list.add(session);
        session.addPropertyChangeListener(o -> fireTableDataChanged());
        fireTableDataChanged();
    }

    public Session getRow(int rowIndex) {
        return list.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Session session = getRow(rowIndex);
        switch (columnIndex) {
            case 0:
                return session.getState();
            case 1:
                return session.getJob().getApp();
            case 2:
                return session.getJob().getConfig();
            case 3:
                return session.getWorkingDir();
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "State";
            case 1:
                return "App";
            case 2:
                return "Config";
            case 3:
                return "User dir";
        }
        return super.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return SwingWorker.StateValue.class;
            case 1:
                return App.class;
            case 2:
                return Config.class;
            case 3:
                return File.class;
        }
        return super.getColumnClass(columnIndex);
    }
}
