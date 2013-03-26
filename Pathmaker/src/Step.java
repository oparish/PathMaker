
public class Step
{	
    private int x;
	private int y;
	private StepState type;
	
	Step(int newX, int newY, StepState newType)
	{
		this.x = newX;
		this.y = newY;
		this.type = newType;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public StepState getType()
	{
		return type;
	}
	
	public void setX(int newX)
	{
		x = newX;
	}
	
	public void setY(int newY)
	{
		y = newY;
	}
	
	public void setType(StepState newType)
	{
		type = newType;
	}
}
