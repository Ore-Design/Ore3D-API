package design.ore.ore3dapi.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Version
{
	final int majorVersion;
	final int minorVersion;

	public boolean majorIsGreaterThan(Version ver) { return majorVersion > ver.majorVersion; }
	public boolean minorIsGreaterThan(Version ver) { return (minorVersion > ver.minorVersion); }
	public boolean isLessThan(Version ver) { return majorVersion < ver.majorVersion || (majorVersion == ver.majorVersion && minorVersion < ver.minorVersion); }
	public boolean isEqualTo(Version ver) { return majorVersion == ver.majorVersion && minorVersion == ver.minorVersion; }
	
	@Override
	public String toString() { return majorVersion + "." + minorVersion; }
}
