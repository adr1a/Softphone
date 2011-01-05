package InterfazGrafica;
import java.io.*;
import java.text.*;
import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;

import org.eclipse.swt.widgets.*;



public class Videomensaje {

	private Display display = null;
	private Shell shell = null;
	private Menu menubar = null;
	private Text user = null, pass = null;
	private Label luser = null, lpass = null;
	public boolean loguejat=false;
	public Vector canals=new Vector();
	public Text log=null;
	public boolean enviar=false;
	public String nom;
	

	
	
	public Videomensaje() {
		super();
		initialize();
		
	}


	private void initialize() {
		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);// podem posar lestil que volguem
		shell.setSize(330, 200);
		shell.setText("Videomensaje");// posar títol

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
		
		grp.setText("Videomensaje");
		grp.setLayout(g2);
		grp.setLayoutData(gd1);		
		
		GridData gd2 = new GridData();
		gd2.horizontalAlignment = SWT.FILL;
		gd2.grabExcessHorizontalSpace = true;
		gd2.verticalAlignment = SWT.CENTER;
		gd2.grabExcessVerticalSpace = true;		

		Font fuente = new Font(display,"Arial", 10, SWT.BOLD );
		
		Label label1 = new Label(grp, 0);
		label1.setText("El usuario no se encuentra disponible, puede \ndejarle un videomensaje si lo desea.\n¿Desea hacerlo?");
		label1.setLayoutData(gd2);			
		label1.setFont(fuente);	
		
		GridData gd3 = new GridData();
		gd3.horizontalAlignment = SWT.FILL;	

		Group grup1= new Group(shell,SWT.SHADOW_NONE);	
		GridLayout gl2 = new GridLayout();
		gl2.numColumns = 2;
		grup1.setLayout(gl2);
		grup1.setLayoutData(gd3);

		GridData gd7 = new GridData();
		gd7.horizontalAlignment = SWT.FILL;
		gd7.grabExcessHorizontalSpace = true;
		gd7.verticalAlignment = SWT.CENTER;
		
		Button botonSi = new Button(grup1, SWT.PUSH | SWT.CENTER);
		botonSi.setText("  Enviar  ");
		botonSi.setLayoutData(gd7);
		
		Button botonNo = new Button(grup1, SWT.PUSH | SWT.CENTER);
		botonNo.setText("   Cancelar   ");
		botonNo.setLayoutData(gd7);


		botonSi.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				enviar = true;
				display.dispose();
			}
		});	
		
		botonNo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {

				enviar = false;
				display.dispose();
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

}
