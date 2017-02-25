package es.iesnervion.example.models.parking;

import java.util.concurrent.Semaphore;

/**
 * Clase parking, esta gestionará todo el parking
 * @author adripol94
 *
 */
public class Parking {
	private static Semaphore sEntrada;
	private static Semaphore sSalida;
	/**
	 * Numero de aparcamientos ocupados
	 */
	private static int aparcamientosOcupados = 0;
	/**
	 * Barreras de entrada.
	 */
	private Entrada[] entradas;
	/**
	 * Barrerasd e salida
	 */
	private Salida[] salidas;
	/**
	 * Tamaño del parking
	 */
	private int sizeParking;
	/**
	 * Contador que usaremos para darles permisos al as barreras de entrada de forma ordenada.
	 */
	private int contador;
	
	/**
	 * Constructor de parking
	 * @param sizeParking tamaño del parking
	 * @param numEntradas Numero de barreras de entradas.
	 * @param numSalidas Numero de barreras de salida.
	 */
	public Parking(int sizeParking, int numEntradas, int numSalidas) {
		//El numero de permisos es el tamaño del parking
		sEntrada = new Semaphore(sizeParking);
		//Solo 1 porque la salida o se puede usar porque hay un coche o no hay nadie y se usa
		sSalida = new Semaphore(1);
		this.sizeParking = sizeParking;
		
		//Contador correspondiente para darles permisos y ordenar las colas de entrada
		contador = 0;
		
		//Inicializacion de las entradas y salidas.
		
		entradas = new Entrada[numEntradas];
		for (int i=0; i < entradas.length; i++)
			entradas[i] = new Entrada(sEntrada);
		
		salidas = new Salida[numSalidas];
		for (int i=0; i < salidas.length; i++)
			salidas[i] = new Salida(sSalida);
	}
	
	/**
	 * Metodo para entrar del parking
	 * @param barrera Numero de barrera a usar. (Empieza desde 0).
	 * @param id Identificacion del coche.
	 * @return String con la respuesta de los {@link #aparcamientosOcupados} y {@link Semaphore#availablePermits()}.
	 * @throws Exception En caso de incrementarse {@link #aparcamientosOcupados} mas que {@link #sizeParking}.
	 * 		   En caso de que {@code barrera > Parking#sEntrada}
	 */
	public synchronized String entrar(int barrera, int id) throws Exception {
		//For debug -> Para saber si podria llegar a ser mas de 21
		if (aparcamientosOcupados > sizeParking)
			throw new Exception("Ha superado el limite " + aparcamientosOcupados);
		if (barrera > entradas.length)
			throw new Exception("La barrera de entrada usada no existe (" + barrera + ")");
		
		System.out.println("El coche " + id + " va a intentar entrar en la barrera n=" + barrera);
		
		entradas[barrera].entrar();
		
		return aparcamientosOcupados + " numero de permisos " + sEntrada.availablePermits();
	}
	
	/**
	 * Metodo para salir del parking
	 * @param barrera Barrera por la que se va a salir
	 * @param id Identificacion del coche
	 * @return {@linkplain String} con la valor de {@link #aparcamientosOcupados} y {@link Semaphore#availablePermits()}.
	 * @throws Exception En caso de darse {@code #aparcamientosOcupados < 0}
	 * 		   En caso de que {@code barrera > Parking#sEntrada} 
	 */
	public String salir(int barrera, int id) throws Exception {
		//For debug -> Para saber si podria llegar a ser menos de 21
		if (aparcamientosOcupados < 0)
			throw new Exception("Error el aparcamiento es infrerior a 0 Valor=" + aparcamientosOcupados);
		if (barrera > salidas.length)
			throw new Exception("La barrera de salida no existe (" + barrera +")");
		
		System.out.println("El coche " + id + " va a salir por la barrera n=" + barrera);
		
		salidas[barrera].salida();
		entradas[contador].release();
		
		aumentarContador();
		
		return aparcamientosOcupados + " numero de permisos " + sEntrada.availablePermits();
	}
	
	/**
	 * Suma 1 el contador de coches
	 */
	protected static void addCar() {
		aparcamientosOcupados++;
	}
	
	/**
	 * Resta 1 el contador de coches
	 */
	protected static void outCar() {
		aparcamientosOcupados--;
	}
	
	/**
	 * Aumenta el contador encargador de dar los permisos a las barreras conrrespondiente.
	 * EJ: primero la n1, segundo la n2, tercero la n3, despues la 1 de nuevo y asi...
	 */
	private synchronized void aumentarContador() {
		contador++;
		if (contador >= 2)
			contador = 0;
	}
	 
}
