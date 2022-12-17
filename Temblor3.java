import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

/*
	Cont de Temblor2. Usa BD completa del Sismologico para calcular probabilidades reales
	1900-2022, 260 mil registros
	
	prob dia con sismo grado X != prob un sismo sea grado X
	prob 1 dia c/sismo 6+ 	= (176+45)/26562 = 0.00832 de acuerdo a 1950+	A
							= (74+12)/ 8300 =	0.01   de acuerdo a 2000+
	
	dias c/cadena potencial desde 1950: 26562 - 37
	para cada dia, la prob de iniciar una cadena es A
	ls prob de que le siga una cadena tamaño 2+ de sismos 6+ es B
	Bin(k>1; N=37, p=0.008) = 0.0354248691
	Prob 1 cadena en 1 dia es A*B = (0.008*0354248691=0.00028)
	Num desde 1950 es suma, *26562 = 7.52 real 12 (14?)
	
	para 6.5
	A = 123/26562=0.00463
	B = Bin(k>1; N=37, p=0.00463) = 0.012822039
	A*B = 0.000059366041496
	A*B*26562 = 1.58	real 4
	
	para 7
	A = 45/26562 = 0.00169
	B = Bin( k>1; N=37, p=0.00169)=0.00184
	A*B = 3.098e-6
	A*B*26562 = 0.08	real 1
	
	para 5
	A = (1456+176+45)/26562=0.063135
	B = 0.68717
	A*B = 0.043385
	*26562 = 1153		real 1271
	
	----

	cadena length 2+, 6+ encuentra  desde 1950
	A = 0.00832
	B = Bin( k=1; N=37, p=0.00832)=0.266
	A*B*26562 = 58.78 real 0			igual cadenas==2
	
	cadena length==2, 7+ encuentra 0 desde 1950
	A = 45/26562 = 0.00169
	B = Bin( k=1; N=37, p=0.00169)=0.05883606244
	A*B*26562 = 2.64 real 0
	
	---
	
	
	
*/
public class Temblor3 { 

	// static int a_init = 1950;	// start day from DB and computations
	
	public static void main(String[] args) throws Exception		 {
		int meta = Integer.parseInt(args[0]);			// longitud exacta de la cadena
		int vent = Integer.parseInt(args[1]);			// ventana 38
		//boolean geq = args[3].equals("true");			// geq: busco Bin(k>= ) y no solo Bin(k= )
		int a_init = Integer.parseInt(args[2]);			// año inicial
		
		int d_1950 = diasDesde(a_init+"-01-01","2022-09-22");
		double dmax = diasDesde(a_init+"-01-01","1985-09-22"); // ultimo dia disponible para comparar cadenas en ventana

		String file = "SSNMX_catalogo_19000101_20220922.csv";
		String path = "C:/Users/arrio/Bases de datos/";
		float[][] m = lee(path+file,",",a_init);		// Año inicial registros, debe ser parametro?
	
		double c4 = count(m,a_init,4d,5d) ; double p4 = c4/d_1950;		double b4 = binomial(meta-1,vent-1,p4)*p4;
		double c5 = count(m,a_init,5d,6d) ; double p5 = c5/d_1950;		double b5 = binomial(meta-1,vent-1,p5)*p5;
		double c6 = count(m,a_init,6d,7d) ; double p6 = c6/d_1950;		double b6 = binomial(meta-1,vent-1,p6)*p6;
		double c7 = count(m,a_init,7d,10d); double p7 = c7/d_1950;		double b7 =	binomial(meta-1,vent-1,p7)*p7;
		double c47 =count(m,a_init,4d,10d); double p47 = c47/d_1950;	double b47 =binomial(meta-1,vent-1,p47)*p47;
		double c57 =count(m,a_init,5d,10d); double p57 = c57/d_1950;	double b57 =binomial(meta-1,vent-1,p57)*p57;
		double c67 =count(m,a_init,6d,10d); double p67 = c67/d_1950; 	double b67 =binomial(meta-1,vent-1,p57)*p67;

		boolean[][][]  m4 = alta(m,a_init,4d,5d);  double   ctot4 = regula( m4,a_init,vent,meta,false, "1985-09-22" , false); 
		boolean[][][]  m5 = alta(m,a_init,5d,6d);  double   ctot5 = regula( m5,a_init,vent,meta,false, "1985-09-22" , false);
		boolean[][][]  m6 = alta(m,a_init,6d,7d);  double   ctot6 = regula( m6,a_init,vent,meta,false, "1985-09-22" , false);
		boolean[][][]  m7 = alta(m,a_init,7d,10d); double   ctot7 = regula( m7,a_init,vent,meta,false, "1985-09-22" , false);
		boolean[][][] m47 = alta(m,a_init,4d,10d);  double ctot47 = regula(m47,a_init,vent,meta,false, "1985-09-22", false);
		boolean[][][] m57 = alta(m,a_init,5d,10d);  double ctot57 = regula(m57,a_init,vent,meta,false, "1985-09-22", false);
		boolean[][][] m67 = alta(m,a_init,6d,10d);  double ctot67 = regula(m67,a_init,vent,meta,false, "1985-09-22", false);

		System.out.println("Dias desde "+a_init+"/01/01: "+d_1950);	
		System.out.println("Dias desde "+a_init+"/01/01: a 1985/09/22 "+dmax);
		System.out.println("Ventana: "+vent);
		System.out.println("Meta: "+meta);
		System.out.println();
		System.out.println("magnitud / cuenta / prob dia / prob c/binomial / predichos / reales ");
		System.out.println();

		System.out.println("[4,5): "+ c4 +" / "+ p4+" / "+ b4 + " / " + b4*dmax+" / "+  ctot4);
		System.out.println("[5,6): "+ c5 +" / "+ p5+" / "+ b5 + " / " + b5*dmax+" / "+  ctot5);
		System.out.println("[6,7): "+ c6 +" / "+ p6+" / "+ b6 + " / " + b6*dmax+" / "+  ctot6);
		System.out.println();
		System.out.println("[4,_): "+ c47+" / "+p47+" / "+ b47 + " / " +b47*dmax+" / "+ ctot47);
		System.out.println("[5,_): "+ c57+" / "+p57+" / "+ b57 + " / " +b57*dmax+" / "+ ctot57);
		System.out.println("[6,_): "+ c67+" / "+p67+" / "+ b67 + " / " +b67*dmax+" / "+ ctot67);
		System.out.println("[7,_): "+ c7 +" / "+ p7+" / "+ b7 + " / " + b7*dmax+" / "+  ctot7);
	
		// int d_1900 = new Long( ChronoUnit.DAYS.between(date0, date3) ).intValue() + 1;
		// System.out.println("Dias desde 1900/1/1: "+d_1900);	// 1900-01-01
		// int d_2000 = new Long( ChronoUnit.DAYS.between(date2, date3) ).intValue();
		// System.out.println("Dias desde 2000/1/1: "+d_2000);	// 2000-01-01
		// System.out.println();				

	} // main
	
	/*
	// Da de alta sismos en matrices ver 1: solo limite inferior
	static boolean[][][] alta(float[][] m, double liminf) {
		boolean[][][] m7 = new boolean[2022-a_init+1][12][31];	// 7+, 1950-2022 x days therein
		for (int i=0; i<m.length; i++) {
			int a = new Float(m[i][0]).intValue();
			int e = new Float(m[i][1]).intValue();
			int d = new Float(m[i][2]).intValue();
			float magnitud = m[i][3];
			if (e==2 && d==29) continue;
		if (magnitud>=liminf) m7[a-a_init][e-1][d-1] = true;	//
		}
		return m7;
	}
	*/
		   
	// Da de alta sismos en matrices ver 1: limite inf + sup
	static boolean[][][] alta(float[][] m, int a_init, double liminf, double limsup) {
		boolean[][][] m7 = new boolean[2022-a_init+1][12][31];	// 7+, 1950-2022 x months x days therein
		for (int i=0; i<m.length; i++) {
			int a = new Float(m[i][0]).intValue();
			int e = new Float(m[i][1]).intValue();
			int d = new Float(m[i][2]).intValue();
			float magnitud = m[i][3];
			if (e==2 && d==29) continue;
			if (magnitud>=liminf && magnitud<limsup) m7[a-a_init][e-1][d-1] = true;
		}
		return m7;
	}
	// reverts date to string 007 0012
	static String d2s(int a, int m, int d) {
		String ms = "00" + m; ms = ms.substring(ms.length()-2,ms.length());
		String ds = "00" + d; ds = ds.substring(ds.length()-2,ds.length());
		return a + "-" + ms + "-" + ds;
	}
	
	// days w/several quakes count as 1
	// input matrix, year, min magnitude, max magnitude
	static double count(float[][] m, int a, double m0, double m1) {
		double c = 0;
		float xy = 0; float xm = 0; float xd = 0; 	// to keep track of days already counted
		for (int i=0; i<m.length; i++)
			if (m[i][0]>=a && m[i][3]>=m0 && m[i][3]<m1 &&	// pass the line
				!(m[i][0]==xy && m[i][1]==xm && m[i][2]==xd) ) {// not counted yet
				c++;
				xy = m[i][0]; xm = m[i][1]; xd = m[i][2];
			}
		return c;
	}
	
	// Pesky bug. Solo puedo comparar contra [0,35] años 
	// [36,72] son 37 años y ya no alcanzo
	// Limite: 22 sept 1985

	static double regula(boolean[][][] m7,int a_init,int p, int meta, boolean geq, String fmax, boolean pr) { // p 38 meta 3
		String[] ff = fmax.split("-"); 
		int amax = Integer.parseInt(ff[0]) - a_init; 
		int mmax = Integer.parseInt(ff[1]) - 1; 
		int dmax = Integer.parseInt(ff[2]) - 1;
		double c = 0;
		for (int a=0; a<m7.length; a++) {
			for (int e=0; e<12; e++) {
				for (int d=0; d<31; d++) {
					if ( (a==amax) && (e==mmax) && (d>dmax) ) return c;	// past right edge
					int i = e*31+d;	
					if (m7[a][e][d] ) {
						String r = (a_init+a) + "-" + (e+1) + "-" + (d+1);
						int c2 = 0;
						for (int j=1; j<p; j++) {		// m[a+1]... m[a+37]
							//System.out.println(a+" "+e+" "+d+" "+j);
							if ((a+j)<m7.length && m7[a+j][e][d]){ 
								c2++;
								r = r + "/" + (a_init+a+j) + "-" + (e+1) + "-" + (d+1);
							}
						}
						
						// Meta: busco 1 mas (c) para complementar 1a, ie total 2 etc 
						// Suma de c depende de cuantas cadenas separadas se generan: ABC son dos cadenas AB y BC de long 2
						if (!geq) {		// ie eq, solo cadenas exactamente de long meta-1
							if (c2==(meta-1)) { 
								c++; if (pr) System.out.println(r); 
							} //c2+", "+c); }
						} else {		// toma lo que llegue y suma subcadenas
							if (c2>=(meta-1)) c=c+c2-(meta-1)+1;// busco de 2	x d d , sumo 2, x d d d sumo 3 OK
																// busco de 3	x d d d sumo 2 OK
						}
					}
					
				} // d
			}
		}
		return c;
	} // regula

	static double binomial(int k, int n, double p) throws Exception {
		return binomial_coef(n,k) * Math.pow(p,k) * Math.pow(1-p,n-k);
	}
	
	static double binomial_coef(int n, int k) throws Exception {
		double log = log_factorial(n) - ( log_factorial(k) + log_factorial(n-k) );
		return Math.exp(log);
	}	

	static double log_factorial(int n) throws Exception {
		double s = 0; 
		if (n==0) return 0;
		if (n<0) throw new Exception("Log wrong value");
		for (int i=1; i<=n; i++) s = s+Math.log(i);
		return s;
	}

	/*
	// OJO Muy intervenidas, checar antes de usar
	static double bin(float[][] m, double umbral, int meta, int ventana, boolean geq, int dias) throws Exception  {
		double count = count(m,a_init,umbral,10d) ;
		double p = count/dias;
		double b = 0;		
		if (geq) { for (int i=0; i<(meta-1); i++) b = b + binomial(i,ventana,p); b = 1 - b; }
		else b = binomial(meta-1,ventana,p) ; // exacta
	
		return p*b*dias; //b*count;		
	}
	
	// Formula was correct, interpretation was wrong. This formula FORCES an element in the last place
	// Temblor2 has the example. 
	// meta=3 ventana=37
	static double bin2(float[][] m, double umbral, int meta, int ventana, boolean geq, int dias) throws Exception  {
		double count = count(m,a_init,umbral,10d) ;
		double p = count/dias;
		double b = 0;		
		if (geq) { for (int i=0; i<(meta-2); i++) b = b + binomial(i,ventana,p); b = 1 - b; }
		else b = binomial(meta-2,ventana-1,p) ; // exacta
		return p*p*b*dias; //b*count;		
	}
	*/


	
	public static float[][] lee(String fileName, String separator, int min) throws Exception {
		String[] ss = null;

		BufferedReader in1 = new BufferedReader (new InputStreamReader (new FileInputStream(fileName)));
		String data = in1.readLine(); 
		in1.readLine();
		in1.readLine();
		in1.readLine();
		in1.readLine();
		int ren = 0; 
		while ( (data=in1.readLine())!=null ) {
			ss = data.split(separator); 
			try {
				Float.parseFloat(ss[2]);			// excepcion
				String[] f = ss[0].split("-"); 		// fecha
				int a = Integer.parseInt(f[0]);		// año
				if (a>=min) ren++;					// skip	otherwise			
			} catch (Exception e) {}
		}
		in1.close();
		// System.out.println(ren);	// 242984
		
		in1 = new BufferedReader (new InputStreamReader (new FileInputStream(fileName)));
		in1.readLine(); // header
		in1.readLine();
		in1.readLine();
		in1.readLine();
		in1.readLine();
		float[][] r = new float[ren][4];	// cols: año / mes / dia / magnitud
		int i = 0;
		while (i<r.length) {
			ss = in1.readLine().split(separator);
			try {
				r[i][3] = Float.parseFloat(ss[2]);	// exception
				String[] f = ss[0].split("-"); 		// fecha
				int a = Integer.parseInt(f[0]);		// año
				if (a>=min) {						// skip	otherwise			
					r[i][0] = a ;
					r[i][1] = Integer.parseInt(f[1]);
					r[i][2] = Integer.parseInt(f[2]); 
					i++;
				}
			} catch (Exception e) {}
		}
		in1.close();
		return r;
	} // lee
	
	static DateTimeFormatter dtf = null;
	static Temporal dzero = null;
	
	static int diasDesde (String fini, String ffin) {
		if (dtf==null) dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd"); 
		Temporal d0 = LocalDate.parse(fini, dtf); 
		Temporal d1 = LocalDate.parse(ffin, dtf);
		return new Long( ChronoUnit.DAYS.between(d0, d1) ).intValue();
	}
	
} // class
