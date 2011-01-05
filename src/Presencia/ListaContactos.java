package Presencia;

import java.util.ArrayList;
import java.util.List;


public class ListaContactos {
	//private List <Contacto> lista = new ArrayList <Contacto>();
	private ArrayList<Contacto> lista = new ArrayList<Contacto>();
	private XMLContactosParser xmlContactosParser = new XMLContactosParser();
	
	
	public void addContact (Contacto contacto){
		
		xmlContactosParser.addContact("fichero.xml", contacto);
		lista.add(contacto);
	}
	
	public ArrayList<Contacto> getContacts() {
		return lista;
	}
	
	public ArrayList<Contacto> cargarLista (){
		xmlContactosParser.cargarLista("fichero.xml");
		lista = xmlContactosParser.getBuddies();
		return lista;
	}
	
	public void deleteContact (Contacto contacto){
		xmlContactosParser.deleteContact("fichero.xml",contacto);
		lista.remove(contacto);
	}
	
	public int seleccionarContacto(String nombre){
		boolean encontrado = false;
		int i = 0;
		while (!encontrado && i<lista.size()){
			if (lista.get(i).getNombre().equals(nombre))
			{
				encontrado = true;
				return i;
			}
			i++;
		}
		return -1;
	}

}
