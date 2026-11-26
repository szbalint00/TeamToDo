package backend;

public class Task {
	private final String taskName;
	private final String comment;
	private final int taskID;
	private boolean isDone;
	
	public Task(int taskID, boolean isDone, String taskName, String comment) {
		this.taskName = taskName;
		this.comment = comment;
		this.isDone = isDone;
		this.taskID = taskID;
	}
	
	public void setIsDone(boolean isDone) {
		this.isDone = isDone;
	}
	
	public String getTaskName() { return taskName; }
	public String getComment() { return comment; }
	public boolean getIsDone() { return isDone; }
	public int getTaskID() { return taskID; }
}
