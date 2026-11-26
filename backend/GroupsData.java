package backend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class GroupsData extends AbstractTableModel{
	private transient List<Group> groups = new ArrayList<>();
	
	@Override
	public int getRowCount() {
		return groups.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Group curr = groups.get(rowIndex);
		
		return curr.getGroupName();
	}
	
	@Override
	public String getColumnName(int col) {
		return "Groups";
	}
	
	@Override
	public Class<?> getColumnClass(int col){
		return String.class;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	@Override
	public void setValueAt(Object value, int row, int column) {
		// Cannot edit
    }
	
	public void initGroups(DBManager db) throws SQLException {
		groups = db.getGroups();
		
		for(Group gp : groups) {
			gp.getData().initTasks(db.getTasks(gp.getGroupID()));
		}
		
		fireTableDataChanged();
	}
	
	public void addGroup(Group group) {
		groups.add(group);
		fireTableDataChanged();
	}
	
	public Group selectGroup(int idx) {
		return groups.get(idx);
	}
}
