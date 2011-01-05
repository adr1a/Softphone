package Sip;
import java.util.*;
import javax.sdp.*;

public class SDP {

  SdpFactory mySdpFactory;
  InfoSDP mySdpInfo;
  byte[] mySdpContent;


  public SDP() {
    mySdpFactory = SdpFactory.getInstance();
  }

  public byte[] createSdpInvite(InfoSDP sdpinfo, ArrayList<String> listaUsers) {

    try{
    //Session-level description lines
    Version myVersion = mySdpFactory.createVersion(0);
    long ss=mySdpFactory.getNtpTime(new Date());
    Origin myOrigin = mySdpFactory.createOrigin("4855",ss,ss,"IN","IP4",sdpinfo.IpAddress);
    SessionName mySessionName = mySdpFactory.createSessionName("Sesion videolive!");
    Connection myConnection = mySdpFactory.createConnection("IN","IP4", sdpinfo.IpAddress);
    Vector atributos = new Vector<String>();
    Attribute numParticipantes;
    Attribute participante;
    if(listaUsers.size()>1)
    {
	    numParticipantes = mySdpFactory.createAttribute("Num.Participantes", Integer.toString(listaUsers.size()));
	    atributos.add(numParticipantes);
	    for(int i=0; i<listaUsers.size(); i++)
	    {
	    	String cabecera = "Participante "+i;
	    	participante = mySdpFactory.createAttribute(cabecera, listaUsers.get(i));
	    	atributos.add(participante);
	    }
    }
    //Time description lines
    Time myTime=mySdpFactory.createTime();
    Vector myTimeVector=new Vector();
    myTimeVector.add(myTime);

    //Media description lines
    int[] aaf=new int[1];
    aaf[0]=sdpinfo.aformat;

    //aaf[0]=sdpinfo.getAFormat();
    MediaDescription myAudioDescription = mySdpFactory.createMediaDescription("audio", sdpinfo.aport, 1, "RTP/AVP", aaf);
    Vector myMediaDescriptionVector=new Vector();
    myMediaDescriptionVector.add(myAudioDescription);

    if (sdpinfo.vport!=-1) {
     int[] avf=new int[1];
     avf[0]=sdpinfo.vformat;
     MediaDescription myVideoDescription = mySdpFactory.createMediaDescription("video", sdpinfo.vport, 1, "RTP/AVP", avf);
     myMediaDescriptionVector.add(myVideoDescription);
    }

    SessionDescription mySdp = mySdpFactory.createSessionDescription();

    mySdp.setVersion(myVersion);
    mySdp.setOrigin(myOrigin);
    mySdp.setSessionName(mySessionName);
    mySdp.setConnection(myConnection);
    if (listaUsers.size() > 1)
    	mySdp.setAttributes(atributos);
    mySdp.setTimeDescriptions(myTimeVector);
    //mySdp.setInfo(info);
    mySdp.setMediaDescriptions(myMediaDescriptionVector);

    mySdpContent=mySdp.toString().getBytes();

    }catch(Exception e){
      e.printStackTrace();
    }

    return mySdpContent;
  }
  public byte[] createSdpInviteVM(InfoSDP sdpinfo, String user) {

	    try{
	    //Session-level description lines
	    Version myVersion = mySdpFactory.createVersion(0);
	    long ss=mySdpFactory.getNtpTime(new Date());
	    Origin myOrigin = mySdpFactory.createOrigin("4855",ss,ss,"IN","IP4",sdpinfo.IpAddress);
	    SessionName mySessionName = mySdpFactory.createSessionName("Sesion videolive!");
	    Connection myConnection = mySdpFactory.createConnection("IN","IP4", sdpinfo.IpAddress);
	    
	    Info info = mySdpFactory.createInfo("videomensaje");
	    Vector atributos = new Vector<String>();
	    Attribute userVM;
		userVM = mySdpFactory.createAttribute("User videomensaje", user);
		atributos.add(userVM);

	    //Time description lines
	    Time myTime=mySdpFactory.createTime();
	    Vector myTimeVector=new Vector();
	    myTimeVector.add(myTime);

	    //Media description lines
	    int[] aaf=new int[1];
	    aaf[0]=sdpinfo.aformat;

	    //aaf[0]=sdpinfo.getAFormat();
	    MediaDescription myAudioDescription = mySdpFactory.createMediaDescription("audio", sdpinfo.aport, 1, "RTP/AVP", aaf);
	    Vector myMediaDescriptionVector=new Vector();
	    myMediaDescriptionVector.add(myAudioDescription);

	    if (sdpinfo.vport!=-1) {
	     int[] avf=new int[1];
	     avf[0]=sdpinfo.vformat;
	     MediaDescription myVideoDescription = mySdpFactory.createMediaDescription("video", sdpinfo.vport, 1, "RTP/AVP", avf);
	     myMediaDescriptionVector.add(myVideoDescription);
	    }

	    SessionDescription mySdp = mySdpFactory.createSessionDescription();

	    mySdp.setVersion(myVersion);
	    mySdp.setOrigin(myOrigin);
	    mySdp.setSessionName(mySessionName);
	    mySdp.setConnection(myConnection);	  
	    mySdp.setInfo(info);
	    mySdp.setAttributes(atributos);
	    mySdp.setTimeDescriptions(myTimeVector);
	    //mySdp.setInfo(info);
	    mySdp.setMediaDescriptions(myMediaDescriptionVector);

	    mySdpContent=mySdp.toString().getBytes();

	    }catch(Exception e){
	      e.printStackTrace();
	    }

	    return mySdpContent;
	  }

  public byte[] createSdp200Ok(InfoSDP sdpinfo, byte[] content) {

	  try{
	    //Session-level description lines
	    Version myVersion = mySdpFactory.createVersion(0);
	    long ss=mySdpFactory.getNtpTime(new Date());
	    Origin myOrigin = mySdpFactory.createOrigin("4855",ss,ss,"IN","IP4",sdpinfo.IpAddress);
	    SessionName mySessionName = mySdpFactory.createSessionName("Sesion videolive!");
	    Connection myConnection = mySdpFactory.createConnection("IN","IP4", sdpinfo.IpAddress);
	    
	    String s = new String(content);	    
	    SessionDescription recSdp = mySdpFactory.createSessionDescription(s);
	    InfoSDP answerInfo=this.getSdp(content);
	    
	    //Time description lines
	    Time myTime=mySdpFactory.createTime();
	    Vector myTimeVector=new Vector();
	    myTimeVector.add(myTime);

	    //Media description lines
	    int[] aaf=new int[1];
	    aaf[0]=sdpinfo.aformat;

	    //aaf[0]=sdpinfo.getAFormat();
	    MediaDescription myAudioDescription = mySdpFactory.createMediaDescription("audio", sdpinfo.aport, 1, "RTP/AVP", aaf);
	    Vector myMediaDescriptionVector=new Vector();
	    myMediaDescriptionVector.add(myAudioDescription);

	    if (sdpinfo.vport!=-1) {
	     int[] avf=new int[1];
	     avf[0]=sdpinfo.vformat;
	     MediaDescription myVideoDescription = mySdpFactory.createMediaDescription("video", sdpinfo.vport, 1, "RTP/AVP", avf);
	     myMediaDescriptionVector.add(myVideoDescription);
	    }
	    //opciones multiconferencia
	    Info multiconferencia = null;
	    Info info = null;
	    multiconferencia = recSdp.getInfo();
	    Vector<Attribute> atributos = new Vector<Attribute>();
	    
	    if (multiconferencia != null)
	    {	    	
	    	info = mySdpFactory.createInfo("multiconferencia");
	    	Attribute miPuertoAudio = mySdpFactory.createAttribute("Mi puerto audio", Integer.toString(sdpinfo.aport));
	    	Attribute miPuertoVideo = mySdpFactory.createAttribute("Mi puerto video", Integer.toString(sdpinfo.vport));
	    	atributos.add(miPuertoAudio);
	    	atributos.add(miPuertoVideo);
	    }

	    SessionDescription mySdp = mySdpFactory.createSessionDescription();

	    mySdp.setVersion(myVersion);
	    mySdp.setOrigin(myOrigin);
	    mySdp.setSessionName(mySessionName);
	    mySdp.setConnection(myConnection);
	    mySdp.setTimeDescriptions(myTimeVector);
	    
	    if (multiconferencia != null)
	    {	
	    	mySdp.setInfo(info);
	    	mySdp.setAttributes(atributos);
	    }
	    mySdp.setMediaDescriptions(myMediaDescriptionVector);

	    mySdpContent=mySdp.toString().getBytes();

	    }catch(Exception e){
	      e.printStackTrace();
	    }

	    return mySdpContent;
  }
  
  public InfoSDP getSdp(byte[] content) {
    try{
      String s = new String(content);
      SessionDescription recSdp = mySdpFactory.createSessionDescription(s);

      String myPeerIp=recSdp.getConnection().getAddress();
      String myPeerName=recSdp.getOrigin().getUsername();
      
      //opciones multillamada Tx
      String atributo = null;
      atributo = recSdp.getAttribute("Puerto Audio");
      int puertoAudio = 0;
      int puertoVideo = 0;
      if(atributo != null)
      {
    	  puertoAudio = Integer.parseInt(atributo);
    	  atributo = recSdp.getAttribute("Puerto Video");
    	  puertoVideo = Integer.parseInt(atributo);
    	  System.out.println("Tengo los puertos buenos!: " + puertoAudio + " video " + puertoVideo);
      }
      //opciones multillamada Rx
      Info info = null;
      info = recSdp.getInfo();     
      if (info != null)
      {
    	  if(info.getValue().equals("multiconferencia"))
    		  System.out.println("No voy a enviar flujo RTP pk es una multillamada!!");
    	  else if(info.getValue().equals("videomensaje"))
    		  System.out.println("es un videomensajee");
      }

      Vector recMediaDescriptionVector=recSdp.getMediaDescriptions(false);

        MediaDescription myAudioDescription = (MediaDescription) recMediaDescriptionVector.elementAt(0);
        Media myAudio = myAudioDescription.getMedia();
        int myAudioPort = myAudio.getMediaPort();
        Vector audioFormats = myAudio.getMediaFormats(false);

        int myAudioMediaFormat = Integer.parseInt(audioFormats.elementAt(0).toString());

        int myVideoPort=-1;
        int myVideoMediaFormat=-1;

        System.out.println(recMediaDescriptionVector.size());
        if (recMediaDescriptionVector.size()>1) {
        	MediaDescription myVideoDescription = (MediaDescription)
        	recMediaDescriptionVector.elementAt(1);
        	Media myVideo = myVideoDescription.getMedia();
        	myVideoPort = myVideo.getMediaPort();
        	Vector videoFormats = myVideo.getMediaFormats(false);
        	myVideoMediaFormat =  Integer.parseInt(videoFormats.elementAt(0).toString());
        }

      mySdpInfo=new InfoSDP();


    //  mySdpInfo.setIPAddress(myPeerIp);
      mySdpInfo.IpAddress=myPeerIp;
      
      if (atributo!= null){
    	  mySdpInfo.aport=puertoAudio;
    	  mySdpInfo.vport=puertoVideo;
      }
      else{
    	  mySdpInfo.aport=myAudioPort;
    	  mySdpInfo.vport=myVideoPort;
      }
      mySdpInfo.aformat=myAudioMediaFormat;  
      mySdpInfo.vformat=myVideoMediaFormat;

    }catch(Exception e){
      e.printStackTrace();
    }

    return mySdpInfo;
  }
}