package backend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class GroupsData extends AbstractTableModel{
	List<Group> groups = new ArrayList<Group>();
	
	public int getRowCount() {
		return groups.size();
	}

	public int getColumnCount() {
		return 1;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Group curr = groups.get(rowIndex);
		
		return curr.getGroupName();
	}
	
	public String getColumnName(int col) {
		return "Groups";
	}
	
	public Class<?> getColumnClass(int col){
		return String.class;
	}
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	public void setValueAt(Object value, int row, int column) {
        return;
    }
	
	public void initGroups(DBManager db) throws SQLException {
		groups = db.getGroups();
		
		for(Group gp : groups) {
			gp.getData().initTasks(db.getTasks(gp.getGroupID()));
		}
		
		fireTableDataChanged();
	}
	
	public void addGroup(Group Ngroup) {
		groups.add(Ngroup);
		fireTableDataChanged();
	}
	
	public Group selectGroup(int idx) {
		return groups.get(idx);
	}
}
