package gui;

import backend.DBManager;
import backend.Group;
import backend.GroupsData;
import backend.Task;
import backend.TasksData;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class Workspace {
	private static final Logger LOGGER = Logger.getLogger(Workspace.class.getName());

	private static final String ICON_PATH = "/img/TTD_ICON.png";
	private static final String DB_ICON_PATH = "/img/DB_ICON.png";
	private static final String CONN_ICON_PATH  = "/img/CONN_ICON.png";
	private static final String SAVE_ICON_PATH = "/img/SAVE_ICON.png";
	private static final String RELOAD_ICON_PATH = "/img/RELOAD_ICON.png";
	
	private JFrame frame;
	
	private JMenuBar menubar;
	
	private JPanel groupsPanel;
	private JTable groupTable;
	private JPanel tasksPanel;
	private JSplitPane splitPane;
	
	private JTextField groupInsTxt;
	
	private JTable tasksTable;
	
	private final DefaultTableCellRenderer centerRenderer;
	
	private final String dbName;
	private final DBManager dbmanager;
	private GroupsData data;
	private TasksData currTasksData = null;
	private int currSelTask;
	
	
	public Workspace(String dbName) {
		currSelTask = -1;
		this.dbName = dbName;
		dbmanager = new DBManager(dbName);
		centerRenderer = new DefaultTableCellRenderer();

		initFrame();
        initMenu();
        initSplit();
        initGroups();
        initTasks();
		
		frame.setJMenuBar(menubar);
		frame.add(splitPane, BorderLayout.CENTER);
	}
	
	private void initFrame() {
		frame = new JFrame();
		frame.setTitle("Team To-Do (" + dbName + ")");
		frame.setSize(1200, 800);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		ImageIcon icon = new ImageIcon(getClass().getResource(ICON_PATH));
		Image iconImg = icon.getImage();
		
		frame.setIconImage(iconImg);
		
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        
        frame.setLocation(x, y);
	}
	
	private void initMenu() {
		ImageIcon dbIcon = new ImageIcon(getClass().getResource(DB_ICON_PATH));
        Image scaledDbIconImg = dbIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledDbIcon = new ImageIcon(scaledDbIconImg);
        
        ImageIcon connIcon = new ImageIcon(getClass().getResource(CONN_ICON_PATH));
        Image scaledConnIconImg = connIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledConnIcon = new ImageIcon(scaledConnIconImg);
		
		ImageIcon saveIcon = new ImageIcon(getClass().getResource(SAVE_ICON_PATH));
        Image scaledSaveIconImg = saveIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledSaveIcon = new ImageIcon(scaledSaveIconImg);
		
		ImageIcon reloadIcon = new ImageIcon(getClass().getResource(RELOAD_ICON_PATH));
        Image scaledReloadIconImg = reloadIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledReloadIcon = new ImageIcon(scaledReloadIconImg);
        
        menubar = new JMenuBar();
        JMenu dbMenu = new JMenu("  Database  ");
        dbMenu.setIcon(scaledDbIcon);
        JMenuItem saveMenuItem = new JMenuItem("    Save  ");
        saveMenuItem.setIcon(scaledSaveIcon);
        saveMenuItem.addActionListener(e -> saveToDB());
        JMenuItem reloadMenuItem = new JMenuItem("  Reload  ");
        reloadMenuItem.setIcon(scaledReloadIcon);
        reloadMenuItem.addActionListener(e -> reloadDB());
        JMenuItem connMenuItem = new JMenuItem("  Connect ");
        connMenuItem.setIcon(scaledConnIcon);
        connMenuItem.addActionListener(e -> connect());
        
        menubar.add(dbMenu);
        dbMenu.add(saveMenuItem);
        dbMenu.add(reloadMenuItem);
        dbMenu.add(connMenuItem);
	}
	
	private void initSplit() {
		tasksPanel = new JPanel(new BorderLayout());
        groupsPanel = new JPanel(new BorderLayout());
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupsPanel, tasksPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
	}
	
	private void initGroups() {
		JLabel groupInsLabel = new JLabel("New Group: ");
        groupInsTxt = new JTextField(10);
        JButton groupInsButton = new JButton("Add");
        groupInsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, groupInsButton.getPreferredSize().height));
        groupInsButton.addActionListener(e -> addGroup());
        
        JPanel groupInsPanel = new JPanel();
        groupInsPanel.setLayout(new BoxLayout(groupInsPanel, BoxLayout.Y_AXIS));
        groupInsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        data = new GroupsData();
        groupTable = new JTable();
        groupTable.setFillsViewportHeight(true);
        groupTable.setModel(data);
        groupTable.addMouseListener(new MouseClicks());
        groupTable.setRowHeight(30);
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		centerRenderer.setVerticalAlignment(SwingConstants.CENTER);
		groupTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        JScrollPane groupScrollPane = new JScrollPane(groupTable);

        JPanel groupInsTextWrap = new JPanel();
        groupInsTextWrap.add(groupInsLabel);
        groupInsTextWrap.add(groupInsTxt);
        groupInsPanel.add(groupInsTextWrap);
        groupInsPanel.add(groupInsButton);
        
        groupInsTextWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        groupInsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        groupsPanel.add(groupScrollPane);
        groupsPanel.add(groupInsPanel, BorderLayout.SOUTH);
        
        try {
        	data.initGroups(dbmanager);
        } catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "SQL Exception", e);
        }
	}
	
	private void initTasks() {
		currTasksData = new TasksData();
		tasksTable = new JTable();
		tasksTable.setFillsViewportHeight(true);
		tasksTable.setRowHeight(20);
		tasksTable.addMouseListener(new MouseClicks());

		JScrollPane tasksScrollPane = new JScrollPane(tasksTable);
		tasksPanel.add(tasksScrollPane, BorderLayout.CENTER);

		updateTasksTable();
	}

	private void updateTasksTable() {
		if (currSelTask == -1) {
			currTasksData = new TasksData();
		} else {
			currTasksData = data.selectGroup(currSelTask).getData();
		}

		tasksTable.setModel(currTasksData);
		tasksTable.getColumnModel().getColumn(0).setMaxWidth(70);
		tasksTable.getColumnModel().getColumn(0).setPreferredWidth(60);
		tasksTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		tasksTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		tasksTable.getColumnModel().getColumn(2).setPreferredWidth(500);
	}
	
	public void show() {
		frame.setVisible(true);
	}
	
	public void saveToDB() {
		LOGGER.log(Level.INFO, "Saving data to {0}...", dbName);
		
		for(int i=0;i<data.getRowCount();i++) {
			try {
				Group currG = data.selectGroup(i);
				dbmanager.updateGroup(currG);
				
				TasksData currTData = currG.getData();
				for(int j=0;j<currTData.getRowCount();j++) {
					Task currT = currTData.selectTask(j);
					dbmanager.updateTask(currT);
				}
				
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "SQL Exception", e);
			}
		}
		
		LOGGER.log(Level.INFO, "Data saved to {0}!", dbName);
	}
	
	private void reloadDB() {
		try {
        	data.initGroups(dbmanager);
        	updateTasksTable();
        } catch (SQLException e) {
        	LOGGER.log(Level.SEVERE, "SQL Exception", e);
        }
	}
	
	public void connect() {
		StartScreen app = new StartScreen();
		app.show();
	}
	
	private void addGroup() {
		String gName = groupInsTxt.getText();
		
		if(gName.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Group name cannot be empty!");
			return;
		}
		
		try {
			data.addGroup(dbmanager.insertNewGroup(gName));
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "SQL Exception", e);
		}
	}

	private class MouseClicks implements MouseListener{
		@Override
		public void mouseClicked(MouseEvent e){
			if(e.getSource() == tasksTable && e.getClickCount() == 2){
				int row = tasksTable.getSelectedRow();
				int column = tasksTable.getSelectedColumn();
				JOptionPane.showMessageDialog(null, tasksTable.getValueAt(row, column));
			}
			else if (e.getSource() == groupTable && e.getClickCount() == 1) {
				currSelTask = groupTable.getSelectedRow();
				updateTasksTable();
        	}
		}

        @Override
        public void mousePressed(MouseEvent e) { /* Don't need */}
        @Override
        public void mouseReleased(MouseEvent e) { /* Don't need */}
        @Override
        public void mouseEntered(MouseEvent e) { /* Don't need */}
        @Override
        public void mouseExited(MouseEvent e) { /* Don't need */}
	}
	
}
