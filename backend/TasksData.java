package backend;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class TasksData extends AbstractTableModel{
	private transient List<Task> tasks = new ArrayList<>();
	
	@Override
	public int getRowCount() {
		return tasks.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Task curr = tasks.get(rowIndex);

		return switch (columnIndex){
			case 0 -> curr.getIsDone();
			case 1 -> curr.getTaskName();
			default -> curr.getComment();
		};
	}
	
	@Override
	public String getColumnName(int col) {
		return switch (col) {
			case 0 -> "Status";
			case 1 -> "Task";
			default -> "Comment";
		};
	}
	
	@Override
	public Class<?> getColumnClass(int col){
		return switch (col) {
			case 0 -> Boolean.class;
			default -> String.class;
		};
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 0;
	}
	
	@Override
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
