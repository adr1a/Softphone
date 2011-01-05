package Sip;
import gov.nist.javax.sip.header.ProxyAuthenticate;
import gov.nist.javax.sip.header.SIPHeaderNames;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.sdp.*;
import javax.sip.*;
import javax.sip.Dialog;
import javax.sip.address.*;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.Iterator;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;

import Multimedia.*;
import InterfazGrafica.*;
import Presencia.*;



//cambio
public class Softphone implements SipListener {

	private static SipProvider tlsProvider, sipProvider;
	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;
	private int reInviteCount;
	public CallIdHeader callIdHeader;
	private ContactHeader contactHeader; 
	private UserAgentHeader userAgentHeader;
	private ListeningPoint tlsListeningPoint;
	private int terminatedCount;
	private Dialog dialog;
	private Response okResponse;
	private Request inviteRequest;
	long numSeq = 1;
	String realm = "147.83.115.200";
	String callIdAuth = null;
	Softphone listener;
	protected ClientTransaction registerTid;
	protected ClientTransaction inviteTid;
	protected ServerTransaction inviteTid2;
	protected ServerTransaction optionsTid;
	
	public ArrayList<String> listaUsers = null;
	
	//SDP
	private InfoSDP answerInfo;
	private InfoSDP offerInfo;
	private SDP mySdpManager;

	protected static final String usageString ="ant tlsshootist \n";
	private Configuracion miConfig = null; 
	//autenticacion
	boolean autenticado=false;
	boolean esperandoRegister=true;
	int contRegister = 0; 
	
	//llamada
	boolean disponible = false;
	boolean esperandoInvite = true;

	//videomensaje
	boolean enviarVM= false;
	
	private Display display= null;
	private Shell shell= null;
	
	public Text prioritat;
	Text estat;
	public Text subjecte;
	Text caller;
	Text pad;
	Text seleccionados;
	public Button rcv;
	public Button tx;
	public Button tancar;
	public Combo id;
	public List list = null;
	Button borrarSeleccionados;
	
	//presencia
	ListaContactos listaContactos = new ListaContactos();
	ArrayList <Contacto> lista = null;
	private int puertoProxy;
	Contacto contactoLocal = null;
	Text nombre;
	private XmlParser xmlParser;
	private String sipEtagGlobal = null; 
	private boolean enviarSubcribe3 = false;
	private boolean enviarRegisterDes = false;
	
	  JavaPhoneApp sesion = null;
	
	public Softphone(Configuracion miConfig)throws ParseException, UnknownHostException, TooManyListenersException{		
		super();
		listaUsers = new ArrayList<String>();
		this.miConfig = miConfig;		
		sesion = new JavaPhoneApp();
	}
	public void setNomUser(String miUser){
		miConfig.setMiUser(miUser);
	}
	public void setPass(String miPass){
		miConfig.setMiPass(miPass);
	}
	public void setDomain(String miDominio){
		miConfig.setMiDominio(miDominio);
	}
	
	public Shell getShell(){
		return shell;
	}
	private void initialize(){
		display= new Display();
		shell= new Shell(display, SWT.SHELL_TRIM);//podem posar lestil que volguem
		shell.setSize(400,630);
		
		shell.setText("Istados Videolive!");//posar títol
		Color negre = display.getSystemColor (SWT.COLOR_BLACK);
		Color blanc = display.getSystemColor (SWT.COLOR_WHITE);
		Color vermell=display.getSystemColor (SWT.COLOR_RED);
		Color blau=display.getSystemColor (SWT.COLOR_DARK_BLUE);
		final Color verd=display.getSystemColor (SWT.COLOR_DARK_GREEN);
		Color x=display.getSystemColor (SWT.COLOR_WIDGET_DARK_SHADOW);
		shell.setBackground(x);
		GridLayout gl = new GridLayout();
		shell.setLayout(gl);
		gl.numColumns = 4;
		gl.makeColumnsEqualWidth = false;

		Group grup1= new Group(shell,SWT.NONE);
		grup1.setBackground(x);
		
		
		GridLayout gl1 = new GridLayout();
		gl1.numColumns = 1;
		grup1.setLayout(gl1);
		
		GridData g11 = new GridData();
		g11.horizontalSpan = 1;
		g11.grabExcessHorizontalSpace = true;
		g11.horizontalAlignment = SWT.FILL;
		g11.grabExcessVerticalSpace = true;
		g11.verticalAlignment = SWT.FILL;
		grup1.setLayoutData(g11);
	
		Font fontUser = new Font(display,"Arial", 14, SWT.BOLD );
		Font fontLlamar = new Font(display,"Arial", 12, SWT.NONE );
		Font fontAmigos = new Font(display,"Arial", 12, SWT.NONE );
		
		Font fontTitulos = new Font(display,"Arial", 12, SWT.UNDERLINE_DOUBLE );

		GridData gd1 = new GridData();
		gd1.horizontalAlignment = SWT.CENTER;
		gd1.grabExcessHorizontalSpace = true;
		gd1.verticalAlignment = SWT.CENTER;
		//gd1.grabExcessVerticalSpace = true;
		
		
		Label estatl=new Label(grup1,0);
		estatl.setBounds(10, 75, 100, 15);
		estatl.setText("Usuario: " + miConfig.getMiUser());
		estatl.setLayoutData(gd1);
		estatl.setBackground(x);
		estatl.setFont(fontUser);
		
		id= new Combo(grup1,SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		
		id.add("Conectado");
		id.add("No disponible");
		id.add("Ausente");
		id.select(0);
		id.setLayoutData(gd1);
		
		GridData gd2 = new GridData();
		gd2.horizontalAlignment = SWT.LEFT;
		gd2.grabExcessHorizontalSpace = true;
		gd2.verticalAlignment = SWT.CENTER;
		
		Label label1=new Label(grup1,0);
		label1.setText("Llamar a:");
		label1.setFont(fontTitulos);	
		label1.setBackground(x);
		label1.setLayoutData(gd2);
		
		GridData gdt = new GridData();
		gdt.horizontalAlignment = SWT.FILL;
		gdt.grabExcessHorizontalSpace = true;
		gdt.verticalAlignment = SWT.CENTER;
		
		Group grup11= new Group(grup1,SWT.NONE);
		grup11.setBackground(x);
		GridLayout gl11 = new GridLayout();
		gl11.numColumns = 4;
		grup11.setLayout(gl11);
		grup11.setLayoutData(gdt);
		
		GridData gd3 = new GridData();
		gd3.horizontalAlignment = SWT.FILL;
		gd3.grabExcessHorizontalSpace = true;
		gd3.verticalAlignment = SWT.CENTER;
		
		estat=new Text(grup11, SWT.MULTI);
		estat.setForeground(verd);
		estat.setFont(fontLlamar);		
		estat.setLayoutData(gd3);
		
		GridData gdb = new GridData();

		gdb.horizontalAlignment = SWT.RIGHT;
		gdb.horizontalSpan = 3;
		gdb.verticalSpan = 1;

		Button boton1 = new Button(grup11, SWT.PUSH | SWT.CENTER);
		boton1.setText("Llamar");
		boton1.setLayoutData(gdb);


		boton1.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {		
				if(estat.getText()!="")
				{
					listaUsers.add(estat.getText());
					Invite(listaUsers);	
					while (esperandoInvite)
					{
						try {
			    			Thread.sleep (1000);
			    		} catch (Exception e2) {
			    		}
					}
					if(disponible){
						disponible = false;
						esperandoInvite=true;
						//Bye(listaUsers.get(0));
					}					
					else{
						disponible = false;
						esperandoInvite=true;
						display.dispose();
						abrirVideomensaje();
						if (enviarVM == true){
							InviteVM(listaUsers.get(0));
						}
						listaUsers.clear();
						enviarVM=false;
						initialize();
						try {
							execute();
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					
					}
				}
				else
				{
					MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					messageBox.setText("Error");
					messageBox.setMessage("Debe introducir un nombre de usuario");
					messageBox.open();
				}
					
			}
		});	
		
		GridData gd4 = new GridData();
		gd4.horizontalAlignment = SWT.LEFT;
		gd4.grabExcessHorizontalSpace = true;
		gd4.verticalAlignment = SWT.CENTER;
		
		Label label2=new Label(grup1,0);
		label2.setText("Amigos:");
		label2.setFont(fontTitulos);
		label2.setBackground(x);
		label2.setLayoutData(gd4);
		
		Group grup2= new Group(grup1,SWT.NONE);
		grup2.setBackground(x);
		//grup2.setSize(100, 100);
			
		GridLayout gl2 = new GridLayout();
		gl2.numColumns = 1;
		grup2.setLayout(gl2);
		
		GridData g12 = new GridData();
		//g12.horizontalSpan = 1;
		//g12.grabExcessHorizontalSpace = true;
		//g12.horizontalAlignment = SWT.FILL;
		//g12.grabExcessVerticalSpace = true;
		//g12.verticalAlignment = SWT.FILL;
		grup2.setLayoutData(g12);
		
		//GridData gl3 = new GridData();
		//gl3.horizontalAlignment = SWT.CENTER;
		//gl3.minimumHeight = 100;
		//gl3.minimumWidth = 100;
		//gl3.horizontalIndent = 200;  //POSICION
		//gl3.verticalIndent = 200;  //POSICION
		//gl3.horizontalSpan = 200;
		//gl3.verticalSpan = 100;
		
	    list = new List(grup2, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI );
	    
		//list.setLayoutData(gl3);
		list.setBounds(10, 100, 200, 100);
		
		//Primero cargar el xml de mi lista de contactos local
		//lista = listaContactos.getContacts();
		lista = listaContactos.cargarLista();
		
		for (int i = 0; i < lista.size(); i++)
		{
			Contacto a = lista.get(i);
			list.add(a.getNombre()+ "  ("+a.getEstado()+")");
			Subscribe(a.getNombre(),1); //si agrego a un contacto
		}
		
		GridData gl4 = new GridData();
		gl4.horizontalAlignment = SWT.CENTER;
		gl4.horizontalSpan = SWT.FILL;
		gl4.verticalSpan = SWT.FILL;
		
	    seleccionados = new Text(grup2, SWT.BORDER);
	    seleccionados.setLayoutData(gl4);
		
		list.addSelectionListener(new SelectionListener() {
	    	
	    	public void widgetSelected(SelectionEvent event) {	    				
	    		listaUsers.clear();	    				
	    		String[] sp, selectedItems = list.getSelection();	    				
	    		for (String item : selectedItems){	    					
	    			sp=item.split(" "); //coge el texto hasta llegar al espacio	    					
	    			listaUsers.add(sp[0]);	    				
	    		}	    				
	    		seleccionados.setText("User seleccionado/s: "+listaUsers);
	    		
	    	}	    			
	    	public void widgetDefaultSelected(SelectionEvent event) {}	    			
	    }	    		
	    );
		
		 id.addSelectionListener(new SelectionAdapter() {
		       public void widgetSelected(SelectionEvent e) {
		          if (id.getText().equals("Conectado")){
		        	  contactoLocal.setEstado("Online");
		      		  Publish(miConfig.getLocalURI(),contactoLocal.getEstado(), miConfig.getMiUser(),1);
		          }
		          else if (id.getText().equals("No disponible"))
		          {
		        	  contactoLocal.setEstado("Busy (DND)");
		      		  Publish(miConfig.getLocalURI(),contactoLocal.getEstado(), miConfig.getMiUser(),1);
		          }
		          else if (id.getText().equals("Ausente")){
		        	  contactoLocal.setEstado("Away");
		      		  Publish(miConfig.getLocalURI(),contactoLocal.getEstado(), miConfig.getMiUser(),1);
		          }
		        }
		      });
		
		
		/*GridData gl4 = new GridData();
		gl4.horizontalAlignment = SWT.FILL;
		gl4.grabExcessHorizontalSpace = true;
		gl4.grabExcessVerticalSpace = true;
		gl4.verticalAlignment = SWT.CENTER;
		
		Button estado1=new Button(grup2,SWT.CHECK);
		estado1.setText("Maria");
		estado1.setFont(fontAmigos);
		estado1.setBackground(x);
		GridData g13 = new GridData();
		g13.horizontalAlignment=SWT.LEFT;
		estado1.setLayoutData(g13);	
		
		Label label3=new Label(grup2,0);
		label3.setText("(conectado)");
		label3.setForeground(verd);
		label3.setBackground(x);
		label3.setLayoutData(gl4);
		
		Button estado2=new Button(grup2,SWT.CHECK);
		estado2.setText("Albert");
		estado2.setFont(fontAmigos);
		estado2.setBackground(x);
		estado2.setLayoutData(g13);	
		
		Label label4=new Label(grup2,0);
		label4.setText("(desconectado)");
		label4.setForeground(vermell);
		label4.setBackground(x);
		label4.setLayoutData(gl4);
		
		Button estado3=new Button(grup2,SWT.CHECK);
		estado3.setText("Marc");
		estado3.setFont(fontAmigos);
		estado3.setBackground(x);
		estado3.setLayoutData(g13);	
		
		Label label5=new Label(grup2,0);
		label5.setText("(ausente)");
		label5.setForeground(blau);
		label5.setBackground(x);
		label5.setLayoutData(gl4);
		
		Button estado4=new Button(grup2,SWT.CHECK);
		estado4.setText("Jordi");
		estado4.setFont(fontAmigos);
		estado4.setBackground(x);
		estado4.setLayoutData(g13);	
		
		Label label6=new Label(grup2,0);
		label6.setText("(conectado)");
		label6.setForeground(verd);
		label6.setBackground(x);
		label6.setLayoutData(gl4);*/
		
		GridData gdc = new GridData();
		gdc.horizontalAlignment = SWT.CENTER;
		gdc.horizontalSpan = 3;
		gdc.verticalSpan = 1;
		
		Button boton2 = new Button(grup1, SWT.PUSH | SWT.CENTER);
		boton2.setText("Llamar seleccionados");
		boton2.setLayoutData(gdc);


		boton2.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				
				if (listaUsers.size()>0)
					Invite(listaUsers);
				else{
					MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					messageBox.setText("Error");
					messageBox.setMessage("Debe seleccionar almenos un usuario");
					messageBox.open();
				}
			}
		});	
		
		Button boton3 = new Button(grup1, SWT.PUSH | SWT.CENTER);
		boton3.setText("mandar bye");
		boton3.setLayoutData(gdc);


		boton3.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				
				Bye("adria");
			}
		});	
		
		nombre=new Text(grup1, SWT.MULTI);
		nombre.setForeground(verd);
		nombre.setFont(fontLlamar);		
		nombre.setLayoutData(gdc);
		Button boton4 = new Button(grup1, SWT.PUSH | SWT.CENTER);
		boton4.setText("Agregar Contacto");
		boton4.setLayoutData(gdc);


		boton4.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if(nombre.getText()!="")
				{
					listaUsers.add(nombre.getText());
					agregarContacto("Offline",nombre.getText(),nombre.getText()+"@"+miConfig.getProxy());
					//agregarContacto("offline","adria","adria@147.83.115.200");
				}
				else
				{
					MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					messageBox.setText("Error");
					messageBox.setMessage("Debe introducir un nombre de usuario");
					messageBox.open();
				}
			}
		});	
		
		Button boton5 = new Button(grup1, SWT.PUSH | SWT.CENTER);
		boton5.setText("Elimina Contacto");
		boton5.setLayoutData(gdc);


		boton5.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (listaUsers.size()==1)
					eliminaContacto(lista.get(list.getSelectionIndex()));
				else{
					MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					messageBox.setText("Error");
					messageBox.setMessage("Debe seleccionar solo 1 usuario");
					messageBox.open();
				}
				
			}
		});	
		
		
	}

	
	private static void usage() {
		System.out.println(usageString);
		System.exit(0);
	}
	
	class MyTimerTask extends TimerTask {
        Softphone softphone;

        public MyTimerTask(Softphone softphone) {
            this.softphone = softphone;

        }

        public void run() {
        	softphone.sendInviteOK();
        }

    }
	public void processRequest(RequestEvent requestEvent) {
		
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId =requestEvent.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod() + " received at "
				+ sipStack.getStackName() + " with server transaction id " + serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);
		else if (request.getMethod().equals(Request.INVITE))
			processInvite(requestEvent, serverTransactionId);
		else if (request.getMethod().equals(Request.OPTIONS))
			processOptions(request, serverTransactionId);
		else if (request.getMethod().equals(Request.NOTIFY))
			processNotify(requestEvent, serverTransactionId);
	}
	
	public void processBye(Request request, ServerTransaction serverTransactionId) {
		try {
			if (serverTransactionId == null) {
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			Response response = messageFactory.createResponse(Response.OK, request);
			serverTransactionId.sendResponse(response);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            Response response = messageFactory.createResponse(Response.RINGING, request);
            ServerTransaction st = requestEvent.getServerTransaction();
            
            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            dialog = st.getDialog();

            st.sendResponse(response);
            
            mySdpManager=new SDP();
            byte[] cont=(byte[]) request.getContent();  	    
            answerInfo=mySdpManager.getSdp(cont); 	    
            
            this.okResponse = messageFactory.createResponse(Response.OK,request);
            Address address = addressFactory.createAddress(miConfig.getMiUser() + " <sip:"+ miConfig.getMiIP() + ":" + miConfig.getMiPuertoSip() + ">");
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            FromHeader fromHeader = (FromHeader) okResponse.getHeader(FromHeader.NAME);
            String userLlamante = fromHeader.getAddress().getDisplayName();
			
            Llamada llamada = new Llamada(userLlamante);
			llamada.execute();
				
			if(llamada.llamadaAceptada){        
            	   
				ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);	         
	            toHeader.setTag("4321"); // Application is supposed to set.
	            okResponse.addHeader(contactHeader);
	            
	            //Add the user agent
				ArrayList<String> Agentes = new ArrayList<String>();
				Agentes.add("Istados videolive!");
				userAgentHeader = headerFactory.createUserAgentHeader(Agentes);
				okResponse.addHeader(userAgentHeader);
	
				offerInfo=new InfoSDP();
		        offerInfo.IpAddress=miConfig.getMiIP();
		        offerInfo.aport=miConfig.getPuertoAudio();
		        offerInfo.aformat=miConfig.getFormatoAudio();
		        offerInfo.vport=miConfig.getPuertoVideo();
		        offerInfo.vformat=miConfig.getFormatoVideo();
	
	
		        ContentTypeHeader contentTypeHeader=headerFactory.createContentTypeHeader("application","sdp");
		        mySdpManager=new SDP();
		        byte[] content = mySdpManager.createSdp200Ok(offerInfo, cont);
		        okResponse.setContent(content,contentTypeHeader);
			}
			else{
				this.okResponse = messageFactory.createResponse(Response.TEMPORARILY_UNAVAILABLE,request);
				ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);	 
				toHeader.setTag("4321"); // Application is supposed to set.
	            okResponse.addHeader(contactHeader);
	            
	            //Add the user agent
				ArrayList<String> Agentes = new ArrayList<String>();
				Agentes.add("Istados videolive!");
				userAgentHeader = headerFactory.createUserAgentHeader(Agentes);
				okResponse.addHeader(userAgentHeader);
			}
			
            this.inviteTid2 = st;
            // Defer sending the OK to simulate the phone ringing.
            // Answered in 1 second ( this guy is fast at taking calls)
            this.inviteRequest = request;
            new Timer().schedule(new MyTimerTask(this), 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
	
	private void sendInviteOK() {
        try {
            if (inviteTid2.getState() != TransactionState.COMPLETED) {
                System.out.println("sala: Dialog state before 200: " + inviteTid2.getDialog().getState());
                inviteTid2.sendResponse(okResponse);
                System.out.println("sala: Dialog state after 200: "+ inviteTid2.getDialog().getState());
                //JavaPhoneApp sesion = new JavaPhoneApp();
                sesion.startMedia(miConfig.getMiIP(), miConfig.getProxy(),answerInfo.aport, answerInfo.vport, miConfig.getPuertoAudio(),
               		miConfig.getPuertoVideo());
            }
        } catch (SipException ex) {
            ex.printStackTrace();
        } catch (InvalidArgumentException ex) {
            ex.printStackTrace();
        }
    }
	
	public void processOptions(Request request, ServerTransaction serverTransactionId) {
		try {
			System.out.println("Sala:  got a OPTIONS");
			Response response = messageFactory.createResponse(200, request);
			if (serverTransactionId == null) {
				System.out.println("Sala:  null TID.");
				optionsTid = sipProvider.getNewServerTransaction(request);
//				return;
			}
//			Dialog dialog = serverTransactionId.getDialog();
//			System.out.println("Dialog State = " + dialog.getState());

			optionsTid.sendResponse(response);
			System.out.println("Sala:  Sending Response OK.");
//			System.out.println("Dialog State = " + dialog.getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	public void processNotify(RequestEvent requestEvent, ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("Got a Notifiy");
			ServerTransaction st = requestEvent.getServerTransaction();
			byte[] cont = (byte[]) request.getContent();
			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			dialog = st.getDialog();

			this.inviteTid2 = st;

			this.inviteRequest = request;

			this.okResponse = messageFactory.createResponse(Response.OK,
					request);
			Address address = addressFactory.createAddress(miConfig.getMiUser()
					+ " <sip:" + miConfig.getMiIP() + ":"
					+ miConfig.getMiPuertoSip() + ">");
			ContactHeader contactHeader = headerFactory.createContactHeader(address);
			// response.addHeader(contactHeader);
			ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			okResponse.addHeader(contactHeader);

			// Add the user agent
			ArrayList<String> Agentes = new ArrayList<String>();
			Agentes.add("Istados videolive!");
			userAgentHeader = headerFactory.createUserAgentHeader(Agentes);
			okResponse.addHeader(userAgentHeader);
			// st.sendResponse(okResponse);

			
			Header contentLength = request.getHeader("Content-Length");
			String stringContentLength = contentLength.toString();	
			String []substring = stringContentLength.split(" ");
			stringContentLength = substring[1].trim();
			
			substring [1] = substring[1].replaceAll("[\n\r]", ""); //quitar el salto de linia
			if (substring[1].equals("0"))
				System.out.println("No hay usuarios conectados");
			else{
			//Voy a coger la cabecera contentTypeHeader, si es: application/watcherinfo+xml --> inicio de sesion
			// si es application/pidf+xml
			ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request
				.getHeader(ContentTypeHeader.NAME);
			System.out.println("ContentTypeHeader: "+contentTypeHeader);
			String xmlType = contentTypeHeader.getContentSubType();	
			System.out.println("xmlType: "+xmlType);
			
			Object content=request.getContent();
            String text=null;
            //text = (String) content;
            if (content instanceof String) text=(String)content;
            else  if (content instanceof byte[] ) text=new String(  (byte[])content  );
            else {
    		    System.out.println("DEBUG, IMNotifyProcessing, process(), "+
    			       " Error, the body of the request is unknown!!");
    		    System.out.println("ERROR, IMNotifyProcessing, process(): "+
    			       " pb with the xml body, 488 Not Acceptable Here replied");
    		    Response response=messageFactory.createResponse
    			(Response.NOT_ACCEPTABLE,request);
    		    serverTransaction.sendResponse(response);
    		    return;
	    		}
				
	            text = text.trim(); //li trec espais inicials o finals
	            System.out.println(text); //aqui tinc tot el <?xml ... --> vull extreure: presence>tuple>note 
	            //xml.hazParse(text.tr);
	            
	            xmlParser=new XmlParser();
	            
	            //if (xmlType.equals("watcherinfo+xml")){ //inicio de sesion
	            
				if (xmlType.equals("pidf+xml")){ //al agregar contacto, o contacto cambia de estado
					String presencia = XmlParser.tratarXML(text.trim());
					
					System.out.println("Status:" + presencia);
					
					// We have to update the buddy list!!!
					
					//COGER DEL FROM EL NOMBRE
					FromHeader from = (FromHeader) request.getHeader(FromHeader.NAME);
					
					String stringFrom = from.getAddress().toString();
					String []substringFrom = stringFrom.split(":");
					substringFrom = substringFrom[1].split("@");
					String nombre = substringFrom[0];
					
					int index = listaContactos.seleccionarContacto(nombre);
					lista.get(index).setEstado(presencia);
					
					//pintar contactos
					display.syncExec(
							  new Runnable() {
							    public void run(){
									list.removeAll();
									lista = listaContactos.getContacts();
									for (int i = 0; i < lista.size(); i++)
									{
										Contacto a = lista.get(i);
										list.add(a.getNombre()+ "  ("+a.getEstado()+")");
									}
							    }
							  });
				}
			}
			// Send an OK
			Response response = messageFactory.createResponse(
					Response.OK, request);
			serverTransaction.sendResponse(response);
			System.out.println("OK replied to the NOTIFY");
		}
		 catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		
	}
	
	private Request ackRequest;
	
	public void processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        ClientTransaction tid = responseReceivedEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        //System.out.println("Response received : Status Code = " + response.getStatusCode() + " " + cseq);

        if (tid == null) {

            // RFC3261: MUST respond to every 2xx
            if (ackRequest!=null && dialog!=null) {
               System.out.println("re-sending ACK");
               try {
                  dialog.sendAck(ackRequest);
               } catch (SipException se) {
                  se.printStackTrace();
               }
            }
            return;
        }

        try {
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE)) {
                    System.out.println("Dialog after 200 OK  " + dialog);
                    System.out.println("Dialog State after 200 OK  " + dialog.getState()); 
                    this.process200OK(response);
                    ackRequest = dialog.createAck( ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getSeqNumber() );         
                    System.out.println("Sending ACK");
                    dialog.sendAck(ackRequest);                    
                    System.out.println("IP cogida en getsdp: " + answerInfo.IpAddress);
                    //JavaPhoneApp sesion = new JavaPhoneApp();
                    sesion.startMedia(miConfig.getMiIP(), miConfig.getProxy(),answerInfo.aport, answerInfo.vport, miConfig.getPuertoAudio(),
                    		miConfig.getPuertoVideo());

                    esperandoInvite=false;
                    disponible=true;
                    
                } else if(cseq.getMethod().equals(Request.REGISTER)) {	
                	autenticado=true;
                	esperandoRegister=false;
                	contRegister = 0; 

                } else if (cseq.getMethod().equals(Request.CANCEL)) {
                    if (dialog.getState() == DialogState.CONFIRMED) {
                        // oops cancel went in too late. Need to hang up the
                        // dialog.
                        System.out.println("Sending BYE -- cancel went in too late !!");
                        Request byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
                        dialog.sendRequest(ct);
                    }
                }
                else if (cseq.getMethod().equals(Request.PUBLISH)){
					//Cojo sipetag para mandar publish de desconexion
					Header sipEtag = response.getHeader("SIP-ETag");
					String sipetag = sipEtag.toString();

					String []substring = sipetag.split(" ");
					sipEtagGlobal = substring[1];
					
					//Para mandar los Subscribe de desconexion despues d recibir publish d desconexion
					Header expires = response.getHeader("Expires");
					String stringExpires = expires.toString();	
					substring = stringExpires.split(" ");
					
					substring [1] = substring[1].replaceAll("[\n\r]", ""); //quitar el salto de linia
					
					if (substring[1].equals("0"))
						enviarSubcribe3 = true;
				}
            }else if (response.getStatusCode() == Response.NOT_FOUND 
            		|| response.getStatusCode()== Response.REQUEST_TIMEOUT
            		|| response.getStatusCode()== Response.TEMPORARILY_UNAVAILABLE){
            	
            	esperandoInvite=false;
                disponible=false;
             
            	
            }
            else if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
                    || response.getStatusCode() == Response.UNAUTHORIZED) {
                
            	if(contRegister <5)
            	{
	            	contRegister++;
            		//URI uriReq = tid.getRequest().getRequestURI();
	                URI uriReq = addressFactory.createURI("sip:" + miConfig.getProxy());
	                
					
	               // WWWAuthenticateHeader wwwAuthHeader = (WWWAuthenticateHeader)response.getHeader("WWW-Authenticate");
	                
	                Request authrequest = this.processResponseAuthorization(response, uriReq);
	                inviteTid = sipProvider.getNewClientTransaction(authrequest);
	                inviteTid.sendRequest();
	                System.out.println("INVITE AUTHORIZATION sent:\n" + authrequest);
	                numSeq++;
            	}
            	else{
            		autenticado=false;
            		esperandoRegister=false;
            		contRegister=0;
            	}
            		
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
	}
	
	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out" );
	}
	
	public Request processResponseAuthorization(Response response, URI uriReq) {
        Request requestauth = null;
        try {
            System.out.println("processResponseAuthorization()");     
        
             WWWAuthenticateHeader wwwAuthHeader = (WWWAuthenticateHeader)response.getHeader("WWW-Authenticate");
             String schema = wwwAuthHeader.getScheme();
             
             String nonce = wwwAuthHeader.getNonce();
            DigestClientAuthenticationMethod digest = new DigestClientAuthenticationMethod();

            this.callIdAuth = ((CallIdHeader) response.getHeader(CallIdHeader.NAME)).getCallId();
            requestauth = this.RegisterAuth();
            
            //cambiado uriReq.toString() x 192.168.1.200
            digest.initialize(realm, miConfig.getMiUser(), uriReq.toString(), nonce,miConfig.getMiPass(),
                    ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod(), null, "MD5");
            
            String respuestaM = digest.generateResponse();

            AuthorizationHeader authorizationHeader = headerFactory.createAuthorizationHeader(schema);
            authorizationHeader.setUsername(miConfig.getMiUser());
            authorizationHeader.setRealm(realm);
            authorizationHeader.setNonce(nonce);
            authorizationHeader.setURI(uriReq);
            authorizationHeader.setResponse(respuestaM);
            authorizationHeader.setAlgorithm("MD5");
            requestauth.addHeader(authorizationHeader);
            
            
            
        } catch (ParseException pa) {
            System.out
                    .println("processResponseAuthorization() ParseException:");
            System.out.println(pa.getMessage());
            pa.printStackTrace();
        } catch (Exception ex) {
            System.out.println("processResponseAuthorization() Exception:");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return requestauth;
    }
	
	public void process200OK(Response response) {
	    
        byte[] cont=(byte[]) response.getContent();  	    
        answerInfo=mySdpManager.getSdp(cont);
	}
	
	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();

		properties.setProperty("javax.sip.IP_ADDRESS", miConfig.getMiIP()); 
		properties.setProperty("javax.sip.OUTBOUND_PROXY", miConfig.getProxyPuerto() + "/"+ miConfig.getTransport());
		properties.setProperty("javax.sip.STACK_NAME", miConfig.getMiUser());
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "on");

		// The following properties are specific to nist-sip
		// and are not necessarily part of any other jain-sip
		// implementation.
		// You can set a max message size for tcp transport to 
		// guard against denial of service attack.
		properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "saladebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "salalog.txt");

		// Drop the client connection after we are done with the transaction.
		properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
		// Set to 0 in your production code for max speed.
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("createSipStack " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			listener = this;

			tlsListeningPoint = sipStack.createListeningPoint(miConfig.getMiIP(), miConfig.getMiPuertoSip(), miConfig.getTransport()); 
			tlsProvider = sipStack.createSipProvider(tlsListeningPoint);
			System.out.println("udp provider " + tlsProvider);
			tlsProvider.addSipListener(listener);
			sipProvider = tlsProvider;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}
	
	public void Register() {
		try {
			String fromName = miConfig.getMiUser();  
			String fromSipAddress = miConfig.getMiDominio(); //"here.com";
			String fromDisplayName = miConfig.getMiUser();

			String toSipAddress = miConfig.getMiDominio(); //"there.com";
			String toUser = miConfig.getMiUser();
			String toDisplayName = miConfig.getMiUser();

			// create >From Header
			SipURI fromAddress =addressFactory.createSipURI(fromName, fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader =headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress =addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader =headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI =
				addressFactory.createSipURI(null, miConfig.getProxyPuerto());

			// Create ViaHeaders
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			//int port = sipProvider.getListeningPoint().getPort();
			ViaHeader viaHeader =
				headerFactory.createViaHeader(sipStack.getIPAddress(),sipProvider.getListeningPoint(miConfig.getTransport()).getPort(),miConfig.getTransport(),null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();

			// Create contact headers
			String host = sipStack.getIPAddress();
			
			// Create the contact name address.
			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tlsListeningPoint.getPort());
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(miConfig.getTransport()).getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			contactHeader = headerFactory.createContactHeader(contactAddress);
				
			CSeqHeader cSeqHeader =headerFactory.createCSeqHeader(numSeq, Request.REGISTER);

			//Add the user agent
			ArrayList<String> Agentes = new ArrayList<String>();
			Agentes.add("Istados videolive!");
			userAgentHeader = headerFactory.createUserAgentHeader(Agentes);
			
			// Create the request.
			Request request =
				messageFactory.createRequest(requestURI, Request.REGISTER, callIdHeader,
					cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			
			request.addHeader(contactHeader);
			request.addHeader(userAgentHeader);
			
			if (enviarRegisterDes){//Registro de desconexion	
				//Expires Header
				Header expiresHeader = headerFactory.createExpiresHeader(0);
				request.setHeader(expiresHeader);
			}
			
			// Create the client transaction.
			listener.registerTid = sipProvider.getNewClientTransaction(request);
			//ClientTransaction registerTid = sipProvider.getNewClientTransaction(request);
			
			// send the request out.
			listener.registerTid.sendRequest();
			//registerTid.sendRequest();

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}
	
	//register con autenticacion
	public Request RegisterAuth() {
		Request request = null;
		try {
			String fromName = miConfig.getMiUser();  
			String fromSipAddress = miConfig.getMiDominio(); //"here.com";
			String fromDisplayName = miConfig.getMiUser();

			String toSipAddress = miConfig.getMiDominio(); //"there.com";
			String toUser = miConfig.getMiUser();
			String toDisplayName = miConfig.getMiUser();

			// create >From Header
			SipURI fromAddress =addressFactory.createSipURI(fromName, fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader =headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress =addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader =headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI =
				addressFactory.createSipURI(null, miConfig.getProxyPuerto());

			// Create ViaHeaders
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			//int port = sipProvider.getListeningPoint(this.getTransport()).getPort();
			ViaHeader viaHeader =
				headerFactory.createViaHeader(sipStack.getIPAddress(),sipProvider.getListeningPoint(miConfig.getTransport()).getPort(), miConfig.getTransport(),null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();
			if (this.callIdAuth.trim().length() > 0)
		            callIdHeader.setCallId(this.callIdAuth);

			// Create contact headers
			String host = sipStack.getIPAddress();
			
			// Create the contact name address.
			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tlsListeningPoint.getPort());
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(miConfig.getTransport()).getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);
			
			contactHeader = headerFactory.createContactHeader(contactAddress);
			
			//Add the user agent
			ArrayList<String> Agentes = new ArrayList<String>();
			Agentes.add("Istados videolive!");
			userAgentHeader = headerFactory.createUserAgentHeader(Agentes);
			
			
			CSeqHeader cSeqHeader =headerFactory.createCSeqHeader(numSeq, Request.REGISTER);

			// Create the request.
			request =
				messageFactory.createRequest(requestURI, Request.REGISTER, callIdHeader,
					cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
			
			request.addHeader(contactHeader);
			request.addHeader(userAgentHeader);
			if (enviarRegisterDes){//Registro de desconexion	
				//Expires Header
				Header expiresHeader = headerFactory.createExpiresHeader(0);
				request.setHeader(expiresHeader);
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
		return request;
	}
	
	public void Invite(ArrayList<String> listaUsers) {
		Softphone listener = this;
		String suUser;
		try {
			String fromName = miConfig.getMiUser();
			String fromSipAddress = miConfig.getMiDominio(); //"here.com";
			String fromDisplayName = miConfig.getMiUser();
			
			if(listaUsers.size()==1)
				suUser=listaUsers.get(0);
			else
				suUser="sala";

			String toSipAddress = miConfig.getMiDominio(); //"there.com";
			String toUser = suUser; //"20002";
			String toDisplayName = suUser;

			// create >From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader =
				headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress =
				addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader =
				headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI =
				addressFactory.createSipURI(toUser, miConfig.getProxyPuerto());

			// Create ViaHeaders
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			//int port = sipProvider.getListeningPoint(this.getTransport()).getPort();
			ViaHeader viaHeader = headerFactory.createViaHeader(sipStack.getIPAddress(),
					sipProvider.getListeningPoint(miConfig.getTransport()).getPort(), miConfig.getTransport(), null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
				
			/*// Create contact headers
			String host = sipStack.getIPAddress();
			
			// Create the contact name address.
			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tlsListeningPoint.getPort());
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(miConfig.getTransport()).getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			contactHeader = headerFactory.createContactHeader(contactAddress);
			*/	
			// Create a new CallId header
			callIdHeader = sipProvider.getNewCallId();
			 
			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(numSeq, Request.INVITE);
			
			//Add the user agent
			ArrayList<String> Agentes = new ArrayList<String>();
			Agentes.add("Istados videolive!");
			userAgentHeader = headerFactory.createUserAgentHeader(Agentes);

			// Create the request.
			Request request = messageFactory.createRequest(requestURI, Request.INVITE, callIdHeader,
					cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

			request.addHeader(contactHeader);
			request.addHeader(userAgentHeader);
			
			offerInfo=new InfoSDP();
	        offerInfo.IpAddress=miConfig.getMiIP();
	        offerInfo.aport=miConfig.getPuertoAudio();
	        offerInfo.aformat=miConfig.getFormatoAudio();
	        offerInfo.vport=miConfig.getPuertoVideo();
	        offerInfo.vformat=miConfig.getFormatoVideo();
	        
	        ContentTypeHeader contentTypeHeader=headerFactory.createContentTypeHeader("application","sdp");
	        mySdpManager=new SDP();
	        byte[] content = mySdpManager.createSdpInvite(offerInfo, listaUsers);
	        request.setContent(content,contentTypeHeader);

			// Create the client transaction.
			listener.inviteTid = sipProvider.getNewClientTransaction(request);

			// send the request out.
			listener.inviteTid.sendRequest();
			dialog = inviteTid.getDialog();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}
	public void InviteVM(String userVM) {
		Softphone listener = this;
		String suUser;
		try {
			String fromName = miConfig.getMiUser();
			String fromSipAddress = miConfig.getMiDominio(); //"here.com";
			String fromDisplayName = miConfig.getMiUser();
			
			suUser="videomensaje";

			String toSipAddress = miConfig.getMiDominio(); //"there.com";
			String toUser = suUser; //"20002";
			String toDisplayName = suUser;

			// create >From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader =
				headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI = addressFactory.createSipURI(toUser, miConfig.getProxyPuerto());

			// Create ViaHeaders
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			//int port = sipProvider.getListeningPoint(this.getTransport()).getPort();
			ViaHeader viaHeader = headerFactory.createViaHeader(sipStack.getIPAddress(),
					sipProvider.getListeningPoint(miConfig.getTransport()).getPort(),
					miConfig.getTransport(), null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
				
			/*// Create contact headers
			String host = sipStack.getIPAddress();
			
			// Create the contact name address.
			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tlsListeningPoint.getPort());
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(miConfig.getTransport()).getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);		
			
			contactHeader = headerFactory.createContactHeader(contactAddress);*/
				
			// Create a new CallId header
			callIdHeader = sipProvider.getNewCallId();
			 
			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(numSeq, Request.INVITE);
			
			//Add the user agent
			ArrayList<String> Agentes = new ArrayList<String>();
			Agentes.add("Istados videolive!");
			userAgentHeader = headerFactory.createUserAgentHeader(Agentes);

			// Create the request.
			Request request = messageFactory.createRequest(requestURI, Request.INVITE, callIdHeader,
					cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

			request.addHeader(contactHeader);
			request.addHeader(userAgentHeader);
			
			offerInfo=new InfoSDP();
	        offerInfo.IpAddress=miConfig.getMiIP();
	        offerInfo.aport=miConfig.getPuertoAudio();
	        offerInfo.aformat=miConfig.getFormatoAudio();
	        offerInfo.vport=miConfig.getPuertoVideo();
	        offerInfo.vformat=miConfig.getFormatoVideo();
	        
	        ContentTypeHeader contentTypeHeader=headerFactory.createContentTypeHeader("application","sdp");
	        mySdpManager=new SDP();
	        byte[] content = mySdpManager.createSdpInviteVM(offerInfo, userVM);
	        request.setContent(content,contentTypeHeader);

			// Create the client transaction.
			listener.inviteTid = sipProvider.getNewClientTransaction(request);

			// send the request out.
			listener.inviteTid.sendRequest();
			dialog = inviteTid.getDialog();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}
	
	public void Bye(String user) {
		Softphone listener = this;
		System.out.println("He entrado en bye");
		try {
			String fromName = miConfig.getMiUser();
			String fromSipAddress = miConfig.getMiDominio(); //"here.com";
			String fromDisplayName = miConfig.getMiUser();
			

			String toSipAddress = miConfig.getMiDominio(); //"there.com";
			String toUser = user; //"20002";
			String toDisplayName = user;

			// create From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader =
				headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI = addressFactory.createSipURI(toUser, miConfig.getProxyPuerto());

			// Create ViaHeaders
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			//int port = sipProvider.getListeningPoint(this.getTransport()).getPort();
			ViaHeader viaHeader = headerFactory.createViaHeader(sipStack.getIPAddress(),
					sipProvider.getListeningPoint(miConfig.getTransport()).getPort(),
					miConfig.getTransport(), null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
				
			/*// Create contact headers
			String host = sipStack.getIPAddress();
			
			// Create the contact name address.
			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tlsListeningPoint.getPort());
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(miConfig.getTransport()).getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			contactHeader = headerFactory.createContactHeader(contactAddress);
			*/
			
			// Create a new CallId header
			CallIdHeader callIdHeaderBye = this.callIdHeader;
			//CallIdHeader callIdHeaderBye = sipProvider.getNewCallId();
			
			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(numSeq, Request.BYE);
			
			//Add the user agent
			ArrayList<String> Agentes = new ArrayList<String>();
			Agentes.add("Istados videolive!");
			userAgentHeader = headerFactory.createUserAgentHeader(Agentes);

			// Create the request.
			Request request = messageFactory.createRequest(requestURI, Request.BYE, callIdHeaderBye,
					cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);

			System.out.println("Contactheader: " + contactHeader);
			request.addHeader(contactHeader);
			request.addHeader(userAgentHeader);	
			
			SipURI addressRouter = addressFactory.createSipURI(null, "147.83.115.200;lr=on");
			Address address = addressFactory.createAddress(addressRouter);
			RouteHeader route = headerFactory.createRouteHeader(address);
			System.out.println("routerrr " + address);
			request.addHeader(route);
			
			
			// Create the client transaction.
			listener.inviteTid = sipProvider.getNewClientTransaction(request);

			
			// send the request out.
			//dialog.sendRequest(listener.inviteTid);
			//listener.inviteTid.sendRequest();
			dialog = listener.inviteTid.getDialog();
			dialog.sendRequest(listener.inviteTid);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}
	
	public void Publish(String localURI,String status, String miUser, int opcion) {
        try {
        	ToHeader toHeader = null;
            String toDisplayName = miUser;
            String toUser= miUser;
            
            System.out.println("Sending PUBLISH in progress");       

	    // Request-URI:
            SipURI requestURI = addressFactory.createSipURI(toUser, miConfig.getProxyPuerto());
          
	    //  Via header
            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            ViaHeader viaHeader = headerFactory.createViaHeader(sipStack
					.getIPAddress(), sipProvider.getListeningPoint(
					miConfig.getTransport()).getPort(),
					miConfig.getTransport(), null);
            viaHeaders.add(viaHeader);   
            
	     // To header:
            String toSipAddress = miConfig.getMiDominio();
            SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			toHeader = headerFactory.createToHeader(toNameAddress,null);
            

	    // From header:  
	    String fromName = miConfig.getMiUser();
		String fromSipAddress = miConfig.getMiDominio(); // "here.com";
		String fromDisplayName = miConfig.getMiUser();
		
		SipURI fromAddress = addressFactory.createSipURI(fromName,
				fromSipAddress);

		
		Address fromNameAddress = addressFactory.createAddress(fromAddress);
		fromNameAddress.setDisplayName(fromDisplayName);
		FromHeader fromHeader = headerFactory.createFromHeader(
				fromNameAddress, "12345");
	    
	    // Call-ID:
		CallIdHeader callIdHeader = null;
		if(opcion==1)
			callIdHeader = sipProvider.getNewCallId();
		else
			callIdHeader = headerFactory.createCallIdHeader("7ee9f379e71ac191c021a8d095cd4dac@0.0.0.0");
		
		
	    // CSeq:
		CSeqHeader cseqHeader = null;
		if (opcion ==1){
		    cseqHeader = headerFactory.createCSeqHeader(1,"PUBLISH");
		}
		else{
			cseqHeader = headerFactory.createCSeqHeader(18,"PUBLISH");
		}
	    
	    // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader =
		headerFactory.createMaxForwardsHeader(70);
           
	    //Create Request
	    Request request=messageFactory.createRequest(requestURI, "PUBLISH",
		    callIdHeader,cseqHeader,fromHeader,toHeader,viaHeaders,maxForwardsHeader);
        
	    // Content and Content-Type header:
	    String basic;
	    if (status.equals("offline"))
		basic="closed";
	    else
		basic="open";

	    String content=null;
	    if (opcion==1){
	    //String entity = Utils.generateTag();
	    localURI= "sip:"+localURI;
	    String entity = "t4583";
	    content = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" +
			"xmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\""+
			"xmlns:rpid=\"urn:ietf:params:xml:ns:pidf:rpid\"" + 
			" entity=\"" + localURI  + "\">\n" +
			"<dm:person id=\"p3546\">"+
			"<rpid:activities/>"+
			"</dm:person>"+
			" <tuple id=\"" + entity + "\">\n" +
			"  <status>\n" +
			"   <basic>" + basic + "</basic>\n" +
			"  </status>\n" + 
			"  <contact>" + localURI + "</contact>\n" +
			"  <note>" + status + "</note>\n" +
			" </tuple>\n" +
			"</presence>";
	    
	    	ContentTypeHeader contentTypeHeader = 
			headerFactory.createContentTypeHeader("application", "pidf+xml");
		    request.setContent(content, contentTypeHeader);
	    }
	    if (opcion==2){
	    	ContentTypeHeader contentTypeHeader = 
			headerFactory.createContentTypeHeader("application", "pidf+xml");
		    request.setHeader(contentTypeHeader);
		    }
		    
	    //Contact header
		    String host = sipStack.getIPAddress();			
		    Address contactAddress = null;
		    SipURI sipURI=addressFactory.createSipURI(fromName,host);
            sipURI.setPort(tlsListeningPoint.getPort());
            sipURI.setTransportParam(miConfig.getTransport());
            sipURI.setParameter("registering_acc", "147_83_115_200");
            contactAddress=addressFactory.createAddress(sipURI);
            contactHeader = headerFactory.createContactHeader(contactAddress);

		// Add the contact address.
		contactAddress.setDisplayName(fromName);
	    request.setHeader(contactHeader);
		
		// Add the user agent
		ArrayList<String> Agentes = new ArrayList<String>();
		//Agentes.add("Istados videolive!");
		Agentes.add("SIP Communicator1.0-alpha6-nightly.build.3162Windows XP");
		//Agentes.add("X-Lite release 1002tx stamp 29712");
		userAgentHeader = headerFactory.createUserAgentHeader(Agentes);
		request.addHeader(userAgentHeader);
		
	    // Expires header: 
		ExpiresHeader expiresHeader = null;
		if (opcion ==1)
			expiresHeader = headerFactory.createExpiresHeader(3600);
		else
			expiresHeader = headerFactory.createExpiresHeader(0);
		request.setHeader(expiresHeader);

		//Event header:
		Header eventHeader = headerFactory.createHeader("Event", "presence");
		request.setHeader(eventHeader);
		
		//Sip-If-Match Header
		if (opcion==2 || opcion==3){
			Header sipIfMatchHeader = headerFactory.createHeader("SIP-If-Match",sipEtagGlobal);
			request.setHeader(sipIfMatchHeader);
		}
	    // Content-Length header: 
		if (opcion==1){
		    ContentLengthHeader contentLengthHeader =
			headerFactory.createContentLengthHeader(content.length());
		    request.setContentLength(contentLengthHeader);
		}
		else{
			ContentLengthHeader contentLengthHeader =
			headerFactory.createContentLengthHeader(0);
		    request.setContentLength(contentLengthHeader);
		}
	    // Send request
	    ClientTransaction clientTransaction=sipProvider.getNewClientTransaction(request);
	    clientTransaction.sendRequest();
             
	}
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	public void Subscribe(String suUser, int opcion) {
		Softphone listener = this;
		try {
			String fromName = miConfig.getMiUser();
			String fromSipAddress = miConfig.getMiDominio(); // "here.com";
			String fromDisplayName = miConfig.getMiUser();

			String toSipAddress = miConfig.getMiDominio(); // "there.com";
			String toUser = suUser; // "20002";
			String toDisplayName = suUser;

			// create >From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName,
					fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader = headerFactory.createFromHeader(
					fromNameAddress, "12345");
			
			
			// create To Header
			ToHeader toHeader = null;
			System.out.println("JEAJEA"+miConfig.getMiUser());
			System.out.println("JOJO"+ suUser);
			if (suUser.equals(miConfig.getMiUser()))
			{
				SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
				Address toNameAddress = addressFactory.createAddress(toAddress);
				toNameAddress.setDisplayName(toDisplayName);
				toHeader = headerFactory.createToHeader(toNameAddress,null);
			}
			else{
				SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
				Address toNameAddress = addressFactory.createAddress(toAddress);
				if (opcion==2 || opcion==3)
					toHeader = headerFactory.createToHeader(toNameAddress,"12345");
				else
					toHeader = headerFactory.createToHeader(toNameAddress,null);
			}

			// create Request URI
			SipURI requestURI = null;
			if (suUser.equals(miConfig.getMiUser()) && opcion==1)
				requestURI=addressFactory.createSipURI(miConfig.getMiUser(),miConfig.getProxy());
			if (!(suUser.equals(miConfig.getMiUser())) && opcion==1 )
				requestURI=addressFactory.createSipURI(toUser,miConfig.getProxy());
			if (opcion==3) //Si es cuando se desconecta Request-Line: SUBSCRIBE sip:istados.com:5060 SIP/2.0
			{
				requestURI=addressFactory.createSipURI(null,miConfig.getProxy());
	            requestURI.setPort(puertoProxy);

				//requestURI.setHost(miConfig.getMiDominio());
			}
			
			
			// Create ViaHeaders
			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			// int port =
			// sipProvider.getListeningPoint(this.getTransport()).getPort();
			ViaHeader viaHeader = headerFactory.createViaHeader(sipStack
					.getIPAddress(), sipProvider.getListeningPoint(
					miConfig.getTransport()).getPort(),
					miConfig.getTransport(), null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory
					.createMaxForwardsHeader(70);

			// Create contact headers
			String host = sipStack.getIPAddress();
			Address contactAddress = null;
			
			// Create the contact name address.
			if (suUser.equals(miConfig.getMiUser())){
				/*SipURI contactUrl = addressFactory.createSipURI(fromName, host);
				contactUrl.setPort(tlsListeningPoint.getPort());
				SipURI contactURI = addressFactory.createSipURI(fromName, host);
				contactURI.setPort(sipProvider.getListeningPoint(
						miConfig.getTransport()).getPort());
	
				contactAddress = addressFactory.createAddress(contactURI);
	
				// Add the contact address.
				contactAddress.setDisplayName(fromName);*/
				
				// Contact header SIP COMM:
	            SipURI sipURI=addressFactory.createSipURI(fromName,host);
	            sipURI.setPort(tlsListeningPoint.getPort());
	            sipURI.setTransportParam(miConfig.getTransport());
	            sipURI.setParameter("registering_acc", "147_83_115_200");
	            contactAddress=addressFactory.createAddress(sipURI);
	            // Add the contact address.
				contactAddress.setDisplayName(fromName);
			}
			else{			
				// Contact header SIP COMM:
	            SipURI sipURI=addressFactory.createSipURI(fromName,host);
	            sipURI.setPort(tlsListeningPoint.getPort());
	            sipURI.setTransportParam(miConfig.getTransport());
	            sipURI.setParameter("registering_acc", "147_83_115_200");
	            contactAddress=addressFactory.createAddress(sipURI);
	            // Add the contact address.
				contactAddress.setDisplayName(fromName);
			}
			

			contactHeader = headerFactory.createContactHeader(contactAddress);
			
			
			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();

			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(numSeq,
					Request.SUBSCRIBE);

			// Add the user agent
			ArrayList<String> Agentes = new ArrayList<String>();
			//Agentes.add("Istados videolive!");
			Agentes.add("SIP Communicator1.0-alpha6-nightly.build.3162Windows XP");
			//Agentes.add("X-Lite release 1002tx stamp 29712");
			userAgentHeader = headerFactory.createUserAgentHeader(Agentes);

			// Create the request.
				Request request = messageFactory.createRequest(requestURI,
						Request.SUBSCRIBE, callIdHeader, cSeqHeader, fromHeader,
						toHeader, viaHeaders, maxForwards);
	
				request.addHeader(contactHeader);
				request.addHeader(userAgentHeader);
				
			Address router = null;
			Header routeHeader = null;
			if (opcion==3) //si ho faig desde casa mode desconexio
			{
				requestURI=addressFactory.createSipURI("siproxd","192.168.1.1");
				requestURI.setPort(miConfig.getMiPuertoSip());
				//routeHeader = headerFactory.createHeader("Route", requestURI);
				router = addressFactory.createAddress(requestURI);
				routeHeader = headerFactory.createRouteHeader(router);
			}
			
			if (suUser.equals(miConfig.getMiUser())&& opcion==1){
				//Las cabeceras eventHeader i acceptHeader en inicio de sesion
				Header eventHeader = headerFactory.createHeader("Event", "presence.winfo");
				request.setHeader(eventHeader);
	
				Header acceptHeader = headerFactory.createHeader("Accept","application/watcherinfo+xml");
				request.setHeader(acceptHeader);
			}
			else{
				//Las cabeceras eventHeader i acceptHeader al agregar a un contacto
				Header eventHeader = headerFactory.createHeader("Event", "presence");
				request.setHeader(eventHeader);
	
				Header acceptHeader = headerFactory.createHeader("Accept","application/pidf+xml");
				request.setHeader(acceptHeader);
			}
			ExpiresHeader expiresHeader = null;
			if (opcion == 1)
				expiresHeader = headerFactory.createExpiresHeader(3600);
			if (opcion ==2 || opcion ==3)
				expiresHeader = headerFactory.createExpiresHeader(0);
			
			request.setHeader(expiresHeader);

			// Create the client transaction.
			listener.inviteTid = sipProvider.getNewClientTransaction(request);

			// send the request out.
			listener.inviteTid.sendRequest();
			dialog = inviteTid.getDialog();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}

	}
	
	public void execute() throws UnknownHostException, IOException{
		shell.open();//fa que sigui visible
		while(!shell.isDisposed()){//mentre no tanquis la finestra...
			if(!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();//alliberar recursos
		desconexion();
		System.exit(1);
	}
	public void abrirVideomensaje(){
		Videomensaje videomensaje=new Videomensaje();
		videomensaje.execute();
		enviarVM = videomensaje.enviar;
	}	
	public void abrirAutenticacion(){
		Autenticacion autenticacion= new Autenticacion();
		autenticacion.execute();
		this.setNomUser(autenticacion.usuario);
		this.setPass(autenticacion.password);
		this.setDomain(autenticacion.dominio);
	}
	public void agregarContacto(String estado, String nombre, String URI){
		//Contacto contacto = new Contacto ("offline","adria","adria@istados.com");
		Contacto contacto = new Contacto (estado,nombre,URI);
		listaContactos.addContact (contacto);
		list.add(contacto.getNombre()+ "  ("+contacto.getEstado()+")");
		Subscribe(nombre,1);
	}
	
	public void eliminaContacto(Contacto contacto){
		listaContactos.deleteContact(contacto);
		list.removeAll();
		lista = listaContactos.getContacts();
		for (int i = 0; i < lista.size(); i++)
		{
			Contacto a = lista.get(i);
			list.add(a.getNombre()+ "  ("+a.getEstado()+")");
			Subscribe(a.getNombre(),1); //si agrego a un contacto
		}
		
		Subscribe (contacto.getNombre(), 3);
	}
	
	public void cambioEstadoLocal(String estado,String localURI,String miUser, int opcion){
		this.contactoLocal.setEstado(estado);
		Publish(localURI,contactoLocal.getEstado(), miUser,opcion);//localURI y status como parametros
	}
	
	public void desconexion(){
		Publish(miConfig.getLocalURI(),contactoLocal.getEstado(), miConfig.getMiUser(),3);
		lista = listaContactos.getContacts();
		while (!enviarSubcribe3){
			
		}
		//A cada usuario que estas suscrito te tienes que quitar suscribcion
		System.out.println("Envio subscribe con expires a 0");
		for (int i=0;i<lista.size();i++){
			Contacto contacto = lista.get(i);
			Subscribe(contacto.getNombre(), 3); //opcion 3 es desconex desde casa
			//OP 3:Como toy detras de NAT en casa: Route: <sip:siproxd@192.168.1.1:5060;lr>
		}
		
		//Ahora tendre que enviar register con Expires:0
		enviarRegisterDes = true;
		autenticado=false;
		esperandoRegister=true;
		contRegister = 0; 
		Register();
		while (esperandoRegister)
		{
			try {
    			Thread.sleep (1000);
    		} catch (Exception e) {
    		}
		}
		
	}
	
	public static void inicia() throws  ParseException, TooManyListenersException, IOException {
		
		
		String transport = "udp";
		String miIP = "147.83.115.36";
		//String miIP = "192.168.1.5";		
		//String miIP = InetAddress.getLocalHost().getHostAddress();
		int miPuertoSip = 5050;
		String proxy = "147.83.115.200";
		String proxyPuerto = "147.83.115.200:5060";
		String miUser = null;
		String miPass = null;
		String miDominio = null;
		String localURI =miUser+"@"+miIP;
		int puertoAudio = 8000;
		int formatoAudio = 4; //G.711
		int puertoVideo = 8002;
		int formatoVideo = 4;
		Configuracion miConfig = new Configuracion(transport, miIP, miPuertoSip, proxy, proxyPuerto, miUser,miPass,
				miDominio, puertoAudio, formatoAudio, puertoVideo, formatoVideo, localURI);
		
		Softphone softphone = new Softphone(miConfig);
		softphone.puertoProxy=5060;
		
		while(true){
			softphone.abrirAutenticacion();
			
			if(!softphone.esperandoRegister)
				softphone.esperandoRegister=true;
			else
				softphone.init();
			//softphone.videomensaje = videomensaje;
			softphone.Register();
			softphone.initialize(); 
			
			while (softphone.esperandoRegister)
			{
				try {
	    			Thread.sleep (1000);
	    		} catch (Exception e) {
	    		}
			}
			if (softphone.autenticado){
				softphone.Subscribe(miConfig.getMiUser(),1); //Al inicio de sesion,mismo TO Y FROM
				System.out.println("localuri: " + localURI);
				softphone.contactoLocal = new Contacto ("Online",miConfig.getMiUser(),localURI);
				softphone.cambioEstadoLocal("Online", miConfig.getLocalURI(), miConfig.getMiUser(), 1);
				while (true)
				{	
					//softphone.display.dispose();
					//softphone.initialize();
					softphone.execute();
				}
				
			}
			else
			{
				
				MessageBox messageBox = new MessageBox(softphone.shell, SWT.OK | SWT.ICON_INFORMATION);
				messageBox.setText("Error");
				messageBox.setMessage("Los datos no son correctos");
				messageBox.open();
				softphone.display.dispose();
			}
			softphone.autenticado = false;
		}
	}

	public static void main(String args[]) throws ParseException, TooManyListenersException, IOException {
	
		
		while (true)
			inicia();
	}
	public void processIOException(IOExceptionEvent exceptionEvent) {
	        System.out.println("An IO Exception occured!");
	}
	public void processTransactionTerminated(
	            TransactionTerminatedEvent transactionTerminatedEvent) {
	        // System.out.println("TransactionTerminated event notification");
	}
	public void processDialogTerminated(
	    		DialogTerminatedEvent dialogTerminatedEvent) {
		this.terminatedCount ++;
	    System.out.println("DialogTerminatedEvent notification " + this.terminatedCount +
	    		" dialog ID = "+ dialogTerminatedEvent.getDialog().getDialogId());
	}
	
}



