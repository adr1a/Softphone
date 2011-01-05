
package Presencia;

public class Contacto {
	private String URI;
	private String nombre;
	private String estado;
	public Contacto(String estat, String nom, String uri) {
		// TODO Auto-generated constructor stub
		this.estado = estat;
		this.nombre = nom;
		this.URI = uri;
	}
	public String getURI() {
		return URI;
	}
	public void setURI(String uRI) {
		URI = uRI;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
}


