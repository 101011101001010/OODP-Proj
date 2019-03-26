package Classes;

public class Staff {
	private int id;
	private String name;
	private String title;
	private String gender;

	public Staff(int id, String name, String title, String gender) {
		this.id = id;
		this.name = name;
		this.title = title;
		this.gender = gender;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGender() {
		return gender;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getWriteData() {
		return (id + "," + name + "," + title + "," + gender);
	}
}
