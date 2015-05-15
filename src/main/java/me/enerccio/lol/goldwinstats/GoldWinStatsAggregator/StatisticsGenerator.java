package me.enerccio.lol.goldwinstats.GoldWinStatsAggregator;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.common.QueueType;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.core.match.Frame;
import com.robrua.orianna.type.core.match.Match;
import com.robrua.orianna.type.core.match.MatchTeam;
import com.robrua.orianna.type.core.match.Participant;
import com.robrua.orianna.type.core.match.ParticipantFrame;
import com.robrua.orianna.type.core.match.Timeline;
import com.robrua.orianna.type.core.matchhistory.MatchSummary;

public class StatisticsGenerator {
	
	private Map<Long, MatchData> matches = new HashMap<Long, MatchData>();
	private Map<Long, Boolean> summonersChecked = new HashMap<Long, Boolean>();
	private List<Long> summonersCheckedKeys = new ArrayList<Long>();
	private Random r = new Random();
	private int[] exportLevels;
	
	public StatisticsGenerator(Region re, long startPoint, int[] exportLevels) {
		RiotAPI.setMirror(re);
		RiotAPI.setRegion(re);
		addSummoner(startPoint);
		this.exportLevels = exportLevels;
	}

	public StatisticsGenerator generate() {
		while (matches.size() < 50000){
			try {
				getSomeMatches();
			} catch (Exception e){
				e.printStackTrace();
			}
			System.err.println(matches.size() + " of " + 50000);
		}
		summonersChecked.clear();
		summonersCheckedKeys.clear();
		return this;
	}

	private void getSomeMatches() {
		Long sId = getRandomSummoner();
		List<MatchSummary> mslist = RiotAPI.getMatchHistory(sId, QueueType.RANKED_SOLO_5x5);
		for (MatchSummary ms : mslist){
			if (matches.containsKey(ms.getID()))
				continue;
			Match m = ms.getMatch();
			
			MatchData md = new MatchData();
			
			Map<Integer, Integer> goldAt19 = new HashMap<Integer, Integer>();
			
			Timeline t = m.getTimeline();
			Frame closestFrame = null;
			long closestFrameTime = 0;
			List<Frame> fl = t.getFrames();
			if (fl == null){
				continue;
			}
			for (Frame f : t.getFrames()){
				long ts = f.getTimestamp();
				if ((closestFrameTime < (1000 * 60 * 19) && ts > closestFrameTime) ||
						(ts < closestFrameTime)){
					closestFrameTime = ts;
					closestFrame = f;
				}
			}
			
			for (Integer pi : closestFrame.getParticipantFrames().keySet()){
				ParticipantFrame pf = closestFrame.getParticipantFrames().get(pi);
				goldAt19.put(pi, pf.getTotalGold());
			}
			
			List<MatchTeam> teams = m.getTeams();
			int teamAId = teams.get(0).getDto().getTeamId();
			
			md.winA = teams.get(0).getWinner();
			md.winB = teams.get(1).getWinner();
			md.matchId = m.getID();
			
			for (Participant p : m.getParticipants()){
				int tid = (int) p.getTeam().getID();
				if (tid == teamAId){
					md.goldA += goldAt19.get(p.getParticipantID());
				} else {
					md.goldB += goldAt19.get(p.getParticipantID());
				}
				addSummoner(p.getSummonerID());
			}
			
			matches.put(m.getID(), md);
		}
		summonersChecked.put(sId, true);
	}

	private void addSummoner(long summonerID) {
		if (summonersCheckedKeys.size() > 200000)
			return;
		summonersChecked.put(summonerID, false);
		summonersCheckedKeys.add(summonerID);
	}

	private Long getRandomSummoner() {
		Long sId = null;
		while (sId == null){
			sId = summonersCheckedKeys.get(r.nextInt(summonersChecked.size()));
			if (summonersChecked.get(sId))
				sId = null;
		}
		return sId;
	}

	public void export(String exportPath) throws Exception {
		Workbook wb = new XSSFWorkbook();
		
		export(wb);
		
	    FileOutputStream fileOut = new FileOutputStream(exportPath);
	    wb.write(fileOut);
	    fileOut.close();
	}

	private void export(Workbook wb) throws Exception {
		 Sheet sheet = wb.createSheet("Results");
		 Row[] rArr = new Row[exportLevels.length];
		 int[] iArr = new int[exportLevels.length];

		 Sheet sheetDS = wb.createSheet("Datasources");
		 Row header = sheetDS.createRow(0);
		 header.createCell(0).setCellValue("Match id");
		 header.createCell(1).setCellValue("Team A won");
		 header.createCell(2).setCellValue("Team B won");
		 header.createCell(3).setCellValue("Team A gold at 19m");
		 header.createCell(4).setCellValue("Team B gold at 19m");
		 header.createCell(5).setCellValue("Winning team gold difference");
		 header.createCell(6).setCellValue("And you wanted to surrender...");
		 
		 int dsIt = 0;
		 for (MatchData md : matches.values()){
			 if (md.underdogWon()){
				 findPlace(md, iArr);
			 }
			 
			 Row r = sheetDS.createRow(++dsIt);
			 r.createCell(0).setCellValue(md.matchId);
			 r.createCell(1).setCellValue(md.winA);
			 r.createCell(2).setCellValue(md.winB);
			 r.createCell(3).setCellValue(md.goldA);
			 r.createCell(4).setCellValue(md.goldB);
			 r.createCell(5).setCellValue(md.goldDiff());
			 r.createCell(6).setCellValue(md.underdogWon());
		 }
		 
		 Row fr = sheet.createRow(0);
		 fr.createCell(0).setCellValue("Gold difference");
		 fr.createCell(1).setCellValue("Won between");
		 fr.createCell(2).setCellValue("Won with that difference");
		 
		 for (int i=0; i<rArr.length; i++){
			 rArr[i] = sheet.createRow(i+1);
			 rArr[i].createCell(0).setCellValue(exportLevels[i]);
			 rArr[i].createCell(1).setCellValue(iArr[i]);
			 rArr[i].createCell(2).setCellValue(aggregate(iArr, i));
		 }
		 
		 Row lr = sheet.createRow(exportLevels.length+2);
		 lr.createCell(0).setCellValue("Total matches: ");
		 lr.createCell(1).setCellValue(matches.size());
	}

	private int aggregate(int[] iArr, int start) {
		int ag = 0;
		for (int i=start; i<iArr.length; i++)
			ag += iArr[i];
		return ag;
	}

	private void findPlace(MatchData md, int[] iArr) {
		int it = 0;
		int diff = Math.abs(md.goldDiff());
		for (int i=0; i<exportLevels.length; i++){
			if (diff > exportLevels[i])
				it = i+1;
			if (diff <= exportLevels[i]){
				break;
			}
		}
		++iArr[Math.min(it, iArr.length-1)];
	}

}
