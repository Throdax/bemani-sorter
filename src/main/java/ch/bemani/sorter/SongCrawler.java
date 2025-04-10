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
	private static final String IIDX_SONG_LIST_URL = "/Beatmania_IIDX_32_Pinky_Crush_full_song_list";
	private static final String SDVX_SONG_LIST_URL = "/AC_SDVX_EG";
	private static final String SDVX_KONASTE_SONG_LIST_URL = "/CS_SDVX_EG/Song_list";
	private static final String SDVX_KONASTE_PACKS_SONG_LIST_URL = "/CS_SDVX_EG/Music_Packs";
	private static final String POPN_SONG_LIST_URL = "/AC_pnm_Jam%26Fizz";
	private static final String GITATORA_SONG_LIST_URL = "/GITADORA_GALAXY_WAVE_DELTA_Full_Song_List";
	private static final String JUBEAT_SONG_LIST_URL = "/Jubeat_beyond_the_Ave._Full_Song_List";

	public static void main(String[] args) throws MalformedURLException, IOException, URISyntaxException {

		
		List<SongInfo> ddrSongs = crawURL(DDR_SONG_LIST_URL,"DDR",false);
		List<SongInfo> iidxSongs = crawURL(IIDX_SONG_LIST_URL, "IIDX", false);
		List<SongInfo> sdvxSongs = crawURL(SDVX_SONG_LIST_URL, "SDVX", false);
		List<SongInfo> sdvxKoSongs = crawURL(SDVX_KONASTE_SONG_LIST_URL, "SDVX-KO", false);
		List<SongInfo> sdvxKoMPSongs = crawURL(SDVX_KONASTE_PACKS_SONG_LIST_URL, "SDVX-KO-MP", true);
		List<SongInfo> popnSongs = crawURL(POPN_SONG_LIST_URL, "POPN", false);
		List<SongInfo> gitaSongs = crawURL(GITATORA_SONG_LIST_URL, "GITA", false);
		List<SongInfo> juSongs = crawURL(JUBEAT_SONG_LIST_URL, "JUBE", false);
		
		System.out.println("Merging Lists...");
		mergeSongLists(ddrSongs, iidxSongs);
		mergeSongLists(ddrSongs, sdvxSongs);
		mergeSongLists(ddrSongs, sdvxKoSongs);
		mergeSongLists(ddrSongs, sdvxKoMPSongs);
		mergeSongLists(ddrSongs, popnSongs);
		mergeSongLists(ddrSongs, gitaSongs);
		mergeSongLists(ddrSongs, juSongs);
		
		List<SongInfo> mergedList = new ArrayList<SongInfo>();
		mergedList.addAll(ddrSongs);
		mergedList.addAll(iidxSongs);
		mergedList.addAll(sdvxSongs);
		mergedList.addAll(sdvxKoSongs);
		mergedList.addAll(sdvxKoMPSongs);
		mergedList.addAll(popnSongs);
		mergedList.addAll(gitaSongs);
		mergedList.addAll(juSongs);
		
		System.out.println("Writing sorter data...");
		SorterDataWriter.writeDataJson(mergedList);

	}

	private static List<SongInfo> crawURL(String url, String game, boolean withDownload)
			throws MalformedURLException, IOException, URISyntaxException {
		
		Crawler crawler = new Crawler();
		LocalDateTime dtStartGlobal = LocalDateTime.now();
		
		List<SongInfo> crawledSongs = crawler.crawl(url, game, withDownload);
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

