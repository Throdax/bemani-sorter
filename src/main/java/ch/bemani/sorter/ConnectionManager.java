package ch.bemani.sorter;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class ConnectionManager {

	private static final String baseURL = "https://remywiki.com";
	private String url;

	public ConnectionManager(String url) {
		this.url = url;
	}

	public void changeUrl(String url) {
		this.url = url;
	}

	public List<String> connectAndRead() throws MalformedURLException, IOException, URISyntaxException {
		HttpURLConnection conn = (HttpURLConnection) new URI(baseURL + url).toURL().openConnection();

		conn.setDoOutput(true);
		conn.connect();

		List<String> pageHTML = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			pageHTML = br.lines().collect(Collectors.toList());
		} finally {
			conn.disconnect();
		}

		return pageHTML;
	}

	public BufferedImage connectAndReadImage() throws IOException, URISyntaxException {

		HttpURLConnection conn = (HttpURLConnection) new URI(baseURL + url).toURL().openConnection();

		conn.setDoOutput(true);
		conn.connect();

		try {
			return ImageIO.read(conn.getInputStream());
		} finally {
			conn.disconnect();
		}

	}

}
