package backend;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class TasksData extends AbstractTableModel{
	List<Task> tasks = new ArrayList<Task>();
	
	public int getRowCount() {
		return tasks.size();
	}

	public int getColumnCount() {
		return 3;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Task curr = tasks.get(rowIndex);
		
		switch (columnIndex) {
		case 0: return curr.getIsDone();
		case 1: return curr.getTaskName();
		default: return curr.getComment();
		}
	}
	
	public String getColumnName(int col) {
		switch(col) {
		case 0: return "Status";
		case 1: return "Task";
		default: return "Comment";
		}
	}
	
	public Class<?> getColumnClass(int col){
		switch(col) {
		case 0: return Boolean.class;
		default: return String.class;
		}
	}
	
	public boolean isCellEditable(int row, int col) {
		if (col == 0) return true;
		return false;
	}
	
	public void setValueAt(Object value, int row, int column) {
        if(!isCellEditable(row, column)){
            return;
        }
        
        tasks.get(row).setIsDone((boolean) value);
        
        fireTableCellUpdated(row, column);
    }
	
	public void initTasks(List<Task> ts) {
		tasks = ts;
		fireTableDataChanged();
	}
	
	public void addTask(Task t) {
		tasks.add(t);
		fireTableDataChanged();
	}
	
	public Task selectTask(int idx) {
		return tasks.get(idx);
	}

}
