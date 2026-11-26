package backend;

public class Group {
	private String groupName;
	private TasksData tasksData;
	private int groupID;
	
	public Group(int NgroupID, String name) {
		groupID = NgroupID;
		groupName = name;
		tasksData = new TasksData();
	}
	
	public TasksData getData() {
		return tasksData;
	}
	
	public int getGroupID() { return groupID; }
	public String getGroupName() { return groupName; }
}
