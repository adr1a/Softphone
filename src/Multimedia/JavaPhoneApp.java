package Multimedia;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JavaPhoneApp extends javax.swing.JFrame 
{	// declare gui components...
	javax.swing.JPanel Container = new javax.swing.JPanel();
	javax.swing.JTextArea Display = new javax.swing.JTextArea();
	javax.swing.JLabel LHost = new javax.swing.JLabel();
	javax.swing.JLabel LReceivePort = new javax.swing.JLabel();
	javax.swing.JLabel LTransmitPort = new javax.swing.JLabel();
	javax.swing.JTextField TFHost = new javax.swing.JTextField();
	javax.swing.JTextField TFReceivePort = new javax.swing.JTextField();
	javax.swing.JTextField TFTransmitPort = new javax.swing.JTextField();
	javax.swing.JButton BConnect = new javax.swing.JButton();
	javax.swing.JButton BStop = new javax.swing.JButton();
	static JavaPhoneApp MyApp;
	String miIP,IPS;
	int TranPortA,TranPortV,RecPortV,RecPortA;
	Transmitter MyTransmit;
	soloTransmitter soloTransmitir;
	soloReceiver soloRecibir;

	public JavaPhoneApp()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setTitle("JFC Application");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(null);
		setSize(600,500);
		setVisible(false);
		Container.setLayout(null);
		getContentPane().add(Container);
		Container.setBounds(0,0,600,500);
		Container.add(Display);
		Display.setBounds(0,200,600,400);
		LHost.setText("Connect To");
		Container.add(LHost);
		LHost.setBounds(24,24,100,40);
		LReceivePort.setText("ReceivePort");
		Container.add(LReceivePort);
		LReceivePort.setBounds(24,60,72,40);
		LTransmitPort.setText("TransmitPort");
		Container.add(LTransmitPort);
		LTransmitPort.setBounds(24,96,84,40);
		Container.add(TFHost);
		TFHost.setBounds(156,36,180,22);
		Container.add(TFReceivePort);
		TFReceivePort.setBounds(156,72,179,20);
		Container.add(TFTransmitPort);
		TFTransmitPort.setBounds(156,108,181,19);
		BConnect.setText("Connect");
		Container.add(BConnect);
		BConnect.setBounds(84,156,84,30);
		BStop.setText("Stop");
		Container.add(BStop);
		BStop.setBounds(240,156,80,32);
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		SymMouse aSymMouse = new SymMouse();
		BConnect.addMouseListener(aSymMouse);
		BStop.addMouseListener(aSymMouse);
	}
		/*SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		SymMouse aSymMouse = new SymMouse();
		BConnect.addMouseListener(aSymMouse);
		BStop.addMouseListener(aSymMouse);*/

	public void startMedia (String myIp, String peerIP, int TranPortA, int TranPortV, int RecPortA, int RecPortV){
		  
		  
		  AVTransmit ToChannel;
		  this.miIP=myIp;
		  this.IPS=peerIP;
		  this.TranPortA=TranPortA;
		  this.TranPortV=TranPortV;
		  this.RecPortA=RecPortA;
		  this.RecPortV=RecPortV;
		  try {   
			  VFWAuto a = new VFWAuto();
				a.detectaDispositivos();
			  MyApp = new JavaPhoneApp();
		  } catch (Throwable t) {		   
			  t.printStackTrace();
			  //Ensure the application exits with an error condition.		  
			  System.exit(1);
		  }	 
		  MyTransmit = new Transmitter(this);	 
	}


	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();
		
		super.addNotify();
		
		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;
		
		// Adjust size of frame according to the insets and menu bar
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
		    menuBarHeight = menuBar.getPreferredSize().height;
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;
	
	void exitApplication()
	{
		try {
	    	// Beep
	    	Toolkit.getDefaultToolkit().beep();
	    	// Show a confirmation dialog
	    	int reply = JOptionPane.showConfirmDialog(this, 
	    	                                          "Do you really want to exit?", 
	    	                                          "JFC Application - Exit" , 
	    	                                          JOptionPane.YES_NO_OPTION, 
	    	                                          JOptionPane.QUESTION_MESSAGE);
			// If the confirmation was affirmative, handle exiting.
			if (reply == JOptionPane.YES_OPTION)
			{
		    	this.setVisible(false);    // hide the Frame
		    	this.dispose();            // free the system resources
		    	System.exit(0);            // close the application
			}
		} catch (Exception e) {
		}
	}
	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == JavaPhoneApp.this)
				JavaPhone_windowClosing(event);
		}
	}

	void JavaPhone_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
		System.exit(1);
	}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			
			
		}
	}
	class SymMouse extends java.awt.event.MouseAdapter
	{
		public void mousePressed(java.awt.event.MouseEvent event)
		{
			Object object = event.getSource();
			if (object == BStop)
				BStop_mousePressed(event);
		}

		public void mouseClicked(java.awt.event.MouseEvent event)
		{
			Object object = event.getSource();
			if (object == BConnect)
				BConnect_mouseClicked(event);
		}
	}
	void BConnect_mouseClicked(java.awt.event.MouseEvent event)
	{
		IPS= "192.168.11.1";
		miIP="192.168.11.4";
		TranPortA=50050; //puerto destino y origen audio
		TranPortV=50052; //puerto destino y origen video
		RecPortA=50010;
		RecPortV=50012;
		
		//Detectar los dispositivos automaticamente
		/*VFWAuto a = new VFWAuto();
		a.detectaDispositivos();*/

		if ( IPS.equals(null)){
			Display.setText("Error: Enter All the Fields!!");
		}
		else{
			MyTransmit = new Transmitter(this);
			//soloRecibir = new soloReceiver(this);
			
		}
		     
	}
	void DisplayMess(String str){
		Display.append(str);
	}	
	

	void BStop_mousePressed(java.awt.event.MouseEvent event)
	{
		// to do: code goes here.
		MyTransmit.stop();
		//System.exit(0);	 
		MyApp.exitApplication();
	}
	}