package Multimedia;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.format.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;
import com.sun.media.rtp.*;

	public class soloReceiver implements Runnable{
	JavaPhoneApp MyApp;
	Thread ReceiveThread;
	soloRecibir AV;
	
	public soloReceiver(JavaPhoneApp JP){
		 MyApp = JP;
		 ReceiveThread = new Thread(this,"Audio-Phone");
		 ReceiveThread.start(); //BUENOOO
		 //ReceiveThread.run();
	}
	
	public void run(){
		String rpV = Integer.toString(MyApp.RecPortV);
		String rpA = Integer.toString(MyApp.RecPortA);
		int tpV = MyApp.TranPortA;
		int tpA = MyApp.TranPortV;
		String Ip = MyApp.miIP;
		String peerIp= MyApp.IPS;
		String []info = {Ip+"/"+rpV,Ip+"/"+rpA};
		//System.out.println(temp);
		
		//coje el primero que es el micro
		java.util.Vector deviceListVector = CaptureDeviceManager.getDeviceList(null);
		CaptureDeviceInfo deviceInfo = (CaptureDeviceInfo) deviceListVector.elementAt(0);
		MediaLocator mediaLocator = deviceInfo.getLocator();
				
		AV = new soloRecibir(info, MyApp, peerIp,tpV,tpA,mediaLocator);
		MyApp.DisplayMess("Waiting for Incoming call..\n");
		if (!AV.initialize()) {
			MyApp.DisplayMess("Receiver Timing out!!!! Start again!!!\n");
            		//System.err.println("Failed to initialize the sessions.");
            		System.exit(-1);
        	}
		
        	// Check to see if AVReceive is done.
        	try {
            	while (!AV.isDone())
                Thread.sleep(1000);
        	} catch (Exception e) {}
	}
	public void stop(){
		AV.close();
		MyApp.DisplayMess("Receiver Thread stopped....\n");
		//ReceiveThread.destroy();
	}
}

		
		
		     