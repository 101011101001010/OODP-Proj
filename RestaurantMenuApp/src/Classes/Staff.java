package Classes;

public class Staff {

	String name;
	int staffID;
	char gender;
	String title;

	public Staff()
	{
		this.name = " ";
		this.staffID = -1;
		this.gender = ' ';
		this.title = " ";
	}
	public Staff(String name, int staffID, char gender, String title)
	{
		this.name = name;
		this.staffID = staffID;
		this.gender = gender;
		this.title = title;
	}
}
