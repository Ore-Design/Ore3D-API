package design.ore.Ore3DAPI.DataTypes;

import lombok.Getter;

public class Quad<T, U, V, W>
{
	@Getter T first;
	@Getter U second;
	@Getter V third;
	@Getter W fourth;
	
	public Quad(T first, U second, V third, W fourth)
	{
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}
}
