package tools;

public class InputChecker {

	public static boolean checkTableID(int tableID)
	{	
		if(!((tableID>=20 && tableID<=29)||(tableID>=40 && tableID<=49)||(tableID>=80 && tableID<=84)||(tableID>=100 && tableID<=104)))
			return false;
		return true;
		
	}
	
	public static boolean checkPax(int pax)
	{
		if(pax>0 && pax<11)
			return true;
		return false;
	}
	
	public static boolean checkContact(int contact)
	{
		if(contact<=99999999 && contact>=80000000)
			return true;
		return false;
	}
	
}

