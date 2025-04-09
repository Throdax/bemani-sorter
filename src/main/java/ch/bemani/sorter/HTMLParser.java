package ch.bemani.sorter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLParser {

	private static final Pattern songLinePattern = Pattern
			.compile(".*<li><a href=\\\"([\\/\\w_()]+)\\\" title=\\\"([\\/\\w_()\\s]+)\\\".+");
	private static final Pattern jacketLinePattern = Pattern
			.compile(".+<a href=\\\"(.+)\\\" class=\\\"internal\\\" title=\\\"Enlarge\\\">.+jacket.+");
	private static final Pattern imagePattern = Pattern
			.compile("<div class=\\\"fullMedia\\\"><p><a href=\\\"(.+)\\\" class=\\\"internal\\\".+");

	public static SongInfo findSong(String htmlLine) {

		Matcher matcher = songLinePattern.matcher(htmlLine);
		if (matcher.matches()) {
			return new SongInfo(matcher.group(2), matcher.group(1));
		}

		return null;
	}

	public static void findJacket(SongInfo songInfo) throws Exception {

		try {
			ConnectionManager connManager = new ConnectionManager(songInfo.getJacketUrl());

			List<String> pageHTML = connManager.connectAndRead();

			String jacketUrl = pageHTML.stream().map(HTMLParser::findJacketUrl).filter(Objects::nonNull)
					.findFirst().orElseThrow(() -> new CrawlerException());

			connManager.changeUrl(jacketUrl);
			pageHTML = connManager.connectAndRead();

			String realJacket = pageHTML.stream().map(HTMLParser::findLargeJacket).filter(Objects::nonNull)
					.findFirst().orElseThrow(() -> new CrawlerException());

			songInfo.setJacketUrl(realJacket);

		} catch (IOException | URISyntaxException | CrawlerException e) {
			throw e;
		}
	}

	private static String findJacketUrl(String htmlLine) {

		Matcher matcher = jacketLinePattern.matcher(htmlLine);
		if (matcher.matches()) {
			return matcher.group(1);
		}

		return null;
	}

	private static String findLargeJacket(String htmlLine) {
		Matcher matcher = imagePattern.matcher(htmlLine);
		if (matcher.matches()) {
			return matcher.group(1);
		}

		return null;
	}

}
