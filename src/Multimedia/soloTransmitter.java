package Multimedia;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.format.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;
import com.sun.media.rtp.*;

public class soloTransmitter implements Runnable{

	JavaPhoneApp MyApp;
	Thread TransmitThread;
	soloTransmitir AV;
	soloTransmitir AV2;
	VideoFormat			captureVideoFormat;
	AudioFormat			captureAudioFormat;
	String peerIp;
	String miIP;
	int recA;
	int recV;
	int tpV;
	int tpA;
	MediaLocator videoMediaLocator;
	MediaLocator audioMediaLocator;
	CaptureDeviceInfo	captureVideoDevice;
	CaptureDeviceInfo	captureAudioDevice;
	
	public soloTransmitter(JavaPhoneApp JP){
		MyApp = JP;
		TransmitThread = new Thread(this,"Audio Phone");
		TransmitThread.start(); //BUENOOOO
		//this.run();
	}
	
	public void run(){
	
	tpV = MyApp.TranPortV; //puerto destinp video
	tpA = MyApp.TranPortA;
	peerIp = MyApp.IPS;
	miIP = MyApp.miIP;
	recA= MyApp.RecPortA;
	recV= MyApp.RecPortV;
	
	captureVideoFormat = null;
	captureAudioFormat = null;
	String				defaultVideoFormatString = "size=352x288, encoding=yuv";
	//String				defaultVideoFormatString = "size=352x288, encoding=rgb";
	String				defaultAudioFormatString = "linear, 8000.0 hz, 8-bit";
	Format fmt = null;
	
	
	//Estos bucles son prescindibles, sirven para escoger un tipo de submuestro (YUV) y un audio especifico
	//y tambien para listar los dispositivos, si lo borramos escogera el primer dispositivo que encuentre y el priemr formato
	java.util.Vector deviceListVector = CaptureDeviceManager.getDeviceList(null);
	if (deviceListVector == null)
	{
		System.out.println("... No hay dispositivos");
		System.exit(0);
	}
	for (int x = 0; x < deviceListVector.size(); x++)
	{
		// display device name
		CaptureDeviceInfo deviceInfo = (CaptureDeviceInfo) deviceListVector.elementAt(x);
		String deviceInfoText = deviceInfo.getName();
		// display device formats
		Format deviceFormat[] = deviceInfo.getFormats();
		for (int y = 0; y < deviceFormat.length; y++)
		{
			// serach for default video device
			if (captureVideoDevice == null)
			{
				if (deviceFormat[y] instanceof VideoFormat)
				{	//Lo comentado es para especificar una cam si es que hay varias (hay que saber el nombre)
					//if (deviceInfo.getName().indexOf(defaultVideoDeviceName) >= 0)
					
						captureVideoDevice = deviceInfo;
						//System.out.println(">>> capture video device = " + deviceInfo.getName());	
				}
			}
			// search for default video format
			if (captureVideoDevice == deviceInfo)
			{
				if (captureVideoFormat == null)
				{
					//System.out.println("video: "+DeviceInfo.formatToString(deviceFormat[y]).indexOf(defaultVideoFormatString));
					if (DeviceInfo.formatToString(deviceFormat[y]).indexOf(defaultVideoFormatString) >= 0)
					{
						captureVideoFormat = (VideoFormat) deviceFormat[y];
						System.out.println(">>> capture video format = " + DeviceInfo.formatToString(deviceFormat[y]));
					}
				}
			}
			// serach for default audio device
			if (captureAudioDevice == null)
			{
				if (deviceFormat[y] instanceof AudioFormat)
				{
					//System.out.println("audio: "+deviceInfo.getName().indexOf(defaultAudioDeviceName));
					//if (deviceInfo.getName().indexOf(defaultAudioDeviceName) >= 0)
					
						captureAudioDevice = deviceInfo;
						System.out.println(">>> capture audio device = " + deviceInfo.getName());
					
				}
			}

			// search for default audio format
			if (captureAudioDevice == deviceInfo)
			{
				if (captureAudioFormat == null)
				{
					if (DeviceInfo.formatToString(deviceFormat[y]).indexOf(defaultAudioFormatString) >= 0)
					{
						captureAudioFormat = (AudioFormat) deviceFormat[y];
						System.out.println(">>> capture audio format = " + DeviceInfo.formatToString(deviceFormat[y]));
					}
				}
			}
		}
	}
	//Elegir el primer formato por defecto
	//AV = new AVTransmit(MyApp,new MediaLocator("vfw://0"),Ip,tpV,fmt);
	//AV2 = new AVTransmit(MyApp,new MediaLocator("javasound://8000"),Ip,tpA,fmt);
	
	videoMediaLocator = captureVideoDevice.getLocator();
	audioMediaLocator = captureAudioDevice.getLocator();
	
	if ((captureVideoDevice!=null) && (captureAudioDevice!=null))
	{
		iniciaAudio();
		iniciaVideo();
	}
	else if((captureVideoDevice==null) &&(captureAudioDevice==null))
	{
		System.err.println("No encuentra ni webcam ni micro");
	}
	else if (captureVideoDevice==null)
		iniciaAudio();
	
	else if (captureAudioDevice==null)
		iniciaVideo();
        // result will be non-null if there was an error. The return
        // value is a String describing the possible error. Print it.
        
}
	public void stop(){
		AV.stop();
		AV2.stop();
		MyApp.DisplayMess("Stopping the Transmission...\n");
	}
	
	public void iniciaAudio()
	{
		audioMediaLocator = captureAudioDevice.getLocator();
		AV2 = new soloTransmitir(MyApp,audioMediaLocator,peerIp, miIP,tpA,recA,captureAudioFormat);
		String result2 = AV2.start();
		if (result2 != null) {
	        System.err.println("Error de audio: " + result2);
	        System.exit(0);
	    }

	}
	
	public void iniciaVideo()
	{
		videoMediaLocator = captureVideoDevice.getLocator();
		AV = new soloTransmitir(MyApp,videoMediaLocator,peerIp,miIP,tpV,recV,captureVideoFormat);
		String result = AV.start();
		
		if (result != null) {
	        System.err.println("Error de video: " + result);
	        System.exit(0);
	    }
		
	}
}