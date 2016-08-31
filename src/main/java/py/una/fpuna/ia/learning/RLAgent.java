/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.una.fpuna.ia.learning;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class RLAgent {

	private HashMap<String, Double> lookupTable;
	private int[][] tablero;
	private double alpha;
	private boolean entrenar;
	private int gameResult;
	private int N;
	private int[][] lastTablero; //para guardar el estado anterior donde estuvo el agente
	private int jugadorAgente = 1;
        private double qRate = 0.1;
	
	public RLAgent(int N) {
		
		this.N = N;
		lookupTable = new HashMap<String, Double>();		
		reset(true);
	}
	
	public void reset(boolean entrenar) {
		
		tablero = new int[3][3];
		lastTablero = new int[3][3];
		this.entrenar = entrenar;
		this.gameResult = 0;
	}
	
	private int calculateResult(int[][] tablero) {
		
		//1:gana jugador x
		//2:gana jugador o
		//3:empate
		//0:no hay ganador
		
		int jugador = 1;
		int contrario = 2;
		if((tablero[0][0] == tablero[0][1] && tablero[0][1] == tablero[0][2] && tablero[0][0] == jugador)
			|| (tablero[1][0] == tablero[1][1] && tablero[1][1] == tablero[1][2] && tablero[1][0] == jugador)
			|| (tablero[2][0] == tablero[2][1] && tablero[2][1] == tablero[2][2] && tablero[2][0] == jugador)
			|| (tablero[0][0] == tablero[1][0] && tablero[1][0] == tablero[2][0] && tablero[0][0] == jugador)
			|| (tablero[0][1] == tablero[1][1] && tablero[1][1] == tablero[2][1] && tablero[0][1] == jugador)
			|| (tablero[0][2] == tablero[1][2] && tablero[1][2] == tablero[2][2] && tablero[0][2] == jugador)
			|| (tablero[0][0] == tablero[1][1] && tablero[1][1] == tablero[2][2] && tablero[0][0] == jugador)
			|| (tablero[0][2] == tablero[1][1] && tablero[1][1] == tablero[2][0] && tablero[0][2] == jugador)
		) {
			
			return jugador; //gana jugador
		} else if((tablero[0][0] == tablero[0][1] && tablero[0][1] == tablero[0][2] && tablero[0][0] == contrario)
				|| (tablero[1][0] == tablero[1][1] && tablero[1][1] == tablero[1][2] && tablero[1][0] == contrario)
				|| (tablero[2][0] == tablero[2][1] && tablero[2][1] == tablero[2][2] && tablero[2][0] == contrario)
				|| (tablero[0][0] == tablero[1][0] && tablero[1][0] == tablero[2][0] && tablero[0][0] == contrario)
				|| (tablero[0][1] == tablero[1][1] && tablero[1][1] == tablero[2][1] && tablero[0][1] == contrario)
				|| (tablero[0][2] == tablero[1][2] && tablero[1][2] == tablero[2][2] && tablero[0][2] == contrario)
				|| (tablero[0][0] == tablero[1][1] && tablero[1][1] == tablero[2][2] && tablero[0][0] == contrario)
				|| (tablero[0][2] == tablero[1][1] && tablero[1][1] == tablero[2][0] && tablero[0][2] == contrario)
			) {
			
			return contrario; //pierde jugador
		} else {
			
			for(int i=0; i < tablero.length; i++) {
				for(int j=0; j < tablero[0].length; j++) {					
					if(tablero[i][j] == 0) { //esta vacio
						//no es empate
						return 0;
					}
				}
			}
			
			return 3; //es empate
		}
	}
	
	private double calculateReward(int[][] tablero, int jugador){
		
		
		//(1 % 2) + 1 = 2
		//(2 % 2) + 1 = 1
		int contrario = (jugador % 2) + 1;
		
		int result = calculateResult(tablero);
		if(result == jugador) {
			
			return 1.0;
		} else if(result == contrario) {
			
			return 0.0;
		} else if(result == 3) {//empate
		
			return 0.0;
		} else {//no hay ganador
			
			return getProbability(tablero);
		}
	}
	
	private double getProbability(int[][] tablero) {
		
		String tableroSerializado = "";
		for(int i=0; i < tablero.length; i++) {
			for(int j=0; j < tablero[0].length; j++) {
				
				tableroSerializado += tablero[i][j];
			}
		}
		//si aun no contiene la tabla, insertar con valor inicial 0.5
		if(!lookupTable.containsKey(tableroSerializado))
			lookupTable.put(tableroSerializado, 0.5);			
		
		return lookupTable.get(tableroSerializado);	
	}
	
	private String serializarTablero(int[][] tablero) {
		
		String tableroSerializado = "";
		for(int i=0; i < tablero.length; i++) {
			for(int j=0; j < tablero[0].length; j++) {
				
				tableroSerializado += tablero[i][j];
			}
		}
		
		return tableroSerializado;
	}
	
	private int[][] deserializar(String tableroSerializado) {
		
		int valor;
		int[][] tablero = new int[3][3];
		for(int i=0; i < tableroSerializado.length(); i++) {
			
			valor = Integer.parseInt(tableroSerializado.charAt(i)+"");
			tablero[i/3][i%3] = valor;			
		}
		
		return tablero;
	}
	
	private void updateProbability(int[][] tablero, double nextStateProb, int jugador) {
		
		double prob = calculateReward(tablero, jugador);
		//if(lookupTable.containsKey(tableroSerializado))
		//	prob = lookupTable.get(tableroSerializado);		
		
		prob = prob + alpha * (nextStateProb - prob);
		
		String tableroSerializado = serializarTablero(tablero);
		lookupTable.put(tableroSerializado, prob);
	}
	
	private void jugar(int jugador){
		
		double prob;
		int row = 0, column = 0;
		double maxProb = Integer.MIN_VALUE;
		for(int i=0; i < tablero.length; i++) {
			for(int j=0; j < tablero[0].length; j++) {
				
				if(tablero[i][j] == 0) { //esta vacio				
					
					tablero[i][j] = jugador;
					prob = calculateReward(tablero, jugador);
					if(prob > maxProb) {
						
						maxProb = prob;
						row = i;
						column = j;
					}
					tablero[i][j] = 0;
				}
			}
		}
		
		//entrenar
		if(entrenar)
			updateProbability(lastTablero, maxProb, jugador);
			//updateProbability(tablero, maxProb, jugador);
		
		//aplicar jugada
		tablero[row][column] = jugador;	
		
		//actualizar ultimo tablero
		copiarTablero(tablero, lastTablero);	
	}
	
	private void jugarRandom(int jugador) {
		
		ArrayList<Integer> filas =  new ArrayList<Integer>();
		ArrayList<Integer> columnas =  new ArrayList<Integer>();
		for(int i=0; i < tablero.length; i++) {
			for(int j=0; j < tablero[0].length; j++) {
				
				if(tablero[i][j] == 0) { //esta vacio
					filas.add(i);
					columnas.add(j);
				}
			}
		}
		
		int random = (int)(Math.random() * filas.size());		
		
		//aplicar jugada
		tablero[filas.get(random)][columnas.get(random)] = jugador;
		
		//si es el agente, actualizar ultimo tablero
		if(jugador == this.jugadorAgente)
			copiarTablero(tablero, lastTablero);
	}
	
	private void jugarHumano(int jugador) {
		
		printTablero();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String linea = "";
		try{
			linea = br.readLine();
		}catch(Exception ex){			
			ex.printStackTrace();
			System.exit(1);
		}
                
                // se recibe una jugada entre 1 y 9, mismo layout que el teclado numerico
                // |7|8|9|
                // -------
                // |4|5|6|
                // -------
                // |1|2|3|
		int posicion = Integer.parseInt(linea) - 1; 
                int xArray[] = {2,2,2,1,1,1,0,0,0}; 
                int yArray[] = {0,1,2,0,1,2,0,1,2};
                int x = xArray[posicion];
		int y = yArray[posicion];
		
		//aplicar jugada
		tablero[x][y] = jugador;
		
	}
	
	public void printTable() {
		
		for(String key : lookupTable.keySet()) {
			
			System.out.println("Tablero: " + key + ", prob: " + lookupTable.get(key));
			printTablero(deserializar(key));
		}
	}
	
	public void printTablero() {
		
		printTablero(this.tablero);
	}
	
	public void printTablero(int[][] tablero) {
		
		System.out.println();
		System.out.print("-------");
		System.out.println();
		for(int i=0; i < tablero.length; i++) {
			for(int j=0; j < tablero[0].length; j++) {
				
				System.out.print("|");
				if(tablero[i][j] == 1)
					System.out.print("x");
				else if(tablero[i][j] == 2)
					System.out.print("o");
				else
					System.out.print(" ");
			}
			System.out.print("|");
			System.out.println();
			System.out.print("-------");
			System.out.println();
		}
	}
	
	private void copiarTablero(int[][] tableroOrigen, int[][] tableroDestino) {
		
		for(int i=0; i < tableroOrigen.length; i++) {
			for(int j=0; j < tableroOrigen[0].length; j++) {
				
				tableroDestino[i][j] = tableroOrigen[i][j];
			}
		}		
	}
	
	public int getN() {
		
		return this.N;
	}
	
	public void setN(int N) {
		
		this.N = N;
	}
	
	public double getAlpha() {
		
		return this.alpha;
	}
	
	public void setAlpha(double alpha) {
		
		this.alpha = alpha;
	}
	
	public void updateAlpha(int currentGame){
		
		this.alpha = 0.5 - 0.49 * currentGame / this.N;
	}
	
	public int getResult() {
		
		return this.gameResult;
	}
	
	public void jugarVsRandom() {
		
            int jugador = this.jugadorAgente;
            int contrario = (jugador % 2) + 1;
            int turno = 1;
            int jugadas = 9;
            double q;
            do{
                    if(turno == jugador) {
                            q = Math.random();
                            if(q <= qRate || !this.entrenar)
                                    jugar(jugador);
                            else
                                    jugarRandom(jugador);
                    } else {
                            jugarRandom(contrario);				
                    }			

                    //actualizar resultado
                    gameResult = calculateResult(tablero);
                    if(gameResult > 0){ //ya hay resultado
                            if(gameResult != jugador && entrenar) //perdimos, actualizar tablero
                                    updateProbability(lastTablero, calculateReward(tablero, jugador), jugador);
                            break;
                    }

                    turno = 2 - turno + 1;
                    jugadas--;			
            }while(jugadas > 0);
		
	}	
        
        public void setQRate(double qRate){
        
            this.qRate = qRate;
        }
	
	public void jugarVsHumano() {
		
		int jugador = this.jugadorAgente;
		int contrario = (jugador % 2) + 1;
		int turno = 1;
		int jugadas = 9;
		do{
			if(turno == jugador) {
				jugar(jugador);
			} else {
				jugarHumano(contrario);				
			}			
			
			//actualizar resultado
			gameResult = calculateResult(tablero);
			if(gameResult > 0){ //ya hay resultado
				if(gameResult != jugador && entrenar) //perdimos, actualizar tablero
					updateProbability(lastTablero, calculateReward(tablero, jugador), jugador);
				break;
			}
			
			turno = 2 - turno + 1;
			jugadas--;			
		}while(jugadas > 0);
		
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
            int trainingCount = 100000;
            int humanTrainingCount = 1;
            double totalGamesCount = 100000.0;            
            int totalExperiments = 1;
            //Double qRates[] = {0.1, 0.2, 0.3, 0.4, 0.5};
            Double qRates[] = {0.5};
            for(int q=0; q<qRates.length; q++){
                
                double winsRatioAcum = 0;
                double lossesRatioAcum = 0;
                double drawsRatioAcum = 0;
                for(int k=0; k<totalExperiments; k++) {

                    RLAgent ag = new RLAgent(trainingCount);
                    ag.setQRate(qRates[q]);
                    for(int i=0; i < ag.getN(); i++) {
                            ag.reset(true);
                            ag.updateAlpha(i);
                            ag.jugarVsRandom();			
                    }

                    ag.setN(humanTrainingCount);
                    ag.setAlpha(0.7);
                    for(int i=0; i < ag.getN(); i++) {
                            ag.reset(true);
                            //ag.updateAlpha(i);
                            ag.jugarVsHumano();
                    }

                    System.out.println(">>>>>>>>>>>>>>> AFTER TRAINING ");
                    ag.printTable();

                    int wins = 0;
                    int losses = 0;
                    int draws = 0;
                    int contrario = 2 - ag.jugadorAgente + 1;
                    for(int i=0; i < totalGamesCount; i++) {

                            ag.reset(false);
                            ag.jugarVsRandom();

                            if(ag.getResult() == ag.jugadorAgente)
                                wins++;
                            else if(ag.getResult() == contrario)
                                losses++;
                            else
                                draws++;
                    }

                    /*
                    System.out.println("Wins: " + wins + ", Losses: " + losses + ", Draws: " + draws);
                    System.out.println("Ratio W/T: " + wins/totalGamesCount);
                    System.out.println("Ratio L/T: " + losses/totalGamesCount);
                    System.out.println("Ratio D/T: " + draws/totalGamesCount);
                    */
                    winsRatioAcum += wins/totalGamesCount;
                    lossesRatioAcum += losses/totalGamesCount;
                    drawsRatioAcum += draws/totalGamesCount;
                }
                System.out.println(">>>>>>>>>>>>>>> RATIO AVG, Q RATE: " + qRates[q]);
                System.out.println("Ratio Avg W/T: " + winsRatioAcum/totalExperiments);
                System.out.println("Ratio Avg L/T: " + lossesRatioAcum/totalExperiments);
                System.out.println("Ratio Avg D/T: " + drawsRatioAcum/totalExperiments);
            }
	}
}