package Presencia;

import java.io.ByteArrayInputStream;

import java.io.IOException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParser {

	/**
	 * @param args
	 */
	
	public static Document string2DOM(String s) 
	{
	    Document tmpX=null;
	    DocumentBuilder builder = null;
	    int coderror;
		String msgerror;
		try{
	        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    }catch(javax.xml.parsers.ParserConfigurationException error){
	        coderror=10;
	        msgerror="Error crando factory String2DOM "+error.getMessage();
	        return null;
	    }
	    try{
	        tmpX=builder.parse(new ByteArrayInputStream(s.getBytes()));
	    }catch(org.xml.sax.SAXException error){
	        coderror=10;
	        msgerror="Error parseo SAX String2DOM "+error.getMessage();
	        return null;
	    }catch(IOException error){
	        coderror=10;
	        msgerror="Error generando Bytes String2DOM "+error.getMessage();
	        return null;
	    }
	    return(tmpX);
	}
	
	public static String tratarXML (String stringXML) { //Me pasan el String XML
		
		//File archivo = new File("archivo.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Document doc = string2DOM (stringXML); 

		//AÑADIDO DE AQUI HASTA...
		NodeList listaNodosHijo2 = doc.getElementsByTagName("basic");
		Node unNodoHijo2 = listaNodosHijo2.item(0);
		String basic = unNodoHijo2.getTextContent();
		String presencia = null;
		if (basic.equals("open")){
			//...HASTA AQUI
			
			NodeList listaNodosHijo = doc.getElementsByTagName("note");
	
			Node unNodoHijo = listaNodosHijo.item(0);
			
			presencia = unNodoHijo.getTextContent();
			
			return(presencia);
		}
		else if (basic.equals("closed"))
			return "offline";
		else
			System.out.println("EL BASIC ES: "+basic+" por tanto no hay note");
		return presencia;
	}

}
