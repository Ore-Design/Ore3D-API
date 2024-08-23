package design.ore.Ore3DAPI.data;

import lombok.Getter;

public class Tuple<T, U, V>
{
	@Getter T first;
	@Getter U second;
	@Getter V third;
	
	public Tuple(T first, U second, V third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}
}
