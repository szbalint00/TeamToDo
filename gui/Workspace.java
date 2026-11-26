package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

import backend.DBManager;
import backend.Group;
import backend.GroupsData;
import backend.Task;
import backend.TasksData;

public class Workspace {
	private final String ICON_PATH = "src/img/TTD_ICON.png";
	private final String DB_ICON_PATH = "src/img/DB_ICON.png";
	private final String CONN_ICON_PATH  = "src/img/CONN_ICON.png";
	private final String SAVE_ICON_PATH = "src/img/SAVE_ICON.png";
	private final String RELOAD_ICON_PATH = "src/img/RELOAD_ICON.png";
	
	private JFrame frame;
	
	private JMenuBar menubar;
	private JMenu dbMenu;
	private JMenuItem saveMenuItem;
	private JMenuItem reloadMenuItem;
	private JMenuItem connMenuItem;
	
	private JPanel groupsPanel;
	private JScrollPane groupScrollPane; 
	private JTable groupTable;
	private JPanel tasksPanel;
	private JSplitPane splitPane;
	
	private JPanel groupInsPanel;
	private JLabel groupInsLabel;
	private JTextField groupInsTxt;
	private JButton groupInsButton;
	
	private JScrollPane tasksScrollPane;
	private JTable tasksTable;
	
	private DefaultTableCellRenderer centerRenderer;
	
	private String dbName;
	private DBManager dbmanager;
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
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		ImageIcon icon = new ImageIcon(ICON_PATH);
		Image iconImg = icon.getImage();
		
		frame.setIconImage(iconImg);
		
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        
        frame.setLocation(x, y);
	}
	
	private void initMenu() {
		ImageIcon dbIcon = new ImageIcon(DB_ICON_PATH);
        Image scaledDbIconImg = dbIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledDbIcon = new ImageIcon(scaledDbIconImg);
        
        ImageIcon connIcon = new ImageIcon(CONN_ICON_PATH);
        Image scaledConnIconImg = connIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledConnIcon = new ImageIcon(scaledConnIconImg);
		
		ImageIcon saveIcon = new ImageIcon(SAVE_ICON_PATH);
        Image scaledSaveIconImg = saveIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledSaveIcon = new ImageIcon(scaledSaveIconImg);
		
		ImageIcon reloadIcon = new ImageIcon(RELOAD_ICON_PATH);
        Image scaledReloadIconImg = reloadIcon.getImage().getScaledInstance(20, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledReloadIcon = new ImageIcon(scaledReloadIconImg);
        
        menubar = new JMenuBar();
        dbMenu = new JMenu("  Database  ");
        dbMenu.setIcon(scaledDbIcon);
        saveMenuItem = new JMenuItem("    Save  ");
        saveMenuItem.setIcon(scaledSaveIcon);
        saveMenuItem.addActionListener(e -> saveToDB());
        reloadMenuItem = new JMenuItem("  Reload  ");
        reloadMenuItem.setIcon(scaledReloadIcon);
        reloadMenuItem.addActionListener(e -> reloadDB());
        connMenuItem = new JMenuItem("  Connect ");
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
		groupInsLabel = new JLabel("New Group: ");
        groupInsTxt = new JTextField(10);
        groupInsButton = new JButton("Add");
        groupInsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, groupInsButton.getPreferredSize().height));
        groupInsButton.addActionListener(e -> addGroup());
        
        groupInsPanel = new JPanel();
        groupInsPanel.setLayout(new BoxLayout(groupInsPanel, BoxLayout.Y_AXIS));
        groupInsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        data = new GroupsData();
        groupTable = new JTable();
        groupTable.setFillsViewportHeight(true);
        groupTable.setModel(data);
        groupTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent me) {
        		if (me.getClickCount() == 1) {
					currSelTask = groupTable.getSelectedRow();
					initTasks();
        		}
            }
         });
        groupTable.setRowHeight(30);
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		centerRenderer.setVerticalAlignment(SwingConstants.CENTER);
		groupTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        groupScrollPane = new JScrollPane(groupTable);

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
        } catch (SQLException e1) {
        	e1.printStackTrace();
        }
	}
	
	private void initTasks() {
		if(currSelTask == -1) {
			currTasksData = new TasksData();
			tasksTable = new JTable();
			tasksScrollPane = new JScrollPane(tasksTable);
		}
		else currTasksData = data.selectGroup(currSelTask).getData();
		
        
        tasksTable.setFillsViewportHeight(true);
        tasksTable.setModel(currTasksData);
        tasksTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
               if (me.getClickCount() == 2) {
                  JTable target = (JTable)me.getSource();
                  int row = target.getSelectedRow();
                  int column = target.getSelectedColumn();
                  JOptionPane.showMessageDialog(null, tasksTable.getValueAt(row, column));
               }
            }
         });
        tasksTable.setRowHeight(20);
        tasksTable.getColumnModel().getColumn(0).setMaxWidth(70);
        tasksTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        tasksTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		tasksTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		tasksTable.getColumnModel().getColumn(2).setPreferredWidth(500);
		
		tasksPanel.add(tasksScrollPane);
	}
	
	public void Show() {
		frame.setVisible(true);
	}
	
	public void saveToDB() {
		System.out.println("Saving data to " + dbName + "...");
		
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
				e.printStackTrace();
			}
		}
		
		System.out.println("Data saved to " + dbName + "!");
	}
	
	private void reloadDB() {
		try {
        	data.initGroups(dbmanager);
        	initTasks();
        } catch (SQLException e1) {
        	e1.printStackTrace();
        }
	}
	
	public void connect() {
		new StartScreen();
	}
	
	private void addGroup() {
		String gName = groupInsTxt.getText();
		
		if(gName.isEmpty()) {
			//NINCS NAME
			return;
		}
		
		try {
			data.addGroup(dbmanager.insertNewGroup(gName));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
