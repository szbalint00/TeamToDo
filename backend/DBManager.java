package backend;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DBManager {
	private String dbName;
	private String url;
	
	public DBManager(String rawDbName) {
		this.dbName = rawDbName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        if (this.dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be empty.");
        }

        try {
			Files.createDirectories(Paths.get("dbs"));
		} catch (IOException e) {
			e.printStackTrace();
		}

        this.url = "jdbc:sqlite:dbs/" + this.dbName + ".db";
        
        System.out.println("DB: Connected to " + dbName + " (" + url + ")");
        
        try {
			initTables();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
	}
	
	private Connection connect() throws SQLException {
	    Connection conn = DriverManager.getConnection(url);
	    try (Statement pragmaStmt = conn.createStatement()) {
	        pragmaStmt.execute("PRAGMA foreign_keys = ON;");
	    }
	    return conn;
	}

	public void initTables() throws SQLException {
	    String createCsoportSql = """
	        CREATE TABLE IF NOT EXISTS Csoport (
	            csoport_id INTEGER PRIMARY KEY AUTOINCREMENT,
	            csoport_nev VARCHAR(30) NOT NULL
	        );
	        """;

	    String createFeladatokSql = """
	        CREATE TABLE IF NOT EXISTS Feladatok (
	            feladat_id INTEGER PRIMARY KEY AUTOINCREMENT,
	            csoport_id INTEGER NOT NULL,
	            feladat_nev VARCHAR(30) NOT NULL,
	            feladat_megj VARCHAR(300),
	            feladat_teljesitve INTEGER NOT NULL DEFAULT 0 CHECK (feladat_teljesitve IN (0, 1)),
	            FOREIGN KEY (csoport_id) REFERENCES Csoport(csoport_id) ON DELETE CASCADE
	        );
	        """;

	    try (Connection conn = connect();
	         Statement stmt = conn.createStatement()) {
	        stmt.execute(createCsoportSql);
	        stmt.execute(createFeladatokSql);
	    }
	    
	    System.out.println("DB: Table init completed");
	}
	
	public Group insertNewGroup(String gName) throws SQLException {
		Connection conn = connect();
				
		String sql = "INSERT INTO Csoport(csoport_nev) VALUES (?)";
		PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, gName);
		int rows = pstmt.executeUpdate();
		if(rows == 0) {
			System.out.println("Insert failed...");
			return null;
		}
		ResultSet rs = pstmt.getGeneratedKeys();
		rs.next();
		int gID = rs.getInt(1);
		
		System.out.println("DB: New Group - " + gName + " (" + gID + ")");
		
		return new Group(gID, gName);
	}
	
	public List<Group> getGroups() throws SQLException{
		Connection conn = connect();
		
		String sql = "SELECT * FROM Csoport WHERE 1";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		
		List<Group> groups = new ArrayList<Group>();
		
		
		while(rs.next()) {
			int gId;
			String gName;
			
			gId = rs.getInt(1);
			gName = rs.getString(2);
			
			groups.add(new Group(gId, gName));
		}
		
		return groups;
	}
	
	public Task insertNewTask(int gID, boolean tIsDone, String tName, String tComment) throws SQLException {
		Connection conn = connect();
				
		String sql = "INSERT INTO Feladatok(csoport_id, feladat_nev, feladat_megj, feladat_teljesitve) VALUES (?,?,?,?)";
		PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
		pstmt.setInt(1, gID);
		pstmt.setString(2, tName);
		pstmt.setString(3,  tComment);
		pstmt.setBoolean(4, tIsDone);
		int rows = pstmt.executeUpdate();
		if(rows == 0) {
			System.out.println("Insert failed...");
			return null;
		}
		ResultSet rs = pstmt.getGeneratedKeys();
		rs.next();
		int tID = rs.getInt(1);
		
		System.out.println("DB: New Task - " + tName + " (" + tID + ")");
		
		return new Task(tID, tIsDone, tName, tComment);
	}
	
	public List<Task> getTasks(int gID) throws SQLException{
		Connection conn = connect();
		String sql = "SELECT * FROM Feladatok WHERE csoport_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, gID);
		ResultSet rs = pstmt.executeQuery();
		
		List<Task> tasks = new ArrayList<Task>();
		
		while(rs.next()) {
			int taskID = rs.getInt(1);
            String taskName = rs.getString(3);
            String taskComment = rs.getString(4);
            boolean taskIsDone = rs.getBoolean(5);
            
            tasks.add(new Task(taskID, taskIsDone, taskName, taskComment));
		}
		
		return tasks;
	}
	
	public void updateTask(Task t) throws SQLException {
		Connection conn = connect();
		
		String sql = "UPDATE Feladatok SET feladat_nev = ?, feladat_megj = ?, feladat_teljesitve = ? WHERE feladat_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,  t.getTaskName());
		pstmt.setString(2,  t.getComment());
		pstmt.setBoolean(3, t.getIsDone());
		pstmt.setInt(4,  t.getTaskID());
		
		int rows = pstmt.executeUpdate();
		
		if(rows == 0) {
			System.out.println("DB: TASK_" + t.getTaskID() + " not updated.");
			return;
		}
		
		System.out.println("DB: TASK_" + t.getTaskID() + " updated.");
	}
	
	public void updateGroup(Group g) throws SQLException {
		Connection conn = connect();
		
		String sql = "UPDATE Csoport SET csoport_nev = ? WHERE csoport_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,  g.getGroupName());
		pstmt.setInt(2,  g.getGroupID());
		
		int rows = pstmt.executeUpdate();
		
		if(rows == 0) {
			System.out.println("DB: GROUP_" + g.getGroupID() + " not updated.");
			return;
		}
		
		System.out.println("DB: GROUP_" + g.getGroupID() + " updated.");
	}
}
