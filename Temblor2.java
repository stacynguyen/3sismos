import java.util.*;
import java.io.*;
/*
	Cont de Temblor. Simpleton edition. Distribuye temblores aleatoriamente en cien años y luego
	cuenta regularidades. That's it.
	
	A run nmax = 1000 takes ~5 secs 
	
	C:\java>java Temblor2 7.0 3 38 false 100000
	Dias desde 1950/1/1: 26562
	p: 0.00169414953693246
	Avg chains per 100 thou: 0.3059			3.059 per million days or 3.059 in 27.38 centuries or 1 in 8.95 centuries +++
	So, to achieve p>1/2 you need half of that time, about 450 years.
	
	Formula p * Bin(k=2; n=37, p) * 1,000,000 = 0.00169414953693246*0.001801375621*1000000=3.05
	
	Coda, una arruga: Bin encuentra 2 soluciones donde quiera que sean, incl posibilidades de ultima
	posicion (año 2022) esté vacía. Hay que pensar si sólo deben considerarse combinaciones donde la
	ultima esta asignada.
	
	Corriendo la CORRECCION* que obliga a un temblor en la ultima posicion, obtengo
	
	C:\java>java Temblor2 7.0 3 38 false 100000
	Dias desde 1950/1/1: 26562
	p: 0.00169414953693246
	Avg chains per 100 thou: 0.01618 	or 1.618 per 10 mill days or 1.618 in 274.79 centuries or 1 in 169 centuries
	19x less likely than +++
	To achieve p>1/2 you need 84.5 centuries

	Formula p * p * Bin(k=1; n=36, p) * 10,000,000 = 0.00169414953693246*0.00169414953693246*0.05747522521*10000000=1.6496
	
	Necesitas 100 mil para acercarte decentemente: 1.63 redondeado a 2 "decimales" en ~6 minutos
	
	Corriendo 10 000 000 experimentos (10^12 días), llego a 1.651 por 10 millones. Tardó como 8 horas en correr.
	(reality check: 100 veces 6 minutos es 600 minutos, o 10 horas)
	
	En general, se valida el cálculo por fórmula. Lo que pasa es que converge muy lentamente.

*/
public class Temblor2 { 

	public static void main(String[] args) throws Exception {

	int x = 1;
	while ( ((x==1)?1:0)>0 ) x = x-1;
	System.out.println(x);

		double umbral 	= 0;
		int meta 		= 0;
		int ventana 	= 0;
		boolean geq 	= false;
		int nmax 		= 0;
		double umbral_1 = 0;
		int a_init		= 0;
		
		umbral 		= Double.parseDouble(args[0]);		// ie 6.5, 7.0 etc
		umbral_1 	= Double.parseDouble(args[1]);		// ie 6.5, 7.0 etc
		meta 		= Integer.parseInt(args[2]);		// longitud exacta de la cadena
		ventana 	= Integer.parseInt(args[3]);		// 38 AUFPASSEN Temblor3 usa 37
		//geq 		= args[4].equals("true");			// geq: busco Bin(k>= ) y no solo Bin(k= )
		a_init 		= Integer.parseInt(args[4]); 		// año inicial
		nmax 		= Integer.parseInt(args[5]); 		// num experimentos
		
		Random r = new Random( System.currentTimeMillis() );

		int d_1950 = Temblor3.diasDesde(a_init+"-01-01","2022-09-22");
		int d_edge = Temblor3.diasDesde(a_init+"-01-01","1985-09-22");
		System.out.println("Dias desde "+a_init+"/1/1: "+d_1950);

		String file = "SSNMX_catalogo_19000101_20220922.csv";
		String path = "C:/Users/arrio/Bases de datos/";
		float[][] m = Temblor3.lee(path+file,",",a_init);		// Año inicial registros, debe ser parametro?

		double count = Temblor3.count(m,a_init,umbral,umbral_1) ;
		double p = count/d_1950;
		System.out.println("p: "+p);
		
		double ctot = 0;
		for (int i=0; i<nmax; i++) {
			ctot = ctot + run(p,meta,ventana,geq,r);
		}
		
		double chains = ctot/nmax; 						// chains per 100 thou days
		double p_beg_chain = chains/100000d ;			// prob a day is beginning of chain
		double p_e_in_chain = chains*meta/100000d;		// prob earthquake day is part of chain
		double p_any_in_chain = chains*ventana/100000d;	// prob any day is part of chain

		
		System.out.println("Avg chains per 100 thou days: "+chains);
		System.out.println("Expected chains through 2022-09-22: "+ chains*d_1950/100000d ) ;
		System.out.println("Expected chains through 1985-09-22: "+ chains*d_edge/100000d ) ;
		System.out.println("Prob a day is beginning of chain	  " + p_beg_chain 		);
		System.out.println("Prob earthquake day is part of chain  " + p_e_in_chain 		);
		System.out.println("Prob any day is part of chain         " + p_any_in_chain	);
	}
	
	public static double run(double p, int meta, int ventana, boolean geq, Random r) throws Exception {

		int exp_size = 100000;		// 100 thou		
		boolean[] exp = new boolean[exp_size];
		
		// Fase 1: siembra
		for (int i=0; i<exp_size; i++) {
			boolean t = r.nextDouble() <= p;
			if (t) exp[i] = true;
		}
		
		// Fase 2: investiga
		double c = 0;
		for (int i=0; i<exp_size; i++) {
			if (exp[i]) {
				String rr = i + "/";
				int c2 = 0;
				for (int j=1; j<ventana; j++) {
					if ((i+j)<exp_size && exp[i+j]){ 
						c2++;
						rr = rr + (i+j) + "/";
					}
				}			
				if (!geq) {		// ie eq, solo cadenas exactamente de long meta-1
					if (c2==(meta-1) 
								//&& (i+ventana-1)<exp_size && exp[i+ventana-1] // *** CORRECCION para aseg true ultima pos
								) c++;
				} else {		// toma lo que llegue y suma subcadenas
					if (c2>=(meta-1)) c=c+c2-(meta-1)+1;// busco de 2	x d d , sumo 2, x d d d sumo 3 OK
														// busco de 3	x d d d sumo 2 OK
				}
			}
		}
		//System.out.println("cadenas "+c+" en "+nmax);
		return c;
	}// run
	
	
} // class
