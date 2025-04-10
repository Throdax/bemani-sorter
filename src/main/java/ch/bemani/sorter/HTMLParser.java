package ch.bemani.sorter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HTMLParser {

	private static final Pattern songLinePattern = Pattern
			.compile(".*<li><a href=\\\"([\\/\\w_()]+)\\\" title=\\\"([\\/\\w_()\\s]+)\\\".+");
	
	private static final Pattern songLinePattern2 = Pattern
			.compile("<td><a href=\\\"([\\/\\w]+)\\\".+\\\">(.+)</a>.+");
	
	private static final Pattern jacketLinePattern = Pattern
			.compile(".+<a href=\\\"(.+)\\\" class=\\\"internal\\\" title=\\\"Enlarge\\\">.+jacket.+");
	private static final Pattern imagePattern = Pattern
			.compile("<div class=\\\"fullMedia\\\"><p><a href=\\\"(.+)\\\" class=\\\"internal\\\".+");

	// </p>(previous page) (<a
	// href="/index.php?title=Category:SOUND_VOLTEX_Songs&amp;pagefrom=Blue+Forest+%28Prog+Keys+Remix%29#mw-pages"
	// title="Category:SOUND VOLTEX Songs">next page</a>)
	private static final Pattern nextPage = Pattern.compile(".+href=\\\"(.+)\\\"\\s.+>next page<\\/a>.+");

	public static SongInfo findSong(String htmlLine) {

		Matcher matcher = songLinePattern.matcher(htmlLine);
		if (matcher.matches()) {
			return new SongInfo(matcher.group(2), matcher.group(1));
		}
		
		matcher = songLinePattern2.matcher(htmlLine);
		if (matcher.matches()) {
			return new SongInfo(matcher.group(2), matcher.group(1),true);
		}

		return null;
	}

	public static void findJacket(SongInfo songInfo) throws Exception {

		try {
			ConnectionManager connManager = new ConnectionManager(songInfo.getJacketUrl());

			List<String> pageHTML = connManager.connectAndRead();

			// In case of multiple jacked per song, like SDVX, we all all URLs and assume
			// the last one is the highest level.
			List<String> jacketUrls = pageHTML.stream().map(HTMLParser::findJacketUrl).filter(Objects::nonNull)
					.collect(Collectors.toList());

			connManager.changeUrl(jacketUrls.getLast());
			List<String> jacketPageHTML = connManager.connectAndRead();

			String realJacket = jacketPageHTML.stream().map(HTMLParser::findLargeJacket).filter(Objects::nonNull)
					.findFirst().orElseThrow(() -> new CrawlerException("No matching pattern for jacket"));

			songInfo.setJacketUrl(realJacket);

		} catch (IOException | URISyntaxException | CrawlerException e) {
			System.err.println(e.getMessage());
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

	public static String nextPage(List<String> pageHTML) {

		for (String htmlLine : pageHTML) {

			Matcher matcher = nextPage.matcher(htmlLine);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		
		return null;
	}

}
