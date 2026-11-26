package backend;

public class Task {
	private String taskName;
	private String comment;
	private boolean isDone;
	private int taskID;
	
	public Task(int NtaskID, boolean NisDone, String NtaskName, String Ncomment) {
		taskName = NtaskName;
		comment = Ncomment;
		isDone = NisDone;
		taskID = NtaskID;
	}
	
	public void setIsDone(boolean NisDone) {
		isDone = NisDone;
	}
	
	public String getTaskName() { return taskName; }
	public String getComment() { return comment; }
	public boolean getIsDone() { return isDone; }
	public int getTaskID() { return taskID; }
}
