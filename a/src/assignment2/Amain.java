package assignment2;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.util.ArrayList;
import java.awt.*;

import javax.swing.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
public class Amain {
	
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/";
	static final String DISABLE_SSL = "?useSSL=false";
	
	
	public static void main (String[] agrs) throws ClassNotFoundException, SQLException{
		
		// Database credentials
		String User = JOptionPane.showInputDialog("Enter your SQL server username");
		String Pass = JOptionPane.showInputDialog("Enter your SQL server password");
		String name = JOptionPane.showInputDialog("Insert Database name");
		
		
		ArrayList<substations> sub = getsubstation(User , Pass , name); //input of the substations table to java 
		ArrayList<measurement> meas = getmeasure(User , Pass , name);	//input of the measurements table to java 
		ArrayList<measurement> anValues = getanValues(User , Pass , name);	//input of the measurements table to java 
		
		ArrayList<Double> times = new ArrayList<>(); // all the time values when measurements occurred
		
		for (int i = 0 ; i < meas.size() ; i++){ // create a list with the different time values that measurements occurred
			if ( (i%(sub.size()*2)) == 0) { times.add(meas.get(i).GetTime()); }
		}
		
		int connections [][] = new int[][]{{1,4},{2,8},{3,6},{4,5},{4,9},{5,6},{6,7},{7,8},{8,9}}; //connections table of system
		
// kNN clustering
		
	// initialization 
		
		double [][] k1 = new double[sub.size()][2]; //cluster 1 (high loading)
		double [][] k2 = new double[sub.size()][2]; //cluster 2 (low loading)
		double [][] k3 = new double[sub.size()][2]; //cluster 3 (disconnected line)
		double [][] k4 = new double[sub.size()][2]; //cluster 4	(disconnected generator)
		
		k1[0][0] = k2[0][0] = k3[0][0] = k4[0][0] = -1; // create a condition that can not be reached
		
		double v1 , an1 , v2 , an2 , dis , VL_5 , VL_7 , VL_9;
		boolean check = false;
		
		for(int i = 0 ; i < times.size() ; i++){
			check = false;
			if ( (k1[0][0] != -1) && (k2[0][0] != -1) && (k3[0][0] != -1) && (k4[0][0] != -1) ){ break; } // break if all 4 clusters have been initialized 
			for(int j = 0 ; j < connections.length ; j++){
				v1 = meas.get( i*2*sub.size() + ( 2* ( connections[j][0] - 1) )).GetValue();
				an1 = meas.get( i*2*sub.size() + ( 2* ( connections[j][0] - 1) + 1) ).GetValue();
				v2 = meas.get( i*2*sub.size() + ( 2* ( connections[j][1] - 1) )).GetValue();
				an2 = meas.get( i*2*sub.size() + ( 2* ( connections[j][1] - 1) + 1) ).GetValue();
				dis = VoltageD( v1 , an1 , v2 , an2); // ÄV calculation over system lines
				
				if (j < 3 && dis < 0.001){ // if low voltage drop in generator connecting lines-> cluster 4
					for (int k = 0 ; k < sub.size() ; k++){ // initialize cluster 
						k4[k][0] = meas.get(i*2*sub.size()+2*k).GetValue();
						k4[k][1] = meas.get(i*2*sub.size()+2*k+1).GetValue();
					}
					check = true;
					break;
				}
				if (dis > 0.28){ // if not cluster 4 and high voltage drop over line -> cluster 3
					for (int k = 0 ; k < sub.size() ; k++){
						k3[k][0] = meas.get(i*2*sub.size()+2*k).GetValue();
						k3[k][1] = meas.get(i*2*sub.size()+2*k+1).GetValue();
					}
					check = true;
					break;
				}
			}
			if (check){ continue; } // if clusters 3 or 4 continue to next measurement
			
			VL_5 = meas.get(i*2*sub.size()+8).GetValue();
			VL_7 = meas.get(i*2*sub.size()+12).GetValue();
			VL_9 = meas.get(i*2*sub.size()+16).GetValue();
			
			if( ((VL_5+VL_7+VL_9)/3) < 0.9){ // if not clusters 3 or 4 and low voltage on load buses -> cluster 1
				for (int k = 0 ; k < sub.size() ; k++){
					k1[k][0] = meas.get(i*2*sub.size()+2*k).GetValue();
					k1[k][1] = meas.get(i*2*sub.size()+2*k+1).GetValue();
				}
			}
			else{
				for (int k = 0 ; k < sub.size() ; k++){// else cluster 2
					k2[k][0] = meas.get(i*2*sub.size()+2*k).GetValue();
					k2[k][1] = meas.get(i*2*sub.size()+2*k+1).GetValue();
				}
			}
			
		}

		
//   k means clustering
		
		ArrayList<Integer> cbelongs = new ArrayList<>(); // which cluster each observation belongs to
		ArrayList<Integer> obs1 = new ArrayList<>(); // arrayLists including the observations(time measurements) 
		ArrayList<Integer> obs2 = new ArrayList<>(); // closest to each cluster
		ArrayList<Integer> obs3 = new ArrayList<>();
		ArrayList<Integer> obs4 = new ArrayList<>();
		
		
		for (int i = 0 ; i < times.size() ; i ++){ cbelongs.add(0); } // initialize the ArrayList 
		
		
		double dis1 , dis2 , dis3 , dis4;
		boolean check1;
		

				
		while (true){
			
			check1 = true;
			obs1 = new ArrayList<Integer>();  
			obs2 = new ArrayList<Integer>(); 
			obs3 = new ArrayList<Integer>();
			obs4 = new ArrayList<Integer>();
			
			
			for (int i = 0 ; i < times.size() ; i++){
				dis1 = dis2 = dis3 = dis4 = 0; // distances to each cluster
				
				for (int j = 0; j < sub.size() ; j++){
					dis1 = dis1 + Math.pow((k1[j][0] - meas.get(i*2*sub.size()+2*j).GetValue()), 2) //voltages
							+ Math.pow((k1[j][1] - meas.get(i*2*sub.size()+2*j+1).GetValue()), 2); //angles
					dis2 = dis2 + Math.pow((k2[j][0] - meas.get(i*2*sub.size()+2*j).GetValue()), 2) 
							+ Math.pow((k2[j][1] - meas.get(i*2*sub.size()+2*j+1).GetValue()), 2); 
					dis3 = dis3 + Math.pow((k3[j][0] - meas.get(i*2*sub.size()+2*j).GetValue()), 2) 
							+ Math.pow((k3[j][1] - meas.get(i*2*sub.size()+2*j+1).GetValue()), 2); 
					dis4 = dis4 + Math.pow((k4[j][0] - meas.get(i*2*sub.size()+2*j).GetValue()), 2) 
							+ Math.pow((k4[j][1] - meas.get(i*2*sub.size()+2*j+1).GetValue()), 2); 
				}

				if ( (Math.sqrt(dis1) < Math.sqrt(dis2)) && (Math.sqrt(dis1) < Math.sqrt(dis3)) && (Math.sqrt(dis1) < Math.sqrt(dis4)) ) { //check for the smallest distance
					obs1.add(i); 
					if (cbelongs.get(i) != 1) { check1 = false; } //check if this measurement was in this cluster before
					cbelongs.set(i , 1); // state to which cluster the measurement belongs now
					
				}
				if ( (Math.sqrt(dis2) < Math.sqrt(dis1)) && (Math.sqrt(dis2) < Math.sqrt(dis3)) && (Math.sqrt(dis2) < Math.sqrt(dis4)) ) { 
					obs2.add(i); 
					if (cbelongs.get(i) != 2) { check1 = false; }
					cbelongs.set(i , 2);
					
				}
				if ( (Math.sqrt(dis3) < Math.sqrt(dis2)) && (Math.sqrt(dis3) < Math.sqrt(dis1)) && (Math.sqrt(dis3) < Math.sqrt(dis4)) ) { 
					obs3.add(i); 
					if (cbelongs.get(i) != 3) { check1 = false; }
					cbelongs.set(i , 3);

				}
				if ( (Math.sqrt(dis4) < Math.sqrt(dis2)) && (Math.sqrt(dis4) < Math.sqrt(dis3)) && (Math.sqrt(dis4) < Math.sqrt(dis1)) ) { 
					obs4.add(i); 

					if (cbelongs.get(i) != 4) { check1 = false; }
					cbelongs.set(i , 4);				
				}	
			}
			
			for (int h = 0; h < sub.size() ; h++){// erase all previous inputs to clusters (to calculate average)
				k1[h][0] = k1[h][1] = k2[h][0] = k2[h][1] = k3[h][0] = k3[h][1] = k4[h][0] = k4[h][1] = 0; 	
			}
			
			for (int l = 0; l < obs1.size() ; l++){
				for (int h = 0; h < sub.size() ; h++){
					k1[h][0] = k1[h][0] + meas.get(obs1.get(l)*2*sub.size()+2*h).GetValue();// sum of V measurements in cluster1/substation 
					k1[h][1] = k1[h][1] + meas.get(obs1.get(l)*2*sub.size()+2*h+1).GetValue();// sum of è measurements in cluster1/substation
				}
			}
			
			for (int l = 0; l < obs2.size() ; l++){
				for (int h = 0; h < sub.size() ; h++){
					k2[h][0] = k2[h][0] + meas.get(obs2.get(l)*2*sub.size()+2*h).GetValue();
					k2[h][1] = k2[h][1] + meas.get(obs2.get(l)*2*sub.size()+2*h+1).GetValue();
				}
			}
			
			for (int l = 0; l < obs3.size() ; l++){
				for (int h = 0; h < sub.size() ; h++){
					k3[h][0] = k3[h][0] + meas.get(obs3.get(l)*2*sub.size()+2*h).GetValue();
					k3[h][1] = k3[h][1] + meas.get(obs3.get(l)*2*sub.size()+2*h+1).GetValue();
				}
			}
			
			for (int l = 0; l < obs4.size() ; l++){
				for (int h = 0; h < sub.size() ; h++){
					k4[h][0] = k4[h][0] + meas.get(obs4.get(l)*2*sub.size()+2*h).GetValue();
					k4[h][1] = k4[h][1] + meas.get(obs4.get(l)*2*sub.size()+2*h+1).GetValue();
				}
			}
			for (int h = 0; h < sub.size() ; h++){// calculate average for cluster values
				k1[h][0] = k1[h][0]/obs1.size();
				k1[h][1] = k1[h][1]/obs1.size();
				k2[h][0] = k2[h][0]/obs2.size();
				k2[h][1] = k2[h][1]/obs2.size();
				k3[h][0] = k3[h][0]/obs3.size();
				k3[h][1] = k3[h][1]/obs3.size();
				k4[h][0] = k4[h][0]/obs4.size();
				k4[h][1] = k4[h][1]/obs4.size();
			}

			if(check1){ break; } // if no measurement moved clusters then clustering complete
		}
		
//		System.out.println(obs1.size()+" meaurements belong in cluster 1");
//		System.out.println(obs2.size()+" meaurements belong in cluster 2");
//		System.out.println(obs3.size()+" meaurements belong in cluster 3");
//		System.out.println(obs4.size()+" meaurements belong in cluster 4");
		
		
		
///////////kNN algorithm////////////
		
		
		double cluster1 = 0;
		double cluster2 = 0;
		double cluster3 = 0;
		double cluster4 = 0;		
		
        ArrayList<Double> testtimes = new ArrayList<>(); // all the time values when measurements occurred in the testset
        ArrayList<Integer> testcbelongs = new ArrayList<>();
		for (int i = 0 ; i < anValues.size() ; i++){
			if ( (i%(sub.size()*2)) == 0) { testtimes.add(anValues.get(i).GetTime()); }
		}

		
		ArrayList<Integer> testobs1 = new ArrayList<>(); // testset measurements that belong in cluster 1
		ArrayList<Integer> testobs2 = new ArrayList<>();  
		ArrayList<Integer> testobs3 = new ArrayList<>(); 
		ArrayList<Integer> testobs4 = new ArrayList<>(); 

		ArrayList<Double[]> distanceTable = new ArrayList<Double[]>(); 
		
	
		for (int i = 0 ; i < times.size() ; i ++){ 
			distanceTable.add(new Double[3]);
		testcbelongs.add(0);} // initialize the ArrayList
		
	    double testdistance;
	
		for (int i = 0 ; i < testtimes.size() ; i++){

			int knn = 5;
			
			for (int j = 0; j < times.size() ; j++){
				testdistance = 0;		
				for (int m = 0; m < sub.size() ; m++){ // calculate distance between test value and measurement values
					testdistance = testdistance + Math.pow((anValues.get(i*2*sub.size()+2*m).GetValue()) - (meas.get(j*2*sub.size()+2*m).GetValue()), 2)
								   +Math.pow((anValues.get(i*2*sub.size()+2*m+1).GetValue()) - (meas.get(j*2*sub.size()+2*m+1).GetValue()), 2);	       	
				}
				
				testdistance = Math.sqrt(testdistance);
				
				distanceTable.get(j)[0]=times.get(j);
				distanceTable.get(j)[1]=testdistance; // add distance to table
				distanceTable.get(j)[2]=(double)cbelongs.get(j);// add cluster the measurement belongs in to table 
						
			}
				
			double[]clusters = new double[5];
			Quicksort sorter = new Quicksort(); 
			sorter.sort(distanceTable); // sort measurements by distance to test value 
				
				
			while(true) {
					
				clusters = getclusters(distanceTable,knn);	 
				
				if (clusters[4] == 1.0) {//if flag raised add an extra neighbor 
					knn++; 
					continue;
				}  
		 
				cluster1 = clusters[0];
				cluster2 = clusters[1];
			    cluster3 = clusters[2];
			    cluster4 = clusters[3];
					
			    if (cluster1 > cluster2 && cluster1 > cluster3 && cluster1 > cluster4) { // if most of k closest measurements belong in cluster 1 ->
			       	testobs1.add(testtimes.get(i).intValue()); // -> sort test measurement in cluster 1
			       	System.out.println("Test measurement: "+(i+1)+ " corresponding to test time " +testtimes.get(i) +" belongs to cluster: 1 -> High load rate during peak hours");
			       	testcbelongs.set(i , 1);
			    }
				
			    if (cluster2 > cluster1 && cluster2 > cluster3 && cluster2 > cluster4) {
			      	testobs2.add(testtimes.get(i).intValue());
			      	System.out.println("Test measurement: "+(i+1)+ " corresponding to test time " +testtimes.get(i) +" belongs to cluster: 2-> Low load rate during night");
			      	testcbelongs.set(i , 2);
			    }
				    
			    if (cluster3 > cluster1 && cluster3 > cluster2 && cluster3 > cluster4) {
			      	testobs3.add(testtimes.get(i).intValue());
			      	System.out.println("Test measurement: "+(i+1)+" corresponding to test time " +testtimes.get(i) +" belongs to cluster: 3 -> Disconnection of a line for maintenance");
			      	testcbelongs.set(i , 3);
			    }
				    
			    if (cluster4 > cluster1 && cluster4 > cluster2 && cluster4 > cluster3) {
			      	testobs4.add(testtimes.get(i).intValue());
			      	System.out.println("Test measurement: "+(i+1)+" corresponding to test time " +testtimes.get(i) +" belongs to cluster: 4 -> Shut down of generator for maintenance");
			      	testcbelongs.set(i , 4);
			    }
			    break;
			}
		}

//		
            ////////////   GUI  /////////
		
		
		

		ArrayList<Double[]> GUIkMEANS1 = new ArrayList<Double[]>();//Arraylists used to present data later in GUI
		ArrayList<Double[]> GUIkMEANS2= new ArrayList<Double[]>();
		ArrayList<Double[]> GUIkMEANS3= new ArrayList<Double[]>();
		ArrayList<Double[]> GUIkMEANS4= new ArrayList<Double[]>();
		
		
		for (int i = 0 ; i < times.size() ; i++){
		if(cbelongs.get(i)==1){
			Double[]GUIkMEANS1list = new Double[2];
			GUIkMEANS1list[0]=times.get(i);
			GUIkMEANS1list[1]=(double)cbelongs.get(i);
			GUIkMEANS1.add(GUIkMEANS1list);	}
					
		else if(cbelongs.get(i)==2){
			Double[]GUIkMEANS2list = new Double[2];
			GUIkMEANS2list[0]=times.get(i);
			GUIkMEANS2list[1]=(double)cbelongs.get(i);
			GUIkMEANS2.add(GUIkMEANS2list);}
		else if(cbelongs.get(i)==3){
			Double[]GUIkMEANS3list = new Double[2];
			GUIkMEANS3list[0]=times.get(i);
			GUIkMEANS3list[1]=(double)cbelongs.get(i);
			GUIkMEANS3.add(GUIkMEANS3list);}
		
		else if(cbelongs.get(i)==4){
			Double[]GUIkMEANS4list = new Double[2];
			GUIkMEANS4list[0]=times.get(i);
			GUIkMEANS4list[1]=(double)cbelongs.get(i);
			GUIkMEANS4.add(GUIkMEANS4list);
		}
		}


		String[][]GUIkMEANS1Table = new String[GUIkMEANS1.size()][2];        //////////////2D TABLES CREATION FOR JTABLE//////////////
		String[][]GUIkMEANS2Table = new String[GUIkMEANS2.size()][2];
		String[][]GUIkMEANS3Table = new String[GUIkMEANS3.size()][2];
		String[][]GUIkMEANS4Table = new String[GUIkMEANS4.size()][2];



		for (int i = 0 ; i < GUIkMEANS1.size() ; i++){
			GUIkMEANS1Table[i][0]= Double.toString(GUIkMEANS1.get(i)[0]);
			GUIkMEANS1Table[i][1]= Double.toString(GUIkMEANS1.get(i)[1]);
		}
		

		for (int i = 0 ; i < GUIkMEANS2.size() ; i++){
			GUIkMEANS2Table[i][0]=Double.toString(GUIkMEANS2.get(i)[0]);
			GUIkMEANS2Table[i][1]=Double.toString(GUIkMEANS2.get(i)[1]);
		}

		for (int i = 0 ; i < GUIkMEANS3.size() ; i++){
			GUIkMEANS3Table[i][0]=Double.toString(GUIkMEANS3.get(i)[0]);
			GUIkMEANS3Table[i][1]=Double.toString(GUIkMEANS3.get(i)[1]);
		}

		for (int i = 0 ; i < GUIkMEANS4.size() ; i++){
			GUIkMEANS4Table[i][0]=Double.toString(GUIkMEANS4.get(i)[0]);
			GUIkMEANS4Table[i][1]=Double.toString(GUIkMEANS4.get(i)[1]);
		}
		
		
		
		
		
		
		ArrayList<Double[]> GUIkNN1 = new ArrayList<Double[]>();//Arraylists used to present data later in GUI
		ArrayList<Double[]> GUIkNN2= new ArrayList<Double[]>();
		ArrayList<Double[]> GUIkNN3= new ArrayList<Double[]>();
		ArrayList<Double[]> GUIkNN4= new ArrayList<Double[]>();
		
		
		for (int i = 0 ; i < testtimes.size() ; i++){
		if(testcbelongs.get(i)==1){
			Double[]GUIkNN1list = new Double[2];
			GUIkNN1list[0]=testtimes.get(i);
			GUIkNN1list[1]=(double)testcbelongs.get(i);
			GUIkNN1.add(GUIkNN1list);	}
					
		else if(testcbelongs.get(i)==2){
			Double[]GUIkNN2list = new Double[2];
			GUIkNN2list[0]=testtimes.get(i);
			GUIkNN2list[1]=(double)testcbelongs.get(i);
			GUIkNN2.add(GUIkNN2list);}
		else if(testcbelongs.get(i)==3){
			Double[]GUIkNN3list = new Double[2];
			GUIkNN3list[0]=testtimes.get(i);
			GUIkNN3list[1]=(double)testcbelongs.get(i);
			GUIkNN3.add(GUIkNN3list);}
		
		else if(testcbelongs.get(i)==4){
			Double[]GUIkNN4list = new Double[2];
			GUIkNN4list[0]=testtimes.get(i);
			GUIkNN4list[1]=(double)testcbelongs.get(i);
			GUIkNN4.add(GUIkNN4list);
		}
		}
		


		String[][]GUIkNN1Table = new String[GUIkNN1.size()][2];        //////////////2D TABLES CREATION FOR JTABLE//////////////
		String[][]GUIkNN2Table = new String[GUIkNN2.size()][2];
		String[][]GUIkNN3Table = new String[GUIkNN3.size()][2];
		String[][]GUIkNN4Table = new String[GUIkNN4.size()][2];



		for (int i = 0 ; i < GUIkNN1.size() ; i++){
			GUIkNN1Table[i][0]= Double.toString(GUIkNN1.get(i)[0]);
			GUIkNN1Table[i][1]= Double.toString(GUIkNN1.get(i)[1]);
		}
		

		for (int i = 0 ; i < GUIkNN2.size() ; i++){
			GUIkNN2Table[i][0]=Double.toString(GUIkNN2.get(i)[0]);
			GUIkNN2Table[i][1]=Double.toString(GUIkNN2.get(i)[1]);
		}

		for (int i = 0 ; i < GUIkNN3.size() ; i++){
			GUIkNN3Table[i][0]=Double.toString(GUIkNN3.get(i)[0]);
			GUIkNN3Table[i][1]=Double.toString(GUIkNN3.get(i)[1]);
		}

		for (int i = 0 ; i < GUIkNN4.size() ; i++){
			GUIkNN4Table[i][0]=Double.toString(GUIkNN4.get(i)[0]);
			GUIkNN4Table[i][1]=Double.toString(GUIkNN4.get(i)[1]);
		}
		
		
		
		
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						GUI frame = new GUI(GUIkMEANS1Table , GUIkMEANS2Table , GUIkMEANS3Table , GUIkMEANS4Table , GUIkNN1Table , GUIkNN2Table , GUIkNN3Table , GUIkNN4Table);
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						KmeansGUITable frame = new KmeansGUITable(GUIkMEANS1Table , GUIkMEANS2Table , GUIkMEANS3Table , GUIkMEANS4Table);
						frame.setVisible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

	}	
	
		
		
    public static double[] getclusters ( ArrayList <Double[]> distancetable , int KNN){
    	
    	double [] cluster = new double[5];
    	cluster [4] = 0;
   	   	for (int n = 0 ; n < KNN ; n++) {
   	   		if(distancetable.get(n)[2] == 1)	{ cluster[0] ++; }
   	   		else if(distancetable.get(n)[2] == 2)	{ cluster[1] ++; }
   	   		else if(distancetable.get(n)[2] == 3)	{ cluster[2] ++; }
   	   		else if(distancetable.get(n)[2] == 4)	{ cluster[3] ++; }
   	   	}
   	 
        if( ((cluster[0] > cluster[1]) && (cluster[0] > cluster[2]) && (cluster[0] > cluster[3])) || // if we don't have a tie rerun cluster
        		((cluster[1] > cluster[0])  && (cluster[1] > cluster[2]) && (cluster[1] > cluster[3])) || //measurement belongs to
        		((cluster[2] > cluster[0]) && (cluster[2] > cluster[1]) && (cluster[2] > cluster[3])) ||  
        		((cluster[3] > cluster[0]) && (cluster[3] > cluster[1]) && (cluster[3] > cluster[2])) ) { return cluster; }
        else { // if we have a tie raise flag
        	cluster [4] = 1;
    	    return cluster ;
        }
     
  }
	public static ArrayList<substations> getsubstation(String USER , String PASS , String tname) throws ClassNotFoundException, SQLException { 
		// Register JDBC driver
		Class.forName(JDBC_DRIVER);
		// Open a connection
		Connection conn = DriverManager.getConnection(DB_URL+DISABLE_SSL, USER, PASS);
	    Statement stm;
	    stm = conn.createStatement();
	    String sqll = "USE "+tname+" ;";
	    stm.executeUpdate(sqll);
	    String sql = "SELECT * FROM substations;";
	    ResultSet rst;
	    rst = stm.executeQuery(sql);
	    ArrayList<substations> subList = new ArrayList<substations>();
	    while (rst.next()) {
	    	substations sub = new substations(rst.getString("rdfid"), rst.getString("name"), rst.getString("region_id"));
	        subList.add(sub);
	    }
	    conn.close();
	    return subList;
	}
	public static ArrayList<measurement> getmeasure(String USER , String PASS , String tname) throws ClassNotFoundException, SQLException { 
		// Register JDBC driver
		Class.forName(JDBC_DRIVER);
		// Open a connection
		Connection conn = DriverManager.getConnection(DB_URL+DISABLE_SSL, USER, PASS);
	    Statement stm;
	    stm = conn.createStatement();
	    String sqll = "USE "+tname+" ;";
	    stm.executeUpdate(sqll);
	    String sql = "SELECT * FROM measurements ORDER BY time LIMIT 0,10000;";
	    ResultSet rst;
	    rst = stm.executeQuery(sql);
	    ArrayList<measurement> measList = new ArrayList<measurement>();
	    while (rst.next()) {
	    	measurement meas = new measurement(rst.getString("rdfid"), rst.getString("name"), rst.getDouble("time"), rst.getDouble("value"), rst.getString("sub_rdfid"));
	        measList.add(meas);
	    }
	    
	    conn.close();
	    return measList;
	}
	public static ArrayList<measurement> getanValues(String USER , String PASS , String tname) throws ClassNotFoundException, SQLException { 
		// Register JDBC driver
		Class.forName(JDBC_DRIVER);
		// Open a connection
		Connection conn = DriverManager.getConnection(DB_URL+DISABLE_SSL, USER, PASS);
	    Statement stm;
	    stm = conn.createStatement();
	    String sqll = "USE "+tname+" ;";
	    stm.executeUpdate(sqll);
	    String sql = "SELECT * FROM analog_values ORDER BY time LIMIT 0,10000;";
	    ResultSet rst;
	    rst = stm.executeQuery(sql);
	    ArrayList<measurement> anVList = new ArrayList<measurement>();
	    while (rst.next()) {
	    	measurement anV = new measurement(rst.getString("rdfid"), rst.getString("name"), rst.getDouble("time"), rst.getDouble("value"), rst.getString("sub_rdfid"));
	        anVList.add(anV);
	    }
	    
	    conn.close();
	    return anVList;
	}
	public static double VoltageD (double V1 , double a1 , double V2 , double a2){
		return Math.sqrt( (Math.pow((V1*Math.cos(Math.toRadians(a1))-V2*Math.cos(Math.toRadians(a2))),2)) + (Math.pow((V1*Math.sin(Math.toRadians(a1))-V2*Math.sin(Math.toRadians(a2))),2)) );
	}

}
