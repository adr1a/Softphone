package Multimedia;
import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.util.Vector;

import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.rtp.rtcp.*;
import javax.media.protocol.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.Format;
import javax.media.format.FormatChangeEvent;
import javax.media.control.BufferControl;
import javax.media.control.TrackControl;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


/**
 * AVReceive to receive RTP transmission.
 */
public class soloRecibir implements ReceiveStreamListener, SessionListener, 
        ControllerListener
{
    String sessions[] = null;
    RTPManager mgrs[] = null;
    Vector playerWindows = null;
    JavaPhoneApp MyApp;
    String peerIp;
    int tpV;
    int tpA;
    String temp;
    MediaLocator mediaLocator;
    private Processor processor = null;
    private DataSource dataOutput = null;
    private Format formato;
    
    boolean dataReceived = false;
    Object dataSync = new Object();


    public soloRecibir(String sessions[],JavaPhoneApp App,String peerIp, int tpV, int tpA, MediaLocator mediaLocator) {
        this.sessions = sessions;
        MyApp = App;
        this.peerIp = peerIp;
        this.mediaLocator = mediaLocator;
        this.tpV=tpV;
        this.tpA=tpA;
    }

    protected boolean initialize() {

    	 try {

    		 	DataSource ds=null;
    	        try {
    	            ds = javax.media.Manager.createDataSource(mediaLocator);
    	        } catch (Exception e) {
    	            System.out.println("Couldn't create DataSource");
    	        }

    	        // Try to create a processor to handle the input media locator
    	        try {
    	            processor = javax.media.Manager.createProcessor(ds);
    	        } catch (NoProcessorException npe) {
    	        	System.out.println("Couldn't create processor");
    	        } catch (IOException ioe) {
    	        	System.out.println("IOException creating processor");
    	        } 

    	        // Wait for it to configure
    	        boolean result = waitForState(processor, Processor.Configured);
    	        if (result == false)
    	        	System.out.println("Couldn't configure processor");

    	        // Get the tracks from the processor
    	        TrackControl [] tracks = processor.getTrackControls();

    	        // Do we have atleast one track?
    	        if (tracks == null || tracks.length < 1)
    	        	System.out.println("Couldn't find tracks in processor");

    	        // Set the output content descriptor to RAW_RTP
    	        // This will limit the supported formats reported from
    	        // Track.getSupportedFormats to only valid RTP formats.
    	        ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
    	        processor.setContentDescriptor(cd);

    	        Format supported[];
    	        Format chosen,preferido;
    	        boolean atLeastOneTrack = false;

    	        // Program the tracks.
    	        for (int i = 0; i < tracks.length; i++) {
    	          //  Format format = tracks[i].getFormat();
    	            if (tracks[i].isEnabled())
    	            {
    	                    
    	                //Formato AUDIO
    		           // chosen=new AudioFormat(AudioFormat.GSM_RTP);
    		            //chosen=new AudioFormat(AudioFormat.G723_RTP,8000,4,1); //Se puede especificar la FM y los bits por muestra
    		            chosen=new AudioFormat(AudioFormat.ULAW_RTP);
    		            System.err.println("Track " + i + " is set to transmit as:");
    		            //System.err.println("  " + chosen);
    	                   
    	                tracks[i].setFormat(chosen);
    	                temp = "Track  " +i + "is set to transmit as  " + chosen + "\n";
    	                MyApp.DisplayMess(temp);
    	               
    	            }		
    	            else
    	                tracks[i].setEnabled(false);
    	        }
    	      
    	        result = waitForState(processor, Controller.Realized);
    	        if (result == false)
    	        	System.out.println("Couldn't realize processor");

    	        // Get the output data source of the processor
    	        dataOutput = processor.getDataOutput();
    	         
    	        
    	        
        	InetAddress ipAddr;
            SessionAddress destAddr;
            SessionLabel session;
            SendStream sendStream;
          
            mgrs = new com.sun.media.rtp.RTPSessionMgr[sessions.length];
            playerWindows = new Vector();

            

            // Open the RTP sessions.
            for (int i = 0; i < sessions.length; i++) {

                // Parse the session addresses.
                try {
                    session = new SessionLabel(sessions[i]);
                } catch (IllegalArgumentException e) {
		    temp = "Failed to parse the session address given: " + sessions[i]+ "\n";
		    MyApp.DisplayMess(temp); 			    	                	
                    //System.err.println("Failed to parse the session address given: " + sessions[i]);
                    return false;
                }
                temp = " Open connection for Receiving at Addr : " + session.addr + "port: " + session.port + "\n";
                MyApp.DisplayMess(temp); 
                //System.err.println("  - Open RTP session for: addr: " + session.addr + " port: " + session.port + " ttl: " + session.ttl);

                mgrs[i] = new com.sun.media.rtp.RTPSessionMgr();
                mgrs[i].addSessionListener(this);
                mgrs[i].addReceiveStreamListener(this);
                
                ipAddr = InetAddress.getByName(peerIp);
                tpA=tpA+2*i;
                destAddr = new SessionAddress(ipAddr,tpA);
                SessionAddress localAddr = new SessionAddress(InetAddress.getByName(session.addr),session.port);
                mgrs[i].initialize(localAddr);
                    
                // You can try out some other buffer size to see
                // if you can get better smoothness.
                BufferControl bc = (BufferControl)mgrs[i].getControl("javax.media.control.BufferControl");
                if (bc != null)
                    bc.setBufferLength(0); //TAMAÑO BUFFER PARA PERDIDAS (NORMAL 350)
                    
                mgrs[i].addTarget(destAddr);
                
                if (i==0)
                {
                	sendStream = mgrs[i].createSendStream(dataOutput, i);
                	sendStream.start();
                }
            }

} catch (Exception e){
	    temp = "Cannot create The session" + e.getMessage(); 
            //System.err.println("Cannot create the RTP Session: " + e.getMessage());
            return false;
        }

        // Wait for data to arrive before moving on.

        long then = System.currentTimeMillis();
        long waitingPeriod = 600000;  // wait for a maximum of 600 secs.

        try{
            synchronized (dataSync) {
                while (!dataReceived && 
                        System.currentTimeMillis() - then < waitingPeriod) {
                    if (!dataReceived)
                       // System.err.println("  - Waiting for RTP data to arrive...");
                    dataSync.wait(1000);
                }
            }
        } catch (Exception e) {}

        if (!dataReceived) {
            temp = "No Data was received\n";
            MyApp.DisplayMess(temp);	
            //System.err.println("No RTP data was received.");
            close();
            return false;
        }
        MyApp.DisplayMess("Receiving A call...call Established\n");
        return true;
    }


    public boolean isDone() {
        return playerWindows.size() == 0;
    }


    /**
     * Close the players and the session managers.
     */
    protected void close() {

        for (int i = 0; i < playerWindows.size(); i++) {
            try {
                ((PlayerWindow)playerWindows.elementAt(i)).close();
            } catch (Exception e) {}
        }

        playerWindows.removeAllElements();

        // close the RTP session.
        for (int i = 0; i < mgrs.length; i++) {
            if (mgrs[i] != null) {
                mgrs[i].removeTargets("Closing session from AVReceive");
                mgrs[i].dispose();
                mgrs[i] = null;
            }
        }
    }


    PlayerWindow find(Player p) {
        for (int i = 0; i < playerWindows.size(); i++) {
            PlayerWindow pw = (PlayerWindow)playerWindows.elementAt(i);
            if (pw.player == p)
                return pw;
        }
        return null;
    }


    PlayerWindow find(ReceiveStream strm) {
        for (int i = 0; i < playerWindows.size(); i++) {
            PlayerWindow pw = (PlayerWindow)playerWindows.elementAt(i);
            if (pw.stream == strm)
                return pw;
        }
        return null;
    }


    /**
     * SessionListener.
     */
    public synchronized void update(SessionEvent evt) {
        if (evt instanceof NewParticipantEvent) {
            Participant p = ((NewParticipantEvent)evt).getParticipant();
            temp = " A Call is established with the user " + " " + p.getCNAME()+ "\n";
            MyApp.DisplayMess(temp);
            //System.err.println("  - A new participant had just joined: " + p.getCNAME());
        }
    }


    /**
     * ReceiveStreamListener
     */
    public synchronized void update( ReceiveStreamEvent evt) {

        RTPManager mgr = (RTPManager)evt.getSource();
        Participant participant = evt.getParticipant(); // could be null.
        ReceiveStream stream = evt.getReceiveStream();  // could be null.

        if (evt instanceof RemotePayloadChangeEvent) {
     
            System.err.println("  - Received an RTP PayloadChangeEvent.");
            System.err.println("Sorry, cannot handle payload change.");
            System.exit(0);

        }
    
        else if (evt instanceof NewReceiveStreamEvent) {

            try {
                stream = ((NewReceiveStreamEvent)evt).getReceiveStream();
                DataSource ds = stream.getDataSource();

                // Find out the formats.
                RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
                if (ctl != null){
                   ;//  System.err.println("  - Recevied new RTP stream: " + ctl.getFormat());
                } else
                    ;//System.err.println("  - Recevied new RTP stream");

                if (participant == null)
                    ;//System.err.println("      The sender of this stream had yet to be identified.");
                else {
                    ;//System.err.println("      The stream comes from: " + participant.getCNAME()); 
                }

                // create a player by passing datasource to the Media Manager
                Player p = javax.media.Manager.createPlayer(ds);
                if (p == null)
                    return;

                p.addControllerListener(this);
                p.realize();
                PlayerWindow pw = new PlayerWindow(p, stream);
                playerWindows.addElement(pw);
               
              
                // Notify intialize() that a new stream had arrived.
                synchronized (dataSync) {
                    dataReceived = true;
                    dataSync.notifyAll();
                }

            } catch (Exception e) {
                System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
                return;
            }
        
        }

        else if (evt instanceof StreamMappedEvent) {

             if (stream != null && stream.getDataSource() != null) {
                DataSource ds = stream.getDataSource();
                // Find out the formats.
                RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
                //System.err.println("  - The previously unidentified stream ");
                if (ctl != null)
                    System.err.println("      " + ctl.getFormat());
                temp = " Call Established with " + " " + participant.getCNAME() + "\n";    
                //System.err.println("      had now been identified as sent by: " + participant.getCNAME());
             }
        }

        else if (evt instanceof ByeEvent) {
	     temp = " connection terminated by " + " " + participant.getCNAME() + "\n";
	     MyApp.DisplayMess(temp);	
             System.err.println("  - Got \"bye\" from: " + participant.getCNAME());
             PlayerWindow pw = find(stream);
             if (pw != null) {
                pw.close();
                playerWindows.removeElement(pw);
             }
        }

    }


    /**
     * ControllerListener for the Players.
     */
    public synchronized void controllerUpdate(ControllerEvent ce) {

        Player p = (Player)ce.getSourceController();

        if (p == null)
            return;

        // Get this when the internal players are realized.
        if (ce instanceof RealizeCompleteEvent) {
            PlayerWindow pw = find(p);
            if (pw == null) {
                // Some strange happened.
                System.err.println("Internal error!");
                System.exit(-1);
            }
            pw.initialize();
            pw.setVisible(true);
            p.start();
        }

        if (ce instanceof ControllerErrorEvent) {
            p.removeControllerListener(this);
            PlayerWindow pw = find(p);
            if (pw != null) {
                pw.close();     
                playerWindows.removeElement(pw);
            }
            System.err.println("AVReceive internal error: " + ce);
        }

    }
    

    private Integer stateLock = new Integer(0);
    private boolean failed = false;
    
    Integer getStateLock() {
        return stateLock;
    }

    void setFailed() {
        failed = true;
    }
    
    private synchronized boolean waitForState(Processor p, int state) {
        p.addControllerListener(new StateListener());
        failed = false;

        // Call the required method on the processor
        if (state == Processor.Configured) {
            p.configure();
        } else if (state == Processor.Realized) {
            p.realize();
        }
        
        // Wait until we get an event that confirms the
        // success of the method, or a failure event.
        // See StateListener inner class
        while (p.getState() < state && !failed) {
            synchronized (getStateLock()) {
                try {
                    getStateLock().wait();
                } catch (InterruptedException ie) {
                    return false;
                }
            }
        }

        if (failed)
            return false;
        else
            return true;
    }
    
    class StateListener implements ControllerListener {

        public void controllerUpdate(ControllerEvent ce) {

            // If there was an error during configure or
            // realize, the processor will be closed
            if (ce instanceof ControllerClosedEvent)
                setFailed();

            // All controller events, send a notification
            // to the waiting thread in waitForState method.
            if (ce instanceof ControllerEvent) {
                synchronized (getStateLock()) {
                    getStateLock().notifyAll();
                }
            }
        }
    }


    /**
     * A utility class to parse the session addresses.
     */
    class SessionLabel {

        public String addr = null;
        public int port;
        public int ttl = 1;

        SessionLabel(String session) throws IllegalArgumentException {

            int off;
            String portStr = null, ttlStr = null;

            if (session != null && session.length() > 0) {
                while (session.length() > 1 && session.charAt(0) == '/')
                    session = session.substring(1);

                // Now see if there's a addr specified.
                off = session.indexOf('/');
                if (off == -1) {
                    if (!session.equals(""))
                        addr = session;
                } else {
                    addr = session.substring(0, off);
                    session = session.substring(off + 1);
                    // Now see if there's a port specified
                    off = session.indexOf('/');
                    if (off == -1) {
                        if (!session.equals(""))
                            portStr = session;
                    } else {
                        portStr = session.substring(0, off);
                        session = session.substring(off + 1);
                        // Now see if there's a ttl specified
                        off = session.indexOf('/');
                        if (off == -1) {
                            if (!session.equals(""))
                                ttlStr = session;
                        } else {
                            ttlStr = session.substring(0, off);
                        }
                    }
                }
            }

            if (addr == null)
                throw new IllegalArgumentException();

            if (portStr != null) {
                try {
                    Integer integer = Integer.valueOf(portStr);
                    if (integer != null)
                        port = integer.intValue();
                } catch (Throwable t) {
                    throw new IllegalArgumentException();
                }
            } else
                throw new IllegalArgumentException();

            if (ttlStr != null) {
                try {
                    Integer integer = Integer.valueOf(ttlStr);
                    if (integer != null)
                        ttl = integer.intValue();
                } catch (Throwable t) {
                    throw new IllegalArgumentException();
                }
            }
        }
    }


    /**
     * GUI classes for the Player.
     */
    class PlayerWindow extends Frame {

        Player player;
        ReceiveStream stream;

        PlayerWindow(Player p, ReceiveStream strm) {
            player = p;
            stream = strm;
        }

        public void initialize() {
            add(new PlayerPanel(player));
        }

        public void close() {
            player.close();
            setVisible(false);
            dispose();
        }

        public void addNotify() {
            super.addNotify();
            pack();
        }
    }


    /**
     * GUI classes for the Player.
     */
    class PlayerPanel extends Panel {

        Component vc, cc;       
        PlayerPanel(Player p) {
            setLayout(new BorderLayout());
            if ((vc = p.getVisualComponent()) != null)
            {
                add("Center", vc);
            }
            if ((cc = p.getControlPanelComponent()) != null)
                add("South", cc);
        }
        

        public Dimension getPreferredSize() {
            int w = 0, h = 0;
            if (vc != null) {
                Dimension size = vc.getPreferredSize();
                w = size.width;
                h = size.height;
            }
            if (cc != null) {
                Dimension size = cc.getPreferredSize();
                if (w == 0)
                    w = size.width;
                h += size.height;
            }
            if (w < 160)
                w = 160;
            return new Dimension(w, h);
        }
    }

}// end of AVReceive 




