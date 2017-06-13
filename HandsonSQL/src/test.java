package assignment1;

import java.sql.*;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.ArrayList;

public class Amain {
	
	private static boolean check = false;
	static final double Sb = 650; 
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/";
	static final String DISABLE_SSL = "?useSSL=false";
	// Database credentials
	static final String USER = "root";
	static final String PASS = "nv_egl_91"; // insert the password to SQL server
	
	
	public static void main(String[] args){
	
		//Reading files
		FileRead file = new FileRead();
		file.Parse();
		file.doc.getDocumentElement().normalize();
		file.doc1.getDocumentElement().normalize();
	
		
		//BaseVoltage
		NodeList BVlList = file.doc.getElementsByTagName("cim:BaseVoltage");
		ArrayList<BaseVoltage> BV = new ArrayList<BaseVoltage>();
		
		for (int i = 0; i < BVlList.getLength(); i++) {
			BV.add(extractBV (BVlList.item(i)));	
		}
		
		//Substations
		NodeList SubList = file.doc.getElementsByTagName("cim:Substation");
		ArrayList<Substation> Sub = new ArrayList<Substation>();
		
		for (int i = 0; i < SubList.getLength(); i++) {
			Sub.add(extractSub (SubList.item(i)));	
		}
		
		//Regulating Control
		NodeList RCEQList = file.doc.getElementsByTagName("cim:RegulatingControl");
		NodeList RCSSHList = file.doc1.getElementsByTagName("cim:RegulatingControl");
		ArrayList<RegulatingControl>  RCntr = new ArrayList<RegulatingControl>();
		
		for (int i = 0; i < RCEQList.getLength(); i++) {
			RCntr.add(extractRC (RCEQList.item(i), RCSSHList.item(i)));	
		}
		
		//Voltage Level
		NodeList VLList = file.doc.getElementsByTagName("cim:VoltageLevel");
		ArrayList<VoltageLevel> VL = new ArrayList<VoltageLevel>();
		
		for (int i = 0; i < VLList.getLength(); i++) {
			VL.add(extractVL (VLList.item(i)));	
		}
		
		//Generating Unit
		NodeList GUList = file.doc.getElementsByTagName("cim:GeneratingUnit");
		ArrayList<GeneratingUnit> GU = new ArrayList<GeneratingUnit>();
		
		for (int i = 0; i < GUList.getLength(); i++){
			GU.add(extractGU(GUList.item(i)));
		}
		
		//Synchronous Machine
		NodeList SMEQList = file.doc.getElementsByTagName("cim:SynchronousMachine");
		NodeList SMSSHList = file.doc1.getElementsByTagName("cim:SynchronousMachine");
		ArrayList<SynchronousMachine>  SynchM = new ArrayList<SynchronousMachine>();
		
		for (int i = 0; i < SMEQList.getLength(); i++) {
			SynchM.add(extractSM (SMEQList.item(i), SMSSHList.item(i)));	
		}

		//Connectivity Node
		NodeList CNList = file.doc.getElementsByTagName("cim:ConnectivityNode");
		ArrayList<ConnectivityNode> CNs = new ArrayList<ConnectivityNode>();
			
		for (int i = 0; i < CNList.getLength(); i++) {
			CNs.add(extractCN(CNList.item(i) , i));	
		}
		
		//Terminal
		
		NodeList TermList = file.doc.getElementsByTagName("cim:Terminal");
		ArrayList<Terminal> Term = new ArrayList<Terminal>();
				
		for (int i = 0; i < TermList.getLength(); i++) {
			Term.add(extractTerminal(i , TermList.item(i) , CNs));
		}
		
		//Power Transformer End
		NodeList PTEndList = file.doc.getElementsByTagName("cim:PowerTransformerEnd");
		ArrayList<PowerTransformerEnd>  PTEnd = new ArrayList<PowerTransformerEnd>();
		
		for (int i = 0; i < PTEndList.getLength(); i++) {
			PTEnd.add(extractPTend (PTEndList.item(i) , i , BV , Term));
		}
		
		//Power Transformer 
		NodeList PTList = file.doc.getElementsByTagName("cim:PowerTransformer");
		ArrayList<PowerTransformer>  PTrsf = new ArrayList<PowerTransformer>();
		
		for (int i = 0; i < PTList.getLength(); i++) {
			PTrsf.add(extractPT (i , PTList.item(i) , PTEnd));	
		}
		
		//Energy Consumer
		NodeList EnCEQList = file.doc.getElementsByTagName("cim:EnergyConsumer");
		NodeList EnCSSHList = file.doc1.getElementsByTagName("cim:EnergyConsumer");
		ArrayList<EnergyConsumer>  EnCons = new ArrayList<EnergyConsumer>();
		
		for (int i = 0; i < EnCEQList.getLength(); i++) {
			EnCons.add(extractEnC (EnCEQList.item(i), EnCSSHList.item(i)));	
		}
		
		//Breaker
		NodeList BrEQList = file.doc.getElementsByTagName("cim:Breaker");
		NodeList BrSSHList = file.doc1.getElementsByTagName("cim:Breaker");
		ArrayList<Breaker>  Breakers = new ArrayList<Breaker>();
		
		for (int i = 0; i < BrEQList.getLength(); i++) {
			Breakers.add(extractBreaker (BrEQList.item(i), BrSSHList.item(i), i, Term));
		}
		
		//Ratio Tap Changer
		NodeList RTCList = file.doc.getElementsByTagName("cim:RatioTapChanger");
		ArrayList<RatioTapChanger> RTC = new ArrayList<RatioTapChanger>();
		
		for (int i = 0; i < RTCList.getLength(); i++) {
			RTC.add(extractRTC (RTCList.item(i)));	
		}
		
		///// EXTRA INPUTS
		
		//ACLineSegment
		NodeList ACLSList = file.doc.getElementsByTagName("cim:ACLineSegment");
		ArrayList<ACLineSegment> ACLS = new ArrayList<ACLineSegment>();
		
		for (int i = 0; i < ACLSList.getLength(); i++) {
			ACLS.add(extractACLS (i , ACLSList.item(i) , BV , Term));
		}
		
		//Busbar Section
		NodeList BusBList = file.doc.getElementsByTagName("cim:BusbarSection");
		ArrayList<BusbarSection> BBs = new ArrayList<BusbarSection>();
				
		for (int i = 0; i < BusBList.getLength(); i++) {
			BBs.add(extractBusbar(BusBList.item(i) , i , Term));	
		}
		
		
	//////////////////////////////////////////////////////////////////
	/////////////////////////////Ybus/////////////////////////////////
	//////////////////////////////////////////////////////////////////
		
		
		
		
		ArrayList<String[]> Transl = new ArrayList<String[]>(); //Terminal -> Component
		
		Transl = CreateTranslation(Term , BBs , Breakers , ACLS , PTrsf);
		
		
			
		ArrayList <CN_Terms> ConNodeTerms = new ArrayList <CN_Terms>(); // Connectivity Node ->Terminals
		
		for (int i = 0; i < CNs.size(); i++){
			ArrayList <Integer> ConTerm = new ArrayList <Integer>();
			for (int j = 0; j < Term.size(); j++){
				if (Term.get(j).GetConnNode() == (CNs.get(i).GetID())){ 
					ConTerm.add(Term.get(j).GetID()); 
				}	
			}	
			CN_Terms x = new CN_Terms (CNs.get(i).GetID(), ConTerm);
			ConNodeTerms.add(x);
		}
		
		ArrayList<Check> check = new ArrayList<Check>();//has the terminal been passed and by which bus?
		for (int i = 0; i < Term.size() ; i++ ){ 
			Check x = new Check(false , BBs.size()+1);
			check.add(x); 
			} 
		
		ArrayList<Integer[]> samebus = new ArrayList<Integer[]>(); // Bus connected through breakers only
		
		ArrayList<Integer> isol = new ArrayList<Integer>(); // isolated buses
		
		//routing through Ybus
		
		int term1 , term , term2 ,term3 , term4 , term5 , term6 , ConnNode , ConnNode1 , isocount , connected;
		boolean isolation;
		isocount = connected = 0;
		Complex Y, Ysh;
		ArrayList<Ybus> Yb = new ArrayList<Ybus>();// it includes the Y ,ysh between two buses and their BusIDs
		
		for (int i = 0; i < BBs.size(); i++){
			isolation = false;
			term1 = BBs.get(i).GetTerm(); // bus -> terminal
			Check x = new Check(true , i);
			check.set(term1 , x);
			ConnNode = Term.get(term1).GetConnNode(); // term ->ConnNode
			for (int j = 0 ; j < ConNodeTerms.get(ConnNode).GetList().size() ; j++){ //terminals connected to CN
				term = ConNodeTerms.get(ConnNode).GetList().get(j);
				if (term == term1){ continue; }
				if (!check(term , Transl)){ continue; } // check if term is a component for Y bus
				Check y = new Check(true , i);//say I passed through these terms starting from bus i
				check.set(term , y);
				switch(Transl.get(term)[1]){
				
				case "Breaker":
					if (Breakers.get(Integer.parseInt(Transl.get(term)[2])).GetState().equals("true")){ //isolated bus check
						isolation = true;
						continue;
					}
					term2 = Breakers.get(Integer.parseInt(Transl.get(term)[2])).GetTerm1();
					if (term == term2){term2 = Breakers.get(Integer.parseInt(Transl.get(term)[2])).GetTerm2(); } //term-> other terminal of breaker
					if (check.get(term2).GetCheck()) {  //have I been here before only through buses?
						samebus.add(new Integer[]{i , check.get(term2).GetBusID()});
						connected++; }
					Check e = new Check(true , i);
					check.set(term2 , e);
					ConnNode1 = Term.get(term2).GetConnNode(); // term ->ConnNode
					for (int l = 0; l < ConNodeTerms.get(ConnNode1).GetList().size() ; l++){//terminals connected to CN
						term5 = ConNodeTerms.get(ConnNode1).GetList().get(l);
						if (term5 == term2){ continue; }
						if (!check(term5 , Transl)){ continue; }
						if (Transl.get(term5)[1].equals("PowerTransformer")){
							Check f = new Check(true , i);
							check.set(term5 , f);
							Y = PTrsf.get(Integer.parseInt(Transl.get(term5)[2])).GetY();
							Ysh = PTrsf.get(Integer.parseInt(Transl.get(term5)[2])).Getysh();
							term6 = PTrsf.get(Integer.parseInt(Transl.get(term5)[2])).GetTerm1();
							if (term5 == term6){term6 = PTrsf.get(Integer.parseInt(Transl.get(term5)[2])).GetTerm2(); }	
							if (check.get(term6).GetCheck()) { 
								Ybus x3 = new Ybus(Y , Ysh , i , check.get(term6).GetBusID());
								Yb.add(x3);
								isolation = false;
								continue;
							}
							Check u = new Check(true , i);
							check.set(term6 , u);
						
						}		
					}
					break;
				
				case "ACLine":
					Y = ACLS.get(Integer.parseInt(Transl.get(term)[2])).GetY();
					Ysh = ACLS.get(Integer.parseInt(Transl.get(term)[2])).Getysh();
					term3 = ACLS.get(Integer.parseInt(Transl.get(term)[2])).GetTerm1();
					if (term == term3){term3 = ACLS.get(Integer.parseInt(Transl.get(term)[2])).GetTerm2(); }
					if (check.get(term3).GetCheck()) { 
						Ybus x1 = new Ybus(Y , Ysh , i , check.get(term3).GetBusID());//add Y , ysh of line to an arraylist
						Yb.add(x1);
						isolation = false;
						continue; //If i have been here before skip the rest
					}
					
					Check z = new Check(true , i);//mark that I passed through here
					check.set(term3 , z);
					break;
				
				case "PowerTransformer":
					Y = PTrsf.get(Integer.parseInt(Transl.get(term)[2])).GetY();
					Ysh = PTrsf.get(Integer.parseInt(Transl.get(term)[2])).Getysh();
					term4 = PTrsf.get(Integer.parseInt(Transl.get(term)[2])).GetTerm1();
					if (term == term4){term4 = PTrsf.get(Integer.parseInt(Transl.get(term)[2])).GetTerm2(); }
					if (check.get(term4).GetCheck()) { 
						Ybus x2 = new Ybus(Y , Ysh , i , check.get(term4).GetBusID());
						Yb.add(x2);
						isolation = false;
						continue;
					}
					
					Check g = new Check(true , i);
					check.set(term4 , g);
					break;
				}
			}
			if (isolation) { 
				System.out.println("Bus "+i+" is isolated");
				isocount ++;
				isol.add(i);
			}
		}
		
//Create the Ybus
		
		int start = 0;
		boolean flag = false;
		ArrayList<Help> help = new ArrayList<Help>(); // has the BusID of breaker only connected
													  // buses and the buses connected to it
		ArrayList<Integer> startList = new ArrayList<Integer>();
		
		for (int i = 0 ; i < samebus.size() ; i++){ //fill help ArrayList
			flag = false;
			start = samebus.get(i)[1];
			for (int k = 0 ; k < startList.size(); k++){
				if (startList.get(k) == start){ flag = true ;}
			}
			if (flag) {continue;}
			startList.add(start);
			
			ArrayList<Integer> samebusList = new ArrayList<Integer>();
			for (int j = 0 ; j < samebus.size() ; j++){
				if (start == samebus.get(j)[0]){ samebusList.add(samebus.get(j)[1]); }
				if (start == samebus.get(j)[1]){ samebusList.add(samebus.get(j)[0]); }
			}
			Help x =new Help(start , samebusList);
			help.add(x);
		}
		
		for (int i = 0 ; i < Yb.size() ; i++){ // modify Y bus to only contain real buses IDs
			for (int k = 0 ; k < help.size(); k++){
				for (int j = 0 ; j < help.get(k).GetList().size(); j++){
					if (Yb.get(i).GetBusID1() == help.get(k).GetList().get(j)){
						Ybus x = new Ybus(Yb.get(i).GetY() , Yb.get(i).Gety() , help.get(k).GetBusID() , Yb.get(i).GetBusID2());
						Yb.set(i, x);
					}
					if (Yb.get(i).GetBusID2() == help.get(k).GetList().get(j)){
						Ybus x = new Ybus(Yb.get(i).GetY() , Yb.get(i).Gety() , Yb.get(i).GetBusID1() , help.get(k).GetBusID());
						Yb.set(i, x);
					}
				}
			}
		}
		
		startList = new ArrayList<Integer>(); //how many bus do i have in Ybus and their IDs
		flag = false;
		for (int i = 0 ; i < Yb.size() ; i++){
			flag = false;
			start = Yb.get(i).GetBusID1();
			for (int k = 0 ; k < startList.size(); k++){
				if (startList.get(k) == start){ flag = true ;}
			}
			if (!flag){ startList.add(start); }
			flag = false;
			start = Yb.get(i).GetBusID2();
			for (int k = 0 ; k < startList.size(); k++){
				if (startList.get(k) == start){ flag = true ;}
			}
			if (!flag){ startList.add(start); }
			
		}
		

		
		
		
		
		
//		System.out.println("there are : "+ (BBs.size()-connected)+" buses and: "+isocount+" isolated ones");	
		
//		System.out.println(PTEnd.get(5).Getysh().StringRep()+"\n");
//		
		for (int i = 0; i < Yb.size(); i++){
			System.out.println(Yb.get(i).GetY().StringRep()+"\t"+Yb.get(i).Gety().StringRep()+"\t"+Yb.get(i).GetBusID1()+"\t"+Yb.get(i).GetBusID2());	
		}
//	
//		for (int i = 0; i < startList.size(); i++){
//			System.out.println(startList.get(i)/*.GetBusID()+"\t"+help.get(i).GetList()*/ ); 
//		}
//		
}
	
		
	
	
	
	
	public static BaseVoltage extractBV (Node node){
		Element element = (Element) node;
		
		String  NV = null; 
		
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			NV = element.getElementsByTagName("cim:BaseVoltage.nominalVoltage").item(0).getTextContent();
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this program");
			System.exit(0);
		}
	    
		BaseVoltage x = new BaseVoltage(rdfID, NV);
		return x;	
	}
	
	public static Substation extractSub (Node node){
		NodeList childList = node.getChildNodes();
		Element element = (Element) node;
		
		String SR , name ;
		SR = name = null;
		
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
				if ("cim:Substation.Region".equals(childNode.getNodeName())) {
					Element element1 = (Element) childNode;
					SR= element1.getAttribute("rdf:resource");
	            }
	        }
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this program");
			System.exit(0);
		}
		
		Substation x = new Substation (rdfID , name , SR);
		return x;
	}
	
	public static RegulatingControl extractRC (Node nodeEQ, Node nodeSSH){
		Element element1 = (Element) nodeEQ;
		Element element2 = (Element) nodeSSH;
		
		String name , targetValue;
		name = targetValue = null;
		String rdfID = element1.getAttribute("rdf:ID");
		try{
			name = element1.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
		
			targetValue = element2.getElementsByTagName("cim:RegulatingControl.targetValue").item(0).getTextContent();
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this"); 
			System.exit(0);
		}
		RegulatingControl x = new RegulatingControl(rdfID, name, targetValue);
		return x;	
	}	
	
	public static VoltageLevel extractVL (Node node){
		
		NodeList childList = node.getChildNodes();
		Element element = (Element) node;
		
		String VS , BV , name;
		VS = BV = name = null;
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
		
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
	            
				if ("cim:VoltageLevel.Substation".equals(childNode.getNodeName())) {
					Element element1 = (Element) childNode;
	                VS= element1.getAttribute("rdf:resource");
	            }
	            if ("cim:VoltageLevel.BaseVoltage".equals(childNode.getNodeName())) {
	            	Element element1 = (Element) childNode;
	                BV= element1.getAttribute("rdf:resource");
	            }
	        }
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		VoltageLevel x = new VoltageLevel (rdfID , name , VS , BV );
		return x;
	}
	
	public static GeneratingUnit extractGU (Node node){
		NodeList childList = node.getChildNodes();
		Element element = (Element) node;
		
		String EqC , name ,Pmx , Pmn;
		EqC = name = Pmx = Pmn = null;
		
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			Pmx = element.getElementsByTagName("cim:GeneratingUnit.maxOperatingP").item(0).getTextContent();
			Pmn = element.getElementsByTagName("cim:GeneratingUnit.minOperatingP").item(0).getTextContent();
		
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
	            	if ("cim:Equipment.EquipmentContainer".equals(childNode.getNodeName())) {
	            		Element element1 = (Element) childNode;
	            		EqC = element1.getAttribute("rdf:resource");
	            	}
	        }
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		GeneratingUnit x = new GeneratingUnit (rdfID , name , Pmx , Pmn , EqC);
		return x;
	}
	
	public static SynchronousMachine extractSM (Node nodeEQ , Node nodeSSH){
		NodeList childList = nodeEQ.getChildNodes();
		Element element1 = (Element) nodeEQ;
		Element element2 = (Element) nodeSSH;
	
		String EqC , RC , RMGU , name , S , P , Q;
		EqC = RC = RMGU = name = S = P = Q = null;
		
		String rdfID = element1.getAttribute("rdf:ID");
		
		try{
			name = element1.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			S = element1.getElementsByTagName("cim:RotatingMachine.ratedS").item(0).getTextContent();
		
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
				if ("cim:Equipment.EquipmentContainer".equals(childNode.getNodeName())) {
	        	   	Element element3 = (Element) childNode;
	        	   	EqC = element3.getAttribute("rdf:resource");
				}
	           	if ("cim:RegulatingCondEq.RegulatingControl".equals(childNode.getNodeName())) {
	           		Element element3 = (Element) childNode;
	           		RC = element3.getAttribute("rdf:resource");
	           	}
	           	if ("cim:RotatingMachine.GeneratingUnit".equals(childNode.getNodeName())) {
	           		Element element3 = (Element) childNode;
	           		RMGU = element3.getAttribute("rdf:resource");
	            }
	        }
			
			P = element2.getElementsByTagName("cim:RotatingMachine.p").item(0).getTextContent();
			Q = element2.getElementsByTagName("cim:RotatingMachine.q").item(0).getTextContent();
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		SynchronousMachine x = new SynchronousMachine (rdfID , name , EqC , RC , RMGU , S , P , Q);
		return x;
	}

	public static PowerTransformerEnd extractPTend (Node node , int j , ArrayList<BaseVoltage> bv , ArrayList<Terminal> term){
		NodeList childList = node.getChildNodes();
		Element element = (Element) node;
		
		int terminal = 0;
		double BaseV = 0;
		String Trans , BV , name , R , X , B , G , s , v , Term;
		Trans = BV = name = R = X = B = G = s = v = Term = null;
		
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			R = element.getElementsByTagName("cim:PowerTransformerEnd.r").item(0).getTextContent();
			X = element.getElementsByTagName("cim:PowerTransformerEnd.x").item(0).getTextContent();
			B = element.getElementsByTagName("cim:PowerTransformerEnd.b").item(0).getTextContent();
			G = element.getElementsByTagName("cim:PowerTransformerEnd.g").item(0).getTextContent();
			s = element.getElementsByTagName("cim:PowerTransformerEnd.ratedS").item(0).getTextContent();
			v = element.getElementsByTagName("cim:PowerTransformerEnd.ratedU").item(0).getTextContent();
		
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
				if ("cim:TransformerEnd.BaseVoltage".equals(childNode.getNodeName())) {
					Element element1 = (Element) childNode;
					BV= element1.getAttribute("rdf:resource");
					for (int k = 0; k < bv.size(); k++){
						if (BV.substring(1).equals(bv.get(k).GetrdfID())){
							BaseV = bv.get(k).GetnV();
							break;
						}
					}
	            }
	            if ("cim:PowerTransformerEnd.PowerTransformer".equals(childNode.getNodeName())) {
	            	Element element1 = (Element) childNode;
	            	Trans= element1.getAttribute("rdf:resource");
	            }
	            if ("cim:TransformerEnd.Terminal".equals(childNode.getNodeName())) {
	            	Element element1 = (Element) childNode;
	            	Term= element1.getAttribute("rdf:resource");
	            	for (int k =0 ; k < term.size() ; k++){
	            		if (Term.substring(1).equals(term.get(k).GetrdfID())){
	            			terminal = term.get(k).GetID();
	            		}
	            	}
	            }
	        }
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		PowerTransformerEnd y = new PowerTransformerEnd (j , rdfID , name , Trans , BV , R , X , B , G , s , v , BaseV , terminal , Sb);
		
		return y;
	}
	
	public static PowerTransformer extractPT (int j , Node node , ArrayList<PowerTransformerEnd> pte){
		NodeList childList = node.getChildNodes();
		Element element = (Element) node;
		
		Complex Yf = new Complex(0,0); 
		Complex yf = new Complex(0,0);
		Complex Y1 = new Complex(0,0);
		Complex Y2 = new Complex(0,0);
		Complex y1 = new Complex(0,0);
		Complex y2 = new Complex(0,0);
		int pte1 , pte2 , h;
		pte1 = pte2 = h = 0;
		String EqC ,name;
		EqC = name = null;
		
		String rdfID = element.getAttribute("rdf:ID");
		
		for (int k = 0; k < pte.size() ; k++){
			if (rdfID.equals(pte.get(k).GetTransf()) && h == 1){
				pte2 = pte.get(k).GetTerminal();
				Y1 = pte.get(k).GetY();
				y1 = pte.get(k).Getysh();
				break;
			}
			if (rdfID.equals(pte.get(k).GetTransf()) && h == 0){
				pte1 = pte.get(k).GetTerminal();
				h = 1;
				Y2 = pte.get(k).GetY();
				y2 =pte.get(k).Getysh();
			}
		}
		Yf = Y1.plus(Y2);
		yf = y1.plus(y2);
		try{
			name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
		
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
				if ("cim:Equipment.EquipmentContainer".equals(childNode.getNodeName())) {
					Element element1 = (Element) childNode;
					EqC= element1.getAttribute("rdf:resource");
	            }
	        }
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		PowerTransformer x = new PowerTransformer (j , pte1 , pte2 , rdfID , name , EqC , Yf , yf);
		return x;
	}

	public static Terminal extractTerminal(int j , Node node , ArrayList<ConnectivityNode> cn){
		NodeList childList = node.getChildNodes();
		Element element = (Element) node;
		
		String CE = null;
		String ConnNode = null;
		int ConnID = 0;
		
		
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			for (int k = 0; k < childList.getLength(); k++) {
				Node childNode = childList.item(k);
				if ("cim:Terminal.ConductingEquipment".equals(childNode.getNodeName())) {
					Element element1 = (Element) childNode;
					CE= element1.getAttribute("rdf:resource");
	            }
				if ("cim:Terminal.ConnectivityNode".equals(childNode.getNodeName())) {
					Element element1 = (Element) childNode;
					ConnNode= element1.getAttribute("rdf:resource");
					
					for (int i = 0 ; i < cn.size() ; i++){
						if (ConnNode.substring(1).equals(cn.get(i).GetrdfID())){
							ConnID = cn.get(i).GetID();
							break;
						}
					}
				}
			}
		}
		
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		Terminal x = new Terminal (j , rdfID , CE , ConnID);
		return x;
	}
	
	public static EnergyConsumer extractEnC (Node nodeEQ , Node nodeSSH){
		NodeList childList = nodeEQ.getChildNodes();
		Element element1 = (Element) nodeEQ;
		Element element2 = (Element) nodeSSH;
		
		String EqC , name ,P , Q;
		EqC = name = P = Q = null;
		
		String rdfID = element1.getAttribute("rdf:ID");
		
		try{
			name = element1.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
		
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
				if ("cim:Equipment.EquipmentContainer".equals(childNode.getNodeName())) {
					Element element3 = (Element) childNode;
					EqC = element3.getAttribute("rdf:resource");
				} 
	        }
			P = element2.getElementsByTagName("cim:EnergyConsumer.p").item(0).getTextContent();
			Q = element2.getElementsByTagName("cim:EnergyConsumer.q").item(0).getTextContent();
			
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		EnergyConsumer x = new EnergyConsumer (rdfID , name , EqC , P , Q);
		return x;
	}

	public static Breaker extractBreaker (Node nodeEQ , Node nodeSSH , int j , ArrayList<Terminal> terminal){
		NodeList childList = nodeEQ.getChildNodes();
		Element element1 = (Element) nodeEQ;
		Element element2 = (Element) nodeSSH;
		
		int term1 , term2 , h;
		term1 = term2 = h = 0;
		String EqC , name , st;
		EqC = name = st = null;
		
		String rdfID = element1.getAttribute("rdf:ID");
		
		try{
			name = element1.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
		
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
				if ("cim:Equipment.EquipmentContainer".equals(childNode.getNodeName())) {
					Element element3 = (Element) childNode;
					EqC = element3.getAttribute("rdf:resource");
				} 
			}
			st = element2.getElementsByTagName("cim:Switch.open").item(0).getTextContent();
			
			for (int i = 0; i < terminal.size() ; i++){
				if (rdfID.equals(terminal.get(i).GetCondEq()) && h == 1){
					term2 = terminal.get(i).GetID();
					break; 
				}
				if (rdfID.equals(terminal.get(i).GetCondEq()) && h == 0){
					term1 = terminal.get(i).GetID();
					h = 1; 
				}
			}
		
		}
		
		
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		Breaker x = new Breaker (j , rdfID , name , EqC , st , term1 , term2);
		return x;
	}

	public static RatioTapChanger extractRTC (Node node){
		Element element = (Element) node;
		
		String name , stp;
		name = stp = null;
		
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			stp = element.getElementsByTagName("cim:TapChanger.normalStep").item(0).getTextContent();
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		RatioTapChanger x = new RatioTapChanger(rdfID, name , stp);
		return x;	
	}

	public static ACLineSegment extractACLS (int j ,Node node , ArrayList<BaseVoltage> bv , ArrayList<Terminal> terminal){
		NodeList childList = node.getChildNodes();
		Element element = (Element) node;
		
		String BV, l , r , X, g ,b;
		BV = l = r = X = g = b = null;
		double BaseV = 0;
		int term1 , term2 , h;
		term1 = term2 = h = 0;
		
		String rdfID = element.getAttribute("rdf:ID");
		
		try{
			
			l = element.getElementsByTagName("cim:Conductor.length").item(0).getTextContent();
			r = element.getElementsByTagName("cim:ACLineSegment.r").item(0).getTextContent();
			X = element.getElementsByTagName("cim:ACLineSegment.x").item(0).getTextContent();
			b = element.getElementsByTagName("cim:ACLineSegment.bch").item(0).getTextContent();
			g = element.getElementsByTagName("cim:ACLineSegment.gch").item(0).getTextContent();
			
			for (int i = 0; i < terminal.size() ; i++){
				if (rdfID.equals(terminal.get(i).GetCondEq()) && h == 1){
					term2 = terminal.get(i).GetID();
					break; 
				}
				if (rdfID.equals(terminal.get(i).GetCondEq()) && h == 0){
					term1 = terminal.get(i).GetID();
					h = 1; 
				}
			}
			
			for (int i = 0; i < childList.getLength(); i++) {
				Node childNode = childList.item(i);
	            if ("cim:ConductingEquipment.BaseVoltage".equals(childNode.getNodeName())) {
	            	Element element1 = (Element) childNode;
	                BV= element1.getAttribute("rdf:resource");
	                
	                for (int k = 0 ; k < bv.size() ; k++){
	                	if (BV.substring(1).equals(bv.get(k).GetrdfID())){
	                		BaseV = bv.get(k).GetnV();
	                	}
	                }
	            }
			}
		}
		catch(NullPointerException e){
			System.out.println("your file can not be parsed by this");
			System.exit(0);
		}
		
		ACLineSegment x = new ACLineSegment (j , rdfID , BaseV , l , r , X , b , g , term1 , term2 , Sb);
		return x;
	}
	
	public static ConnectivityNode extractCN (Node node , int j){
		Element element = (Element) node;
		
		String rdfID = element.getAttribute("rdf:ID");
		
		ConnectivityNode x = new ConnectivityNode (rdfID , j);
		return x;
	}
	
	public static BusbarSection extractBusbar (Node node , int j , ArrayList<Terminal> terminal){
		Element element = (Element) node;
		
		int TermID = 0;
		String rdfID = element.getAttribute("rdf:ID");
		
		for (int i = 0; i < terminal.size(); i++){
			if (rdfID.equals(terminal.get(i).GetCondEq())){
				TermID = terminal.get(i).GetID();
			}
		}
		
		BusbarSection x = new BusbarSection (rdfID , j , TermID);
		return x;
	}

	public static ArrayList<String[]> CreateTranslation (ArrayList<Terminal> term , ArrayList<BusbarSection> bbs , ArrayList<Breaker> br , ArrayList<ACLineSegment> ACL , ArrayList<PowerTransformer> pt){
		
		ArrayList<String[]> Translation = new ArrayList<String[]>();
		
		for (int i = 0; i < term.size(); i++){
			check = false;
			for (int k = 0; k < bbs.size(); k++){
				if (bbs.get(k).GetTerm() == i){
					Translation.add(new String[]{String.valueOf(term.get(i).GetID()) , "Busbar" , String.valueOf(bbs.get(k).GetID())});
					check = true;
					break;
				}	
			}
			if (check) {continue;}
			for (int k = 0; k < br.size(); k++){
				if (br.get(k).GetTerm1() == i){
					Translation.add(new String[]{String.valueOf(term.get(i).GetID()) , "Breaker" , String.valueOf(br.get(k).GetID())});
					check = true;
					break;
				}
				if (br.get(k).GetTerm2() == i){
					Translation.add(new String[]{String.valueOf(term.get(i).GetID()) , "Breaker" , String.valueOf(br.get(k).GetID())});
					check = true;
					break;
				}
			}
			if (check) {continue;}
			for (int k = 0; k < ACL.size(); k++){
				if (ACL.get(k).GetTerm1() == i){
					Translation.add(new String[]{String.valueOf(term.get(i).GetID()) , "ACLine" , String.valueOf(ACL.get(k).GetID())});
					check = true;
					break;
				}
				if (ACL.get(k).GetTerm2() == i){
					Translation.add(new String[]{String.valueOf(term.get(i).GetID()) , "ACLine" , String.valueOf(ACL.get(k).GetID())});
					check = true;
					break;
				}
			}
			if (check) {continue;}
			for (int k = 0; k < pt.size(); k++){
				if (pt.get(k).GetTerm1() == i){
					Translation.add(new String[]{String.valueOf(term.get(i).GetID()) , "PowerTransformer" , String.valueOf(pt.get(k).GetID())});
					check = true;
					break;
				}
				if (pt.get(k).GetTerm2() == i){
					Translation.add(new String[]{String.valueOf(term.get(i).GetID()) , "PowerTransformer" , String.valueOf(pt.get(k).GetID())});
					check = true;
					break;
				}
			}
			if (check) {continue;}
			Translation.add(new String[]{String.valueOf(term.get(i).GetID()), "a" , "a"});
		}
		
		return Translation;
	}
	
	public static boolean check (int t , ArrayList<String[]> tr){
		boolean ch = false;
		for (int i = 0; i < tr.size() ; i++){
			if((t==(Integer.parseInt(tr.get(i)[0])))&& !tr.get(i)[1].equals("a")){ ch = true;}
		}
		return ch;
	}
	
}


