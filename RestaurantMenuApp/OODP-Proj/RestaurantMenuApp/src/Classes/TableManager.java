package Classes;

import java.util.ArrayList;

public class TableManager {

	ArrayList<Table> tableList = new ArrayList<Table>();
	
	public TableManager()
	{
		String id;
		for (int i = 0; i<10; i++)
		{
			id = "2" + i;
			tableList.add(new Table(2,id));
		}
		for (int i = 0; i<10; i++)
		{
			id = "4" + i;
			tableList.add(new Table(4,id));
		}
		for (int i = 0; i<5; i++)
		{
			id = "8" + i;
			tableList.add(new Table(8,id));
		}
		for (int i = 0; i<5; i++)
		{
			id = "10" + i;
			tableList.add(new Table(10,id));
		}
	}
	
	/*public void checkVacancy()
	*{
	*}
	*/
	
	/*public void occupyTable()
	 * {
	 * }
	 */
	
	/*public void clearTable()
	 * {
	 * }
	 */
	
}

