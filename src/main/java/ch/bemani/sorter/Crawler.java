package ch.bemani.sorter;

import java.awt.image.BufferedImage;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class Crawler {

	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	private static final String JACKETS_SAVE_PATH = "D:\\workspace\\bemani-sorter\\src\\main\\webapp\\assets\\jackets\\";

	public List<SongInfo> crawl(String url, String game, boolean withDownload)
			throws MalformedURLException, IOException, URISyntaxException {
		
		ConnectionManager connManager = new ConnectionManager(url);

		List<SongInfo> songs = new ArrayList<>();
		
		String nextPage = url; 
		
		int i=0;
		
		do {
			i++;
			System.out.println("Parsing page "+i+" for "+game);
			
			connManager.changeUrl(nextPage);
			List<String> pageHTML = connManager.connectAndRead();
			
			songs.addAll(pageHTML.stream().map(HTMLParser::findSong).filter(Objects::nonNull)
					.collect(Collectors.toList()));
			
			nextPage = HTMLParser.nextPage(pageHTML);
						
		} while(nextPage != null);

		
		System.out.println("Parsed " + songs.size() + " songs...");

		if (withDownload) {
			List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

			for (SongInfo song : songs) {
				futures.add(CompletableFuture.runAsync(() -> crawlJacket(connManager, song, game), executorService));
			}

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).join();
		}

		return songs;
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
			System.err.println("[" + song.getTitle() + "]: Could not obtain song jacket: " + e.getMessage());
			return;
		}

		LocalDateTime dtEnd = LocalDateTime.now();

		System.out.println("[" + song.getTitle() + "]: Found song jacked in "
				+ Duration.between(dtStart, dtEnd).toMillis() + "ms.");

//		System.out.print(" Saving jacket...");

		connManager.changeUrl(song.getJacketUrl());
		
		Path savePath = Path.of(JACKETS_SAVE_PATH).resolve(game);
		try {
			Files.createDirectories(savePath);
		} catch (IOException e) {
			System.err.println("[" + song.getTitle() + "]: Error creating jacket save directory: " + e.getMessage());
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

		System.out.println("[" + song.getTitle() + "]: Saved song jacked");
	}

}
