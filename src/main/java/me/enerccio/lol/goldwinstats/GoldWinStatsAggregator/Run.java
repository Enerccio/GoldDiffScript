package me.enerccio.lol.goldwinstats.GoldWinStatsAggregator;

import java.util.List;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.store.CloseableIterator;
import com.robrua.orianna.store.DataStore;
import com.robrua.orianna.type.api.LoadPolicy;
import com.robrua.orianna.type.api.RateLimit;
import com.robrua.orianna.type.core.OriannaObject;
import com.robrua.orianna.type.core.common.Region;

public class Run {
	
	private static final int[] EXPORT_LEVELS = {1000, 2000, 3000, 4000, 5000, 7500, 10000, 10000};

	public static void main(String[] args) throws Exception{
		RiotAPI.setAPIKey(args[0]);
		RiotAPI.setLoadPolicy(LoadPolicy.LAZY);
		RiotAPI.setDataStore(new DataStore(){

			@Override
			protected boolean allowsNullStoreKeys() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			protected <T extends OriannaObject<?>> boolean checkHasAll(
					Class<T> type) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			protected <T extends OriannaObject<?>> void doDelete(Class<T> type,
					List<?> keys) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected <T extends OriannaObject<?>> void doDelete(Class<T> type,
					Object key) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected <T extends OriannaObject<?>> List<T> doGet(Class<T> type,
					List<?> keys) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T extends OriannaObject<?>> T doGet(Class<T> type,
					Object key) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T extends OriannaObject<?>> List<T> doGetAll(
					Class<T> type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T extends OriannaObject<?>> CloseableIterator<T> doGetIterator(
					Class<T> type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected <T extends OriannaObject<?>> void doStore(List<T> objs,
					List<?> keys, boolean isFullSet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected <T extends OriannaObject<?>> void doStore(T obj,
					Object key) {
				// TODO Auto-generated method stub
				
			}
			
		});
		RiotAPI.setRateLimit(new RateLimit(3000, 10), new RateLimit(180000, 600));
		
		new StatisticsGenerator(Region.valueOf(args[1]), Long.parseLong(args[2]), EXPORT_LEVELS)
			.generate()
			.export(args[3]);
	}

}
