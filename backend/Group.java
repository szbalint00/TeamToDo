package backend;

public class Group {
	private final String groupName;
	private final TasksData tasksData;
	private final int groupID;
	
	public Group(int groupID, String groupName) {
		this.groupID = groupID;
		this.groupName = groupName;
		tasksData = new TasksData();
	}
	
	public TasksData getData() {
		return tasksData;
	}
	
	public int getGroupID() { return groupID; }
	public String getGroupName() { return groupName; }
}
