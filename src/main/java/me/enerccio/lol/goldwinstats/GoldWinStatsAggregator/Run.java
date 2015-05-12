package me.enerccio.lol.goldwinstats.GoldWinStatsAggregator;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.api.LoadPolicy;
import com.robrua.orianna.type.core.common.Region;

public class Run {
	
	private static final int[] EXPORT_LEVELS = {1000, 2000, 3000, 4000, 5000, 7500, 10000, 10000};

	public static void main(String[] args) throws Exception{
		RiotAPI.setAPIKey(args[0]);
		RiotAPI.setLoadPolicy(LoadPolicy.LAZY);
		
		new StatisticsGenerator(Region.EUNE, 43169127L, EXPORT_LEVELS).generate().export("eune-bronze.xlsx");
	}

}
