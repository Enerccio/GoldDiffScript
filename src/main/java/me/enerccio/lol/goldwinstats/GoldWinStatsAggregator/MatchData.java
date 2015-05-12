package me.enerccio.lol.goldwinstats.GoldWinStatsAggregator;

public class MatchData {
	long matchId;
	public int goldA, goldB;
	public boolean winA, winB;
	
	@Override
	public String toString(){
		return "MD: " + goldDiff();
	}

	public boolean underdogWon() {
		return goldDiff() < 0;
	}
	
	public int goldDiff(){
		return winA ? goldA - goldB : goldB - goldA;
	}
}
