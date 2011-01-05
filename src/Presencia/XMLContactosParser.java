package Presencia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class XMLContactosParser {
	//private List <Contacto> buddies =  new ArrayList <Contacto>();
	private ArrayList<Contacto> buddies = new ArrayList<Contacto>();
	
	public void addContact(String fitxer,Contacto contact){

	File archivo = null;
	String nombre = contact.getNombre();
	String URI = contact.getURI();
	
	try {
		archivo = new File(fitxer);
		boolean exists = archivo.exists();
		if (exists) {
			// It returns false if File or directory does not exist
			System.out.println("el fichero YA existe : " + exists);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			Document documento = null;

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				documento = builder.parse(new File(fitxer));
				
				Node root = documento.getFirstChild();
				
				Element usuario = documento.createElement("Usuario");
				
				Text name = documento.createTextNode(nombre);
				Element Nombre = documento.createElement("Nombre");
				Nombre.appendChild(name);
				usuario.appendChild(Nombre);
				
				Text uri = documento.createTextNode(URI);
				Element Uri = documento.createElement("URI");
				Uri.appendChild(uri);
				usuario.appendChild(Uri);
				
				root.appendChild(usuario);
				
				root.appendChild(usuario);

				// AHORA CREO FICHERO FINAL
				// Preparar el documento DOM para la escritura
				Source source = new DOMSource(documento);

				// Preparar el fichero de salida
				File file = new File(fitxer);
				StreamResult result = new StreamResult(file);

				// Escribir el documento DOM en el fichero
				Transformer xformer = TransformerFactory.newInstance()
						.newTransformer();
				xformer.transform(source, result);
				System.out.println("Se ha añadido el usuario al fichero XML: " + fitxer);

			} catch (Exception spe) {
				// Algún tipo de error: fichero no accesible, formato de XML
				// incorrecto, etc.
				System.out.println("El fichero existe pero no es XML");
			}
		} else {
			System.out.println("Crear nodo");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Element root = doc.createElement("Users");
			root.setAttribute("xmlns", "Espacio1");
			root.setAttribute("xmlns:xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation",
					"Espacio1 ../schemas/proyecto.xsd");
			doc.appendChild(root);
			
			Element usuario = doc.createElement("Usuario");
			
			Text name = doc.createTextNode(nombre);
			Element Nombre = doc.createElement("Nombre");
			Nombre.appendChild(name);
			usuario.appendChild(Nombre);
			
			Text uri = doc.createTextNode(URI);
			Element Uri = doc.createElement("URI");
			Uri.appendChild(uri);
			usuario.appendChild(Uri);
			
			root.appendChild(usuario);

			// AHORA CREO FICHERO FINAL
			// Preparar el documento DOM para la escritura
			Source source = new DOMSource(doc);

			// Preparar el fichero de salida
			File file = new File(fitxer);
			StreamResult result = new StreamResult(file);

			// Escribir el documento DOM en el fichero
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);
			System.out.println("Se ha creado el fichero XML: " + fitxer);

		}
	} catch (Exception e) {
		System.out.println("Error en la creacion del fichero");
		e.printStackTrace();
	} 
}
	
	public ArrayList<Contacto> getBuddies() {
        return buddies;
    }
	public void cargarLista(String fitxer) {
		// TODO Auto-generated method stub
		File archivo = null;
		String nombre = null;
		String URI = null;
		try {
			archivo = new File(fitxer);
			boolean exists = archivo.exists();
			if (exists) {
				// It returns false if File or directory does not exist
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				Document documento = null;

				try {
					DocumentBuilder builder = factory.newDocumentBuilder();
					documento = builder.parse(new File(fitxer));
					
					NodeList listaNodosHijo = documento.getElementsByTagName("Usuario");
					int i = 0;
					while ( i<listaNodosHijo.getLength()){
						Node unNodoHijo = listaNodosHijo.item(i);
						
						//Obtengo lista de elementos de un nodo
						NodeList listaElementosNodo = unNodoHijo.getChildNodes();
						//Obtengo el valor de URI
						int j=0;
						while (j<listaElementosNodo.getLength())
						{
						   Node unElementodelHijo = listaElementosNodo.item(j);
						   //System.out.println(unElementodelHijo.getNodeName());
						   if (unElementodelHijo.getNodeName()=="URI")
							   URI = unElementodelHijo.getTextContent();
						   else if (unElementodelHijo.getNodeName()=="Nombre")
							   nombre = unElementodelHijo.getTextContent();
						   j++;
						}
						//estat offline per defecte despres ja mirarem subscribe
						Contacto contact = new Contacto ("Offline", nombre, URI);
						buddies.add(contact);
						i++;
					}


				} catch (Exception spe) {
					System.out.println("No se ha podido consultar");
				}
			} else {
				System.out.println("Crear nodo");
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();

				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.newDocument();

				Element root = doc.createElement("Users");
				root.setAttribute("xmlns", "Espacio1");
				root.setAttribute("xmlns:xsi",
						"http://www.w3.org/2001/XMLSchema-instance");
				root.setAttribute("xsi:schemaLocation",
						"Espacio1 ../schemas/proyecto.xsd");
				doc.appendChild(root);
				

				// AHORA CREO FICHERO FINAL
				// Preparar el documento DOM para la escritura
				Source source = new DOMSource(doc);

				// Preparar el fichero de salida
				File file = new File(fitxer);
				StreamResult result = new StreamResult(file);

				// Escribir el documento DOM en el fichero
				Transformer xformer = TransformerFactory.newInstance()
						.newTransformer();
				xformer.transform(source, result);
				System.out.println("Se ha creado el fichero XML: " + fitxer);

			}
		} catch (Exception e) {
			System.out.println("Error en la creacion del fichero");
			e.printStackTrace();
		} 
	
	}

	public void deleteContact(String fitxer,Contacto contacto) {
		// TODO Auto-generated method stub
		DocumentBuilderFactory factory = DocumentBuilderFactory
		.newInstance();
		Document documento = null;

		try {
		DocumentBuilder builder = factory.newDocumentBuilder();
		documento = builder.parse(new File(fitxer));
		
		Node nodoRaiz = documento.getFirstChild();

		NodeList listaNodosHijo = documento.getElementsByTagName("Usuario");
		int i = 0;
		String URI = null;
		String nombre = null;
		boolean encontrado = false;
		while ( i<listaNodosHijo.getLength()){
			Node unNodoHijo = listaNodosHijo.item(i);
			
			//Obtengo lista de elementos de un nodo
			NodeList listaElementosNodo = unNodoHijo.getChildNodes();
			//Obtengo el valor de URI
			int j=0;
			while (j<listaElementosNodo.getLength())
			{
			   Node unElementodelHijo = listaElementosNodo.item(j);
			   if (unElementodelHijo.getNodeName()=="URI")
				   URI = unElementodelHijo.getTextContent();
			   if (unElementodelHijo.getNodeName()=="Nombre")
				   nombre = unElementodelHijo.getTextContent();
			   if (contacto.getNombre().equals(nombre) && contacto.getURI().equals(URI)){
				   encontrado = true;
				   nodoRaiz.removeChild(unNodoHijo);
			   }
			   j++;
			}
			i++;
		}
		System.out.println("ENCONTRADO"+encontrado);
		// AHORA CREO FICHERO FINAL
		// Preparar el documento DOM para la escritura
		Source source = new DOMSource(documento);

		// Preparar el fichero de salida
		File file = new File(fitxer);
		StreamResult result = new StreamResult(file);

		// Escribir el documento DOM en el fichero
		Transformer xformer = TransformerFactory.newInstance()
				.newTransformer();
		xformer.transform(source, result);
		System.out.println("Se ha creado el fichero XML: " + fitxer);
	
	} catch (Exception spe) {
		System.out.println("No se ha podido consultar");
	}
	}

}

