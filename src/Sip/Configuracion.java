package Sip;

public class Configuracion {

	private String transport;
	private String miIP;
	private int miPuertoSip;
	private String Proxy;
	private String ProxyPuerto;
	private String miUser;
	private String miPass;
	private String miDominio;
	private String localURI;
	
	//Multimedia
	//private String IpAddress;
	private int puertoAudio;
	private int formatoAudio;
	private int puertoVideo; 
	private int formatoVideo;
	
	//Getter & Setters
	public Configuracion (String transport, String miIP, int miPuertoSip, String proxy, String proxyPuerto,
		 	String miUser, String miPass, String miDominio, int puertoAudio, 
		 	int formatoAudio, int puertoVideo, int formatoVideo, String localURI){
		this.setTransport(transport);
		this.setMiIP(miIP);
		this.setMiPuertoSip(miPuertoSip);
		this.setProxy(proxy);
		this.setProxyPuerto(proxyPuerto);
		this.setMiUser(miUser);
		this.setMiPass(miPass);
		this.setMiDominio(miDominio);
		this.setPuertoAudio(puertoAudio);
		this.setFormatoAudio(formatoAudio);
		this.setPuertoVideo(puertoVideo);
		this.setFormatoVideo(formatoVideo);
		this.setLocalURI(localURI);
	}
	public void setLocalURI(String localURI) {
		// TODO Auto-generated method stub
		this.localURI = localURI;
	}
	
	public String getLocalURI(){
		return localURI;
	}
	
	public String getTransport() {
		return transport;
	}
	public void setTransport(String transport) {
		this.transport = transport;
	}
	public String getMiIP() {
		return miIP;
	}
	public void setMiIP(String miIP) {
		this.miIP = miIP;
	}
	public int getMiPuertoSip() {
		return miPuertoSip;
	}
	public void setMiPuertoSip(int miPuertoSip) {
		this.miPuertoSip = miPuertoSip;
	}
	public String getProxy() {
		return Proxy;
	}
	public void setProxy(String proxy) {
		Proxy = proxy;
	}
	public String getProxyPuerto() {
		return ProxyPuerto;
	}
	public void setProxyPuerto(String proxyPuerto) {
		ProxyPuerto = proxyPuerto;
	}
	public String getMiUser() {
		return miUser;
	}
	public void setMiUser(String miUser) {
		this.miUser = miUser;
	}
	public String getMiPass() {
		return miPass;
	}
	public void setMiPass(String miPass) {
		this.miPass = miPass;
	}
	public String getMiDominio() {
		return miDominio;
	}
	public void setMiDominio(String miDominio) {
		this.miDominio = miDominio;
	}
	public int getPuertoAudio() {
		return puertoAudio;
	}
	public void setPuertoAudio(int puertoAudio) {
		this.puertoAudio = puertoAudio;
	}
	public int getFormatoAudio() {
		return formatoAudio;
	}
	public void setFormatoAudio(int formatoAudio) {
		this.formatoAudio = formatoAudio;
	}
	public int getPuertoVideo() {
		return puertoVideo;
	}
	public void setPuertoVideo(int puertoVideo) {
		this.puertoVideo = puertoVideo;
	}
	public int getFormatoVideo() {
		return formatoVideo;
	}
	public void setFormatoVideo(int formatoVideo) {
		this.formatoVideo = formatoVideo;
	}

	/*public Configuracion() {
	   
	 IpAddress="";  
	 aport=0;  
	 aformat=0;
	 vport=0; 
	 vformat=0;
}*/
}