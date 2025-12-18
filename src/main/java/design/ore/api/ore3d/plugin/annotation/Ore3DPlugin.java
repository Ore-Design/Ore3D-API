package design.ore.api.ore3d.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Ore3DPlugin
{
    String value();
    String version();
    String latestVersionUrl();
    String latestDownloadUrl();
}
