import java.util.*;
import java.io.*;
/*
	Modela la probabilidad de "los 3 temblores del 19 de septiembre", tratando de reproducir
	de la manera m�s cercana posible temblores Y el proceso social

	Uso BD https://springernature.figshare.com/articles/dataset/Earthquake_catalog_1787-2018_for_Mexico/9696305?backTo=/collections/An_updated_and_unified_earthquake_catalog_1787-2018_for_seismic_hazard_assessment_studies_in_Mexico/4492763
	
	descrita en 
	https://www.nature.com/articles/s41597-019-0234-z
	
	El modelo funciona as�

	- Toma aleatoriamente un temblor severo (7+) y lo hace "significativo". Marca la fecha 
		La condici�n es que sea un temblor 7+ y gane un volado con p=1/2 (b�sicamente, que afecte DF)
	
	- Despu�s, corre repetidamente bloques de 37 a�os posteriores buscando bloques donde haya exactamente
		dos temblores "significativos" m�s. La 2a significaci�n es menos exigente: una vez que la gente se da
		cuenta que repite la 1a fecha, "encontrar un signo" va a ser mucho m�s f�cil (lo puse en p = 8/10).
		Nota: BD solo trae sismos intensidad 4+ 
		Esta creo es la situaci�n real: despu�s de un fen�meno impactante (1985), la gente est� predispuesta
		a ver "signos"
		
		Resultado: con 1 mill�n de simulaciones, la probabilidad es de 0.00162
	
		Una 2a corrida pidiendo, en vez de "significaci�n", que fuera un temblor 7+. No creo que modele
		tan bien lo que pas�, pero mucha gente est� marcando "tres temblores de 7 o m�s". p=0.00176
		
	PD El c�digo es pedag�gico pero hace much�simo trabajo en balde. Es una manera complicada de hacer binomiales, b�sicamente. Se puede llegar a esa conclusi�n tambi�n por inspecci�n de c�digo: 2o loop d�as no se necesita, ergo el c�lculo de d�a especial tampoco se requiere.
	
	Haciendo las binomiales directas:
	Exactamente 2 temblores
	pt=((5160-2967)/(2018-2000+1))/365=0.316222			was 0.19 1950+
	pt7=12/(5160-2967)=	0.00547							was pt7=0.0099
	pt*pt7=0.00173			Bin=0.001876				* was 0.0019	Bin(k=2; n=37, p=0.0019) = 0.002249434629
	pt*psig2=0.25298		Bin=0.001571 !				* was 0.15368	Bin(k=2; n=37, p=0.15368) = 0.04575305282
	
	2 o mas temblores
	pt*pt7		Bin = 0.00189
	pt*psig2	Bin = 0.9997!	
*/
public class Temblor { 

	static double num_a = 2018-2000+1;			// num a�os consideracion
	static double num_t = 5160-2967;			// num temblores 2000+
	static double num_t_7 = 12 ;				// num temblores >= 7 2000+
	static double p_sig = 0.5d;					// proba de que 1er temblor se vuelva significativo (ademas de ser 7+)
	static double p_t   = (num_t / num_a)/365d;	// proba temblor en un dia
	static double p_t_7 = num_t_7 / num_t;		// proba dado un temblor, sea 7+
	static double p_sig_2 = 0.8d;				// proba temblor subsiguiente significativo (casi cualquiera)

	// arg 0: numero de veces que se corre la simulaci�n. Se us� nmax = 1000000
	public static void main(String[] args) {
	
		int nmax = Integer.parseInt(args[0]); 			// num experimentos
		int periodo = 37;								// periodo despues de 1er evento (1o: 1985, 2o y 3o: [1986,2022])
		Random r = new Random( System.currentTimeMillis() );
		
		// Fase 1: caza hasta encontrar dia significativo
		boolean fin = false;
		int d = 0;										// dia significativo
		while (!fin) {
			for (int i=0; i<365; i++) {
				boolean t = r.nextDouble() <= p_t;		// temblor?
				boolean u = r.nextDouble() <= p_t_7;	// fue 7+?
				boolean s = r.nextDouble() <= p_sig;	// significativo?
				if ( t && u && s ) { d = i; fin = true; break; }
			}
		}

		// Fase 2: corre a�os de bloque en bloque y cuenta cuantas veces cae temblor en dia significativo
		double ctot = 0;
		for (int n=0; n<nmax; n++) {
			double c = 0;
			for (int p=0; p<periodo; p++) {
				for (int i=0; i<365; i++) {					// innecesario, basta probar "el" dia
					if (i>d) break;
					boolean t = r.nextDouble() <= p_t;		// temblor?
					//boolean s = r.nextDouble() <= p_sig_2;	// significativo?	opcion 1
					boolean s = r.nextDouble() <= p_t_7;	// fue 7+?			opcion 2
					if ( t && s && (d == i)) c++;			// innecesario chequeo dia
				}
			}
			if (c>=2) ctot++;								//  >= ser�a otra opci�n, para p alta es un problema no pnerla
		}
		
		System.out.println(ctot/nmax);
		
	}// main
	
} // class
