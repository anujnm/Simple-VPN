package com.EECE412A3;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import com.EECE412A3.Client.Client;
import com.EECE412A3.Client.ClientInterface;



@SuppressWarnings("serial")
public class ClientUI extends JFrame implements ActionListener, GUIInterface {
	
	//Host
	private JLabel		m_lblHost;
	private TextField	m_txtHost;
	
	//Port
	private JLabel		m_lblPort;
	private	TextField	m_txtPort;
	
	//Shared Key
	private JLabel		m_lblSharedKey;
	private TextField	m_txtSharedKey;
	
	//Start / Stop
	private JButton		m_btnStart;
	private JButton		m_btnStop;
	
	//Data to be Sent
	private JLabel		m_lblInput;
	private TextField	m_txtInput;
	private JButton		m_btnInput;
	
	//Data as Received
	private JLabel		m_lblConsole;
	private JTextArea	m_txtConsole;
	
	private ClientInterface m_crypto;
	
	public ClientUI()
	{
		super("Client");
		this.setSize(500,800);
		this.setLocationRelativeTo(null);		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout layout = new SpringLayout();
		this.setLayout( layout );
		this.setVisible(true);
		
		//Host
		m_lblHost = new JLabel("Host:");
		m_txtHost = new TextField(25);
		
		//Port
		m_lblPort = new JLabel("Port:");
		m_txtPort = new TextField(10);

		//Shared Key
		m_lblSharedKey = new JLabel("Shared Key:");
		m_txtSharedKey = new TextField(25);

		
		//Start / Stop
		m_btnStart = new JButton("Connect");
		m_btnStop = new JButton("Disconnect");
		m_btnStop.setEnabled(false);
		m_btnStart.addActionListener(this);
		m_btnStop.addActionListener(this);
		
		//Data to be Sent
		m_lblInput = new JLabel("Data to be Sent:");
		m_txtInput = new TextField();
		m_btnInput = new JButton("Send");
		m_txtInput.setEditable(false);
		m_btnInput.setEnabled(false);
		m_btnInput.addActionListener(this);
		
		//Data as Received
		m_lblConsole = new JLabel("Data as Received:");
		m_txtConsole = new JTextArea();
		m_txtConsole.setEditable(false);
		JScrollPane txtConsoleScroll = new JScrollPane(m_txtConsole);
		
		//Host
		layout.putConstraint(SpringLayout.WEST, m_txtHost, 110, SpringLayout.WEST, this.getContentPane());
		layout.putConstraint(SpringLayout.EAST, m_txtHost, -5, SpringLayout.EAST, this.getContentPane());
		
		layout.putConstraint(SpringLayout.EAST, m_lblHost, -5, SpringLayout.WEST, m_txtHost);
		
		//Port
		layout.putConstraint(SpringLayout.WEST, m_txtPort, 0, SpringLayout.WEST, m_txtHost);
		layout.putConstraint(SpringLayout.NORTH, m_txtPort, 5, SpringLayout.SOUTH, m_txtHost );
		
		layout.putConstraint(SpringLayout.EAST, m_lblPort, -5, SpringLayout.WEST, m_txtPort);
		layout.putConstraint(SpringLayout.NORTH, m_lblPort, 5, SpringLayout.SOUTH, m_txtHost );
		
		//Shared Key
		layout.putConstraint(SpringLayout.WEST, m_txtSharedKey, 0, SpringLayout.WEST, m_txtPort );
		layout.putConstraint(SpringLayout.NORTH, m_txtSharedKey, 5, SpringLayout.SOUTH, m_txtPort );
		layout.putConstraint(SpringLayout.EAST, m_txtSharedKey, -5, SpringLayout.EAST, this.getContentPane());
		
		layout.putConstraint(SpringLayout.EAST, m_lblSharedKey, -5, SpringLayout.WEST, m_txtPort );
		layout.putConstraint(SpringLayout.NORTH, m_lblSharedKey, 5, SpringLayout.SOUTH, m_txtPort );
		
		
		//Start / Stop
		layout.putConstraint(SpringLayout.NORTH, m_btnStart, 5, SpringLayout.SOUTH, m_txtSharedKey);
		layout.putConstraint(SpringLayout.WEST, m_btnStart, 0, SpringLayout.WEST, m_txtSharedKey);
		
		layout.putConstraint(SpringLayout.NORTH, m_btnStop, 5, SpringLayout.SOUTH, m_txtSharedKey);
		layout.putConstraint(SpringLayout.WEST, m_btnStop, 5, SpringLayout.EAST, m_btnStart);
		
		//Data to be Sent
		layout.putConstraint(SpringLayout.WEST, m_txtInput, 0, SpringLayout.WEST, m_btnStart );
		layout.putConstraint(SpringLayout.NORTH, m_txtInput, 5, SpringLayout.SOUTH, m_btnStart );
		layout.putConstraint(SpringLayout.EAST, m_txtInput, -74, SpringLayout.EAST, this.getContentPane());
		
		layout.putConstraint(SpringLayout.EAST, m_lblInput, -5, SpringLayout.WEST, m_btnStart);
		layout.putConstraint(SpringLayout.NORTH, m_lblInput, 5, SpringLayout.SOUTH, m_btnStart );
		
		layout.putConstraint(SpringLayout.WEST, m_btnInput, 5, SpringLayout.EAST, m_txtInput );
		layout.putConstraint(SpringLayout.NORTH, m_btnInput, 5, SpringLayout.SOUTH, m_btnStart );
		
		
		//Data as Received
		layout.putConstraint(SpringLayout.WEST, txtConsoleScroll, 0, SpringLayout.WEST, m_txtInput );
		layout.putConstraint(SpringLayout.NORTH, txtConsoleScroll, 5, SpringLayout.SOUTH, m_txtInput );
		layout.putConstraint(SpringLayout.SOUTH, txtConsoleScroll, -5, SpringLayout.SOUTH, this.getContentPane());
		layout.putConstraint(SpringLayout.EAST, txtConsoleScroll, -5, SpringLayout.EAST, this.getContentPane());

		layout.putConstraint(SpringLayout.EAST, m_lblConsole, -5, SpringLayout.WEST, txtConsoleScroll );
		layout.putConstraint(SpringLayout.NORTH, m_lblConsole, 5, SpringLayout.SOUTH, m_txtInput );
		
		//Host
		this.add(m_lblHost);
		this.add(m_txtHost);
		
		//Port
		this.add(m_lblPort);
		this.add(m_txtPort);
		
		//Shared Key
		this.add(m_lblSharedKey);
		this.add(m_txtSharedKey);
		
		//Start / Stop
		this.add(m_btnStart);
		this.add(m_btnStop);
		
		//Data to be Sent
		this.add(m_lblInput);
		this.add(m_txtInput);
		this.add(m_btnInput);
		
		//Data as Received
		this.add(m_lblConsole);
		this.add(txtConsoleScroll);
		
		m_crypto = new Client(this);
	}
	
	public void actionPerformed( ActionEvent e )
	{
		if(e.getSource() == m_btnStart) start();
		else if(e.getSource() == m_btnStop) stop();
		else if(e.getSource() == m_btnInput ) send();
	}
	
	private void send()
	{
		String data = m_txtInput.getText();
		//printf("Sending message: " + data);
		if(m_crypto.sendMessage( data ))
		{
			//printf("Sucessfully sent message");
		}
		else
		{
			printf("Unable to send message");
		}
	}
	private void stop()
	{
		m_crypto.endConnection();
	
	}
	private void start()
	{
		int port;
		String sharedKey = m_txtSharedKey.getText();
		String host = m_txtHost.getText();
		if( host.length() == 0 )
		{
			printf("Host can not be empty");
			return;
		}
		
		try
		{
			port = Integer.valueOf(m_txtPort.getText());
		}
		catch( NumberFormatException e)
		{
			printf("Port is not a valid number");
			return;
		}
		
		if( sharedKey.length() == 0 )
		{
			printf("Shared Key can not be empty");
			return;
		}
		
		m_btnStart.setEnabled(false);
		if(!m_crypto.startClient(host, port, sharedKey))
		{
			printf("Unable to establish connection to server.");
			m_btnStart.setEnabled(true);
		}		
	}
	
	@Override
	public void printf(String s)
	{
		m_txtConsole.setText(m_txtConsole.getText() + s + "\n" );
	}

	@Override
	public void connectionReady() {
		printf("Connection established.");
		m_txtInput.setEditable(true);
		m_btnInput.setEnabled(true);
		m_btnStop.setEnabled(true);
		m_btnStart.setEnabled(false);
		Thread t = new Thread(new Runnable(){
			public void run(){
				m_crypto.receiveMessages();
			}
		});
		t.start();
	}
	@Override
	public void connectionClosed()
	{
		printf("Client Stopped.");
		m_txtInput.setEditable(false);
		m_btnInput.setEnabled(false);
		m_btnStop.setEnabled(false);
		m_btnStart.setEnabled(true);
	}
}
