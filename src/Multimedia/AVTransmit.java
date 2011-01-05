package Multimedia;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.util.Vector;

import javax.media.*;
import javax.media.protocol.*;
import javax.media.format.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import javax.media.rtp.*;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.media.rtp.rtcp.*;

import com.sun.media.rtp.*;

public class AVTransmit implements ReceiveStreamListener, SessionListener, ControllerListener {

    // Input MediaLocator
    // Can be a file or http or capture source
    private MediaLocator locator;
    private String ipAddress;
    private String localIp;
    private int portBase;
    private int sourcePort;
    private Format formato;
    private Processor processor = null;
    private RTPManager rtpMgrs[];
    private DataSource dataOutput = null;
    JavaPhoneApp MyApp;
    String temp;
    Object dataSync = new Object();
    boolean dataReceived = false;
    Vector playerWindows = null;
    
    public AVTransmit(   JavaPhoneApp App,
                         MediaLocator locator,
                         String ipAddress, String localIP,
                         int pb,int sourcePort,
                         Format format) {
        
        this.locator = locator;
        this.ipAddress = ipAddress;
        this.localIp = localIP;
        this.formato= format;
        MyApp = App;
        this.portBase = pb;
        this.sourcePort = sourcePort;
    }

    /**
     * Starts the transmission. Returns null if transmission started ok.
     * Otherwise it returns a string with the reason why the setup failed.
     */
    public synchronized String start() {
        String result;

        // Create a processor for the specified media locator
        // and program it to output JPEG/RTP
        result = createProcessor();
        if (result != null)
            return result;

        // Create an RTP session to transmit the output of the
        // processor to the specified IP address and port no.
        result = createTransmitter();
        if (result != null) {
            processor.close();
            processor = null;
            return result;
        }

        // Start the transmission
        processor.start();
        
        return null;
    }

    /**
     * Stops the transmission if already started
     */
    public void stop() {
        synchronized (this) {
            if (processor != null) {
                processor.stop();
                processor.close();
                processor = null;
                for (int i = 0; i < rtpMgrs.length; i++)
                {
                    rtpMgrs[i].dispose();
                    rtpMgrs[i].removeTargets("Session stopped.");
                }
            }
        }
    }

    private String createProcessor() {
        if (locator == null)
            return "Locator is null";

        DataSource ds;
        DataSource clone;

        try {
            ds = javax.media.Manager.createDataSource(locator);
        } catch (Exception e) {
            return "Couldn't create DataSource";
        }

        // Try to create a processor to handle the input media locator
        try {
            processor = javax.media.Manager.createProcessor(ds);
        } catch (NoProcessorException npe) {
            return "Couldn't create processor";
        } catch (IOException ioe) {
            return "IOException creating processor";
        } 

        // Wait for it to configure
        boolean result = waitForState(processor, Processor.Configured);
        if (result == false)
            return "Couldn't configure processor";

        // Get the tracks from the processor
        TrackControl [] tracks = processor.getTrackControls();

        // Do we have atleast one track?
        if (tracks == null || tracks.length < 1)
            return "Couldn't find tracks in processor";

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

               // supported = tracks[i].getSupportedFormats();

                // We've set the output content to the RAW_RTP.
                // So all the supported formats should work with RTP.
                // We'll just pick the first one.

               // if (supported.length > 0) {
                    if (formato instanceof VideoFormat)
                    {
                        // For video formats, we should double check the
                        // sizes since not all formats work in all sizes.
                    	//preferido=new Format(VideoFormat.JPEG_RTP);
                    	preferido = new Format(VideoFormat.H263_RTP);
                        chosen = checkForVideoSizes(formato, preferido); //Si hace falta cambiar los tamaños originales para meterlos en el RTP (standard)
                    } else
                    {
                    	//Formato AUDIO
	                    chosen=new AudioFormat(AudioFormat.GSM_RTP);
	                    //chosen=new AudioFormat(AudioFormat.G723_RTP,8000,4,1); //Se puede especificar la FM y los bits por muestra
	                    //chosen=new AudioFormat(AudioFormat.ULAW_RTP);
	                    System.err.println("Track " + i + " is set to transmit as:");
	                    //System.err.println("  " + chosen);
                    }
                    tracks[i].setFormat(chosen);
                    temp = "Track  " +i + "is set to transmit as  " + chosen + "\n";
                    MyApp.DisplayMess(temp);
                    atLeastOneTrack = true;
            }		
            else
                tracks[i].setEnabled(false);
        }

        if (!atLeastOneTrack)
            return "Couldn't set any of the tracks to a valid RTP format";

        // Realize the processor. This will internally create a flow
        // graph and attempt to create an output datasource for JPEG/RTP
        // audio frames.
        result = waitForState(processor, Controller.Realized);
        if (result == false)
            return "Couldn't realize processor";

        // Set the JPEG quality to .5.
        setJPEGQuality(processor, 0.5f); //Aunque elegimod h.263 este comando no afecta
        // Get the output data source of the processor
        dataOutput = processor.getDataOutput();

        return null;
    }


    /**
     * Use the SessionManager API to create sessions for each media 
     * track of the processor.
     */
    private String createTransmitter() {

        // Cheated.  Should have checked the type.
        PushBufferDataSource pbds = (PushBufferDataSource)dataOutput;
        PushBufferStream pbss[] = pbds.getStreams();

        rtpMgrs = new RTPManager[pbss.length];
        SessionAddress localAddr, destAddr;
        InetAddress ipAddr;
        SendStream sendStream;
        int port;
        SourceDescription srcDesList[];
        
        playerWindows = new Vector();
        for (int i = 0; i < pbss.length; i++) {
            try {
            	rtpMgrs[i] = RTPManager.newInstance();         
                rtpMgrs[i].addSessionListener(this);
                rtpMgrs[i].addReceiveStreamListener(this);
               
                port = portBase + 2*i;
                ipAddr = InetAddress.getByName(ipAddress);
                destAddr = new SessionAddress(ipAddr, port, ipAddr, port + 1);
                InetAddress local = InetAddress.getByName(localIp);
                localAddr= new SessionAddress(local, sourcePort);
                rtpMgrs[i].initialize(localAddr);
                rtpMgrs[i].addTarget(destAddr);
                temp = "Started transmission channel at " + ipAddress + " " + port + "\n";
                MyApp.DisplayMess(temp);
                //System.err.println("Created RTP session: " + ipAddress + " " + port);
                sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
                sendStream.start();
            } catch (Exception  e) {
                return e.getMessage();
            }
        }
        return null;
        
    }


    /**
     * For JPEG and H263, we know that they only work for particular
     * sizes.  So we'll perform extra checking here to make sure they
     * are of the right sizes.
     */
    	Format checkForVideoSizes(Format original, Format supported) {

        int width, height;
        Dimension size = ((VideoFormat)original).getSize();
        Format jpegFmt = new Format(VideoFormat.JPEG_RTP);
        Format h263Fmt = new Format(VideoFormat.H263_RTP);

        if (supported.matches(jpegFmt)) {
            // For JPEG, make sure width and height are divisible by 8.
            width = (size.width % 8 == 0 ? size.width :
                                (int)(size.width / 8) * 8);
            height = (size.height % 8 == 0 ? size.height :
                                (int)(size.height / 8) * 8);
        } else if (supported.matches(h263Fmt)) {
            // For H.263, we only support some specific sizes.
            if (size.width < 128) {
                width = 128;
                height = 96;
            } else if (size.width < 176) {
                width = 176;
                height = 144;
            } else {
                width = 352;
                height = 288;
            }
        } else {
            // We don't know this particular format.  We'll just
            // leave it alone then.
            return supported;
        }

        return (new VideoFormat(null, 
                                new Dimension(width, height), 
                                Format.NOT_SPECIFIED,
                                null,
                                Format.NOT_SPECIFIED)).intersects(supported);
    }


    /**
     * Setting the encoding quality to the specified value on the JPEG encoder.
     * 0.5 is a good default.
     */
    void setJPEGQuality(Player p, float val) {

        Control cs[] = p.getControls();
        QualityControl qc = null;
        VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

        // Loop through the controls to find the Quality control for
        // the JPEG encoder.
        for (int i = 0; i < cs.length; i++) {

            if (cs[i] instanceof QualityControl &&
                cs[i] instanceof Owned) {
                Object owner = ((Owned)cs[i]).getOwner();

                // Check to see if the owner is a Codec.
                // Then check for the output format.
                if (owner instanceof Codec) {
                    Format fmts[] = ((Codec)owner).getSupportedOutputFormats(null);
                    for (int j = 0; j < fmts.length; j++) {
                        if (fmts[j].matches(jpegFmt)) {
                            qc = (QualityControl)cs[i];
                            qc.setQuality(val);
                            //System.err.println("- Setting quality to " + 
                              //          val + " on " + qc);
                            break;
                        }
                    }
                }
                if (qc != null)
                    break;
            }
        }
    }


    /****************************************************************
     * Convenience methods to handle processor's state changes.
     ****************************************************************/
    
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
                System.out.println("prueba5");
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

	@Override
	 public synchronized void update(SessionEvent evt) {
        if (evt instanceof NewParticipantEvent) {
            Participant p = ((NewParticipantEvent)evt).getParticipant();
            temp = " A Call is established with the user " + " " + p.getCNAME()+ "\n";
            MyApp.DisplayMess(temp);
            //System.err.println("  - A new participant had just joined: " + p.getCNAME());
        }
    }

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
        for (int i = 0; i < rtpMgrs.length; i++) {
            if (rtpMgrs[i] != null) {
            	rtpMgrs[i].removeTargets("Closing session from AVReceive");
            	rtpMgrs[i] = null;
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

    /****************************************************************
     * Inner Classes
     ****************************************************************/

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
	                add("Center", vc);
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

}

