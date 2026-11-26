package gui;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class StartScreen {
	private static final Logger LOGGER = Logger.getLogger(StartScreen.class.getName());

	private static final String LOGO_PATH = "/img/TTD.png";
	private static final String ICON_PATH = "/img/TTD_ICON.png";
	
	private JFrame frame;
	private JPanel logoPanel;
	private JPanel loginPanel;
	private JPanel dbPanel;
	private JButton connButton;
	private JTextField dbTxt;
	
	public StartScreen(){
		initFrame();
		initLogin();
		initDB();
		initConn();
		
		loginPanel.add(logoPanel);
		loginPanel.add(dbPanel);
		loginPanel.add(connButton);
		
		frame.getRootPane().setDefaultButton(connButton);
		
		frame.add(loginPanel, BorderLayout.NORTH);
	}

	private void initFrame(){
		frame = new JFrame();

		frame.setTitle("Team To-Do");
		frame.setSize(350,210);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		ImageIcon icon = new ImageIcon(getClass().getResource(ICON_PATH));
		Image iconImg = icon.getImage();
		
		frame.setIconImage(iconImg);
		
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        
        frame.setLocation(x, y);
	}

	private void initLogin(){
		loginPanel = new JPanel();
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));

		ImageIcon logo = new ImageIcon(getClass().getResource(LOGO_PATH));
		Image scaledLogoImg = logo.getImage().getScaledInstance(300, -1, Image.SCALE_SMOOTH);
		ImageIcon scaledLogo = new ImageIcon(scaledLogoImg);
		JLabel logoLabel = new JLabel(scaledLogo);

		logoPanel = new JPanel();
		logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		logoPanel.add(logoLabel);
	}

	private void initDB(){
		dbPanel = new JPanel();
		dbPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JLabel dbLabel = new JLabel("DATABASE: ");
		
		dbTxt = new JTextField(20);
		dbTxt.setForeground(new Color(0, 100, 0));
		
		dbPanel.add(dbLabel);
		dbPanel.add(dbTxt);
	}

	private void initConn(){
		connButton = new JButton("Connect");
		Dimension btnSize = new Dimension(300, 20);

		connButton.setPreferredSize(btnSize);
		connButton.setMaximumSize(btnSize);
		
		connButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		connButton.setBackground(new Color(50, 50, 50));
		connButton.setForeground(new Color(220, 220, 220));
		
		connButton.addActionListener(e -> connectToDB());
	}
	
	private void connectToDB() {
        String dbName = dbTxt.getText().trim();
        if (dbName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a database name!");
            return;
        }
        LOGGER.log(Level.INFO, "Connecting to {0}...", dbName);
        frame.dispose();
        Workspace workspace = new Workspace(dbName);
        workspace.Show();
    }

	public void show(){
		frame.setVisible(true);
	}
}
