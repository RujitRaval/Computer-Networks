
public class Hole 
{	
	int first;
	int last;
	public Hole(int first, int last) 
	{
		this.first = first;
		this.last = last;
	}
	public String toString()
	{
		return String.format("Hole:<%s,%s>", this.first, this.last);
	}

}
