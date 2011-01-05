package InterfazGrafica;

import java.io.*;
import java.net.*;
import java.security.*;
import java.text.*;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;


public class Autenticacion {

		private Display display = null;
		private Shell shell = null;
		private Menu menubar = null;
		private Text user = null, pass = null, domain = null;
		private Label luser = null, lpass = null;
		public boolean loguejat=false;
		public Vector canals=new Vector();
		public Text log=null;
		public boolean controlador=false;
		public boolean avio=false;
		public String nom;

		public String usuario = null;
		public String password = null;
		public String dominio = null;
		
		
		public Autenticacion() {
			super();
			initialize();
			
		}

		private void initializeMenubar() {
			menubar = new Menu(shell, SWT.BAR);
			shell.setMenuBar(menubar);
			initializeMenuFile();

		}

		private void initializeMenuFile() {
			MenuItem file = new MenuItem(menubar, SWT.CASCADE);
			file.setText("Archivo");// fem el text "file" del menu
			Menu filemenu = new Menu(shell, SWT.DROP_DOWN);
			file.setMenu(filemenu);// li posem un desplegable
			

			MenuItem separator = new MenuItem(filemenu, SWT.SEPARATOR);
			MenuItem exitItem = new MenuItem(filemenu, SWT.PUSH);
			exitItem.setText("Salir");
			exitItem.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					shell.close();// tanquem la fienstra
				}

			});
		}

		private void initialize() {
			display = new Display();
			shell = new Shell(display, SWT.SHELL_TRIM);// podem posar lestil que volguem
			shell.setSize(200, 280);
			shell.setText("Iniciar Sesión");// posar títol
			
			GridLayout gl = new GridLayout();
			gl.numColumns=1;
			shell.setLayout(gl);		
		
			GridData gd1 = new GridData();
			gd1.horizontalSpan = 1;
			gd1.verticalSpan = 1;
			gd1.horizontalAlignment = SWT.FILL;
			gd1.grabExcessHorizontalSpace = true;
			gd1.verticalAlignment = SWT.FILL;
			
			GridLayout g2 = new GridLayout();
			g2.numColumns=1;
			
			Group grp = new Group(shell, SWT.NONE);
			
			grp.setText("Inicio Sesión");
			grp.setLayout(g2);
			grp.setLayoutData(gd1);		
			
			GridData gd2 = new GridData();
			gd2.horizontalAlignment = SWT.LEFT;
			gd2.grabExcessHorizontalSpace = true;
			gd2.verticalAlignment = SWT.CENTER;
			gd2.grabExcessVerticalSpace = true;		

			Font fuente = new Font(display,"Arial", 10, SWT.BOLD );
			
			Label label1 = new Label(grp, 0);
			label1.setText("Usuario: ");
			label1.setLayoutData(gd2);			
			label1.setFont(fuente);

			
			GridData gd3 = new GridData();
			gd3.horizontalAlignment = SWT.FILL;
			gd3.grabExcessHorizontalSpace = true;
			gd3.verticalAlignment = SWT.CENTER;
			gd3.grabExcessVerticalSpace = true;
			
			user = new Text(grp, SWT.BORDER);
			user.setEditable(true);
			user.setText("adria");
			user.setLayoutData(gd3);
			
			
			GridData gd4 = new GridData();
			gd4.horizontalAlignment = SWT.LEFT;
			gd4.grabExcessHorizontalSpace = true;
			gd4.verticalAlignment = SWT.CENTER;
			gd4.grabExcessVerticalSpace = true;
			
			Label label2 = new Label(grp, SWT.BOLD);
			label2.setText("Contraseña: ");
			label2.setLayoutData(gd4);
			label2.setFont(fuente);
			
			GridData gd5 = new GridData();
			gd5.horizontalAlignment = SWT.FILL;
			gd5.grabExcessHorizontalSpace = true;
			gd5.verticalAlignment = SWT.CENTER;
			gd5.grabExcessVerticalSpace = true;
			
			pass = new Text(grp, SWT.BORDER);
			pass.setEditable(true);
			pass.setText("secret");
			pass.setLayoutData(gd5);
			
			Label label3 = new Label(grp, 0);
			label3.setText("Dominio: ");
			label3.setLayoutData(gd4);
			label3.setFont(fuente);
			
			domain = new Text(grp, SWT.BORDER);
			domain.setEditable(true);
			domain.setLayoutData(gd5);
			domain.setText("147.83.115.200");
			
			GridData gd7 = new GridData();
			gd7.horizontalAlignment = SWT.CENTER;
			gd7.grabExcessHorizontalSpace = true;
			gd7.verticalAlignment = SWT.CENTER;
			gd7.horizontalSpan = 3;
			gd7.verticalSpan = 1;
			
			Button bPush = new Button(shell, SWT.PUSH | SWT.CENTER);
			bPush.setBounds(100, 160, 100, 40);
			bPush.setText("Iniciar Sesión");
			bPush.setLayoutData(gd7);


			bPush.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {

					if(user.getText()!="" && pass.getText()!="" && domain.getText()!="")
					{
						usuario = user.getText();
						password = pass.getText();
						dominio = domain.getText(); 
						display.dispose();
					}
					else
					{
						MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
						messageBox.setText("Error");
						messageBox.setMessage("Debe rellenar todos los campos");
						messageBox.open();
					}
					
					
				}
			});		
		}

		public void execute() {
			shell.open();// fa que sigui visible
			while (!shell.isDisposed()) {// mentre no tanquis la finestra...
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();// alliberar recursos
		}

		/*public static  void inicia() throws  ParseException, TooManyListenersException, IOException {
			Autenticacion autenticacion= new Autenticacion();
			autenticacion.execute();
			Softphone softphone = new Softphone();
			softphone.execute();
			Videomensaje videomensaje= new Videomensaje();
			videomensaje.execute();
			
		}
		
	

		public static void main(String[] args) throws ParseException, TooManyListenersException, IOException {
			
			inicia();
		}*/
		
	

	}