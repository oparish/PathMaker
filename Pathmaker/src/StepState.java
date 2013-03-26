
public enum StepState
{
	STEP(0),WAYPOINT(1), DESTINATION(2);
	
	private int value;
	private StepState(int value)
	{
		this.value = value;
	}
	public int valueOf()
	{
		return this.value;
	}
	public static StepState getStepState(int type)
	{
		if (type == STEP.valueOf())
			return STEP;
		else if (type == WAYPOINT.valueOf())
			return WAYPOINT;
		else
			return DESTINATION;
	}
}

