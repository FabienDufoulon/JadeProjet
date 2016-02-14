package test;

import java.util.HashMap;

import packageTestAgents.Emploi;

public class TestJadeEnvoiObjetSerializable {
	public static void main(String[] args) {
		Emploi emploi1 = new Emploi(1,1);
		Emploi emploi2 = new Emploi(1,1);
		//emploi2 = emploi1;
		
		HashMap<Emploi, Integer> emploisNombre = new HashMap<Emploi,Integer>();
		emploisNombre.put(emploi1, 3);
		System.out.println(emploisNombre.get(emploi1));
		/*emploisNombre.put(emploi2, 61);
		System.out.println(emploisNombre.get(emploi1));*/
		System.out.println(emploisNombre.containsKey(emploi2));

	}
}
