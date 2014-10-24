package com.EECE412A3;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class MainUI extends JFrame implements ActionListener {

	private JButton	m_btnClient;
	private JButton m_btnServer;
	
	public MainUI()
	{
		super("Title");
		this.setSize(300,300);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new FlowLayout());
		this.setVisible(true);

		m_btnClient = new JButton("Client");
		m_btnServer = new JButton("Server");
		
		m_btnClient.addActionListener(this);
		m_btnServer.addActionListener(this);
		
		this.add(m_btnClient);
		this.add(m_btnServer);
		displayClient();
	}
	
	public void actionPerformed( ActionEvent e )
	{
		if(e.getSource() == m_btnClient) displayClient();
		if(e.getSource() == m_btnServer) displayServer();
	}
	
	private void displayClient()
	{
		SwingUtilities.invokeLater( new Runnable(){
			public void run() {
				new ClientUI(); 
			}
		});
	}
	
	private void displayServer()
	{
		SwingUtilities.invokeLater( new Runnable(){
			public void run() {
				new ServerUI(); 
			}
			
		});
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		SwingUtilities.invokeLater( new Runnable(){
			public void run() {
				new MainUI(); 
			}
			
		});
	}
}