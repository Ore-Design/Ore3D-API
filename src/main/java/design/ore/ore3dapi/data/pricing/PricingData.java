package design.ore.ore3dapi.data.pricing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class PricingData
{
	public PricingData(Instant timestamp)
	{
		this.timestamp = timestamp;
	}
	Instant timestamp;
	List<RoutingPricing> routings = new ArrayList<>();
	List<BOMPricing> bom = new ArrayList<>();
}
