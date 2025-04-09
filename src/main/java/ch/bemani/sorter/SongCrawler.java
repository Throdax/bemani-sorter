package ch.bemani.sorter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SongCrawler {

	private static final String DDR_SONG_LIST_URL = "/DanceDanceRevolution_WORLD_Full_Song_List";
	private static final String IIDX_SONG_LIST_URL = "/DanceDanceRevolution_WORLD_Full_Song_List";
	private static final String SDVX_SONG_LIST_URL = "/DanceDanceRevolution_WORLD_Full_Song_List";
	private static final String POPN_SONG_LIST_URL = "/DanceDanceRevolution_WORLD_Full_Song_List";

	public static void main(String[] args) throws MalformedURLException, IOException, URISyntaxException {

		
		List<SongInfo> ddrSongs = crawURL(DDR_SONG_LIST_URL,"DDR",false);
		List<SongInfo> iidxSongs = crawURL(IIDX_SONG_LIST_URL, "IIDX", false);
		List<SongInfo> sdvxSongs = crawURL(SDVX_SONG_LIST_URL, "SDVX", false);
		List<SongInfo> popnSongs = crawURL(POPN_SONG_LIST_URL, "POPN", false);
		
		mergeSongLists(ddrSongs, iidxSongs);
		mergeSongLists(ddrSongs, sdvxSongs);
		mergeSongLists(ddrSongs, popnSongs);
		
		List<SongInfo> mergedList = new ArrayList<SongInfo>();
		mergedList.addAll(ddrSongs);
		mergedList.addAll(iidxSongs);
		mergedList.addAll(sdvxSongs);
		mergedList.addAll(popnSongs);
		
		SorterDataWriter.writeDataJson(mergedList);

	}

	private static List<SongInfo> crawURL(String url, String game, boolean withDownload)
			throws MalformedURLException, IOException, URISyntaxException {
		
		Crawler crawler = new Crawler();
		LocalDateTime dtStartGlobal = LocalDateTime.now();
		
		List<SongInfo> crawledSongs = crawler.crawl(url, game, false);
		crawledSongs.forEach(s -> s.addGame(game));
		
		LocalDateTime dtEndGlobal = LocalDateTime.now();
		Duration timeTaken = Duration.between(dtStartGlobal, dtEndGlobal);
		
		System.out.println("Crawled '" + url + "' with " + crawledSongs.size() + " songs in "
				+ timeTaken.toMinutes() + " minutes and " + timeTaken.toSecondsPart() + " seconds");
		return crawledSongs;
	}

	private static void mergeSongLists(List<SongInfo> toList, List<SongInfo> fromList) {
		
		List<SongInfo> duplicateSongs = new ArrayList<>();
		
		for(SongInfo baseSong : toList) {
			for(SongInfo compareSong : fromList) {
				if(baseSong.equals(compareSong)) {
					baseSong.addGame(compareSong.getGames().get(0));
					duplicateSongs.add(compareSong);
				}
			}
		}
		
		fromList.removeAll(duplicateSongs);
	}

	
}

