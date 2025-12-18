package design.ore.api.ore3d.plugin.processing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ore3DPluginManifest
{
    String pluginId, pluginVersion, pluginCompatibleVersion, pluginEntryPoint, latestVersionUrl, latestDownloadUrl;
}
