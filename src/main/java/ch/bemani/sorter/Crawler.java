package ch.bemani.sorter;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public class Crawler {

	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	private static final String JACKETS_SAVE_PATH = "D:\\workspace\\bemani-sorter\\src\\main\\webapp\\assets\\jackets\\";

	public List<SongInfo> crawl(String url, String game, boolean withDownload)
			throws MalformedURLException, IOException, URISyntaxException {

		ConnectionManager connManager = new ConnectionManager(url);

		List<SongInfo> songs = new ArrayList<>();

		String nextPage = url;

		connManager.changeUrl(nextPage);
		List<String> pageHTML = connManager.connectAndRead();

//			songs.addAll(
//					pageHTML.stream().map(HTMLParser::findSong).filter(Objects::nonNull).collect(Collectors.toList()));

		Iterator<String> pageIterator = pageHTML.iterator();

		while (pageIterator.hasNext()) {
			String line = (String) pageIterator.next();

			SongInfo songInfo = HTMLParser.findSong(line);

			if (songInfo != null) {
				songs.add(songInfo);
				if (songInfo.isMatchedOnPattern2()) {
					// Skipping 2 lines. The SDVX list is a pain
					pageIterator.next();
					pageIterator.next();
				}
			}
		}

		System.out.println("[" + game + "] Parsed " + songs.size() + " songs...");

		if (withDownload) {
			downloadJackets(connManager, game, songs);
		} else {
			findAlreadyDownloadedJackets(game, songs);
		}

		return songs;
	}

	private void findAlreadyDownloadedJackets(String game, List<SongInfo> songs) {
		System.out.println("["+game+"] Song list parsed without jacket download. Trying to find existing jackets...");
		
		for (SongInfo song : songs) {
			if (!Files.exists(Path.of(JACKETS_SAVE_PATH).resolve(game).resolve(song.getTitleForFile()+".png"))) {
				song.setNoJacket(true);
			}
		}
	}

	private void downloadJackets(ConnectionManager connManager, String game, List<SongInfo> songs) {
		List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

		for (SongInfo song : songs) {
			futures.add(CompletableFuture.runAsync(() -> crawlJacket(connManager, song, game), executorService));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).join();

		executorService.shutdown();
	}

	private void crawlJacket(ConnectionManager connManager, SongInfo song, String game) {

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

//		System.out.print("Finding song jacket for " + song.getTitle() + "....");
		LocalDateTime dtStart = LocalDateTime.now();

		try {
			HTMLParser.findJacket(song);
		} catch (Exception e) {
			System.err.println(
					"[" + game + "] [" + song.getTitle() + "]: Could not obtain song jacket: " + e.getMessage());

			try (BufferedWriter writer = Files.newBufferedWriter(Path.of(JACKETS_SAVE_PATH).resolve("notfound.log"),
					StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

				writer.write("[" + game + "] [" + song.getTitle() + "]: Could not obtain song jacket ('"
						+ ConnectionManager.baseURL + song.getJacketUrl() + "'): " + e.getMessage()
						+ System.lineSeparator());

			} catch (IOException e1) {
				System.err.println("Could not save missing jacket log");
			}
			song.setNoJacket(true);
			return;
		}

		LocalDateTime dtEnd = LocalDateTime.now();

		System.out.println("[" + game + "] [" + song.getTitle() + "]: Found song jacked in "
				+ Duration.between(dtStart, dtEnd).toMillis() + "ms.");

//		System.out.print(" Saving jacket...");

		connManager.changeUrl(song.getJacketUrl());

		Path savePath = Path.of(JACKETS_SAVE_PATH).resolve(game);
		try {
			Files.createDirectories(savePath);
		} catch (IOException e) {
			System.err.println("[" + game + "] [" + song.getTitle() + "]: Error creating jacket save directory: "
					+ e.getMessage());
			return;
		}

		try (OutputStream os = Files.newOutputStream(savePath.resolve(song.getTitleForFile() + ".png"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

			BufferedImage jacket = connManager.connectAndReadImage();
			ImageIO.write(jacket, "png", os);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		System.out.println("[" + game + "] [" + song.getTitle() + "]: Saved song jacked");
	}

}
