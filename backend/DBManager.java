package backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {
	private static final Logger LOGGER = Logger.getLogger(DBManager.class.getName());

	private final String dbName;
	private final String url;
	
	public DBManager(String rawDbName) {
		this.dbName = rawDbName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        if (this.dbName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be empty.");
        }

        try {
			Files.createDirectories(Paths.get("dbs"));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "IO Exception", e);
		}

        this.url = "jdbc:sqlite:dbs/" + this.dbName + ".db";
        
		LOGGER.log(Level.INFO, "DB: Connected to {0} ({1})", new Object[]{dbName, url});
        
        try {
			initTables();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "SQL Exception", e);
		}
        
	}
	
	private Connection connect() throws SQLException {
	    Connection conn = DriverManager.getConnection(url);
	    try (Statement pragmaStmt = conn.createStatement()) {
	        pragmaStmt.execute("PRAGMA foreign_keys = ON;");
	    }
	    return conn;
	}

	private void initTables() throws SQLException {
	    String createCsoportSql = """
		CREATE TABLE IF NOT EXISTS Csoport (csoport_id INTEGER PRIMARY KEY AUTOINCREMENT,csoport_nev VARCHAR(30) NOT NULL);
		""";

	    String createFeladatokSql = """
		CREATE TABLE IF NOT EXISTS Feladatok (
		feladat_id INTEGER PRIMARY KEY AUTOINCREMENT,
		csoport_id INTEGER NOT NULL,
		feladat_nev VARCHAR(30) NOT NULL,
		feladat_megj VARCHAR(300),
		feladat_teljesitve INTEGER NOT NULL DEFAULT 0 CHECK (
		feladat_teljesitve IN (0, 1)),
		FOREIGN KEY (csoport_id) REFERENCES Csoport(csoport_id) ON DELETE CASCADE
		);
		""";

	    try (Connection conn = connect();
	         Statement stmt = conn.createStatement()) {
	        stmt.execute(createCsoportSql);
	        stmt.execute(createFeladatokSql);
	    }
	    
		LOGGER.log(Level.INFO, "DB: Table init completed");
	}
	
	public Group insertNewGroup(String gName) throws SQLException {
		Connection conn = connect();
				
		String sql = "INSERT INTO Csoport(csoport_nev) VALUES (?)";
		PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, gName);
		int rows = pstmt.executeUpdate();
		if(rows == 0) {
			LOGGER.log(Level.WARNING, "Insert failed...");
			return null;
		}
		ResultSet rs = pstmt.getGeneratedKeys();
		rs.next();
		int gID = rs.getInt(1);
		
		LOGGER.log(Level.INFO, "DB: New Group - {0} ({1})", new Object[]{gName, gID});
		
		return new Group(gID, gName);
	}
	
	public List<Group> getGroups() throws SQLException{
		Connection conn = connect();
		
		String sql = "SELECT csoport_id, csoport_nev FROM Csoport WHERE 1";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		
		List<Group> groups = new ArrayList<>();
		
		
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
		PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		pstmt.setInt(1, gID);
		pstmt.setString(2, tName);
		pstmt.setString(3,  tComment);
		pstmt.setBoolean(4, tIsDone);
		int rows = pstmt.executeUpdate();
		if(rows == 0) {
			LOGGER.log(Level.WARNING, "Insert failed...");
			return null;
		}
		ResultSet rs = pstmt.getGeneratedKeys();
		rs.next();
		int tID = rs.getInt(1);
		
		LOGGER.log(Level.INFO, "DB: New Task - {0} ({1})", new Object[]{tName, tID});
		
		return new Task(tID, tIsDone, tName, tComment);
	}
	
	public List<Task> getTasks(int gID) throws SQLException{
		Connection conn = connect();
		String sql = "SELECT feladat_id, feladat_nev, feladat_megj, feladat_teljesitve FROM Feladatok WHERE csoport_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, gID);
		ResultSet rs = pstmt.executeQuery();
		
		List<Task> tasks = new ArrayList<>();
		
		while(rs.next()) {
			int taskID = rs.getInt(1);
            String taskName = rs.getString(2);
            String taskComment = rs.getString(3);
            boolean taskIsDone = rs.getBoolean(4);
            
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
			LOGGER.log(Level.WARNING, "DB: TASK_{0} not updated.", t.getTaskID());
			return;
		}
		
		LOGGER.log(Level.INFO, "DB: TASK_{0} updated.", t.getTaskID());
	}
	
	public void updateGroup(Group g) throws SQLException {
		Connection conn = connect();
		
		String sql = "UPDATE Csoport SET csoport_nev = ? WHERE csoport_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1,  g.getGroupName());
		pstmt.setInt(2,  g.getGroupID());
		
		int rows = pstmt.executeUpdate();
		
		if(rows == 0) {
			LOGGER.log(Level.WARNING, "DB: GROUP_{0} not updated.", g.getGroupID());
			return;
		}
		
		LOGGER.log(Level.INFO, "DB: GROUP_{0} updated.", g.getGroupID());
	}

	public void deleteTask(int tID) throws SQLException{
		Connection conn = connect();
		
		String sql = "DELETE FROM Feladatok WHERE feladat_id = ?";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1,  tID);
		
		int rows = pstmt.executeUpdate();
		
		if(rows == 0) {
			LOGGER.log(Level.WARNING, "DB: TASK_{0} not deleted.", tID);
			return;
		}
		
		LOGGER.log(Level.INFO, "DB: TASK_{0} deleted.", tID);
	}
}
