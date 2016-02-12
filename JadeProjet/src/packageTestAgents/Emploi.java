package packageTestAgents;

import java.io.Serializable;

public class Emploi implements Serializable{
	int revenu;
	int id;
	
	public Emploi(int _rev, int _id){
		revenu = _rev;
		id = _id;
	}
	
	public void setId(int _id){
		id = _id;
	}
	

}
