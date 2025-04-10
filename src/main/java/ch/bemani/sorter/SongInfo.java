package ch.bemani.sorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SongInfo {

	
	private String title;
	private String jacketUrl;
	private List<String> games;
	private boolean noJacket;
	private boolean matchedOnPattern2;
	
	
	public SongInfo(String title, String jacketUrl) {
		this.title = title;
		this.jacketUrl = jacketUrl;
		noJacket = false;
		matchedOnPattern2 = false;
		games = new ArrayList<>();
	}
	
	public SongInfo(String title, String jacketUrl, boolean matchedOnPattern2) {
		this(title,jacketUrl);
		this.matchedOnPattern2 = matchedOnPattern2;
	}

	public String getTitle() {
		return title;
	}


	public String getJacketUrl() {
		return jacketUrl;
	}
	
	public void setJacketUrl(String jacketUrl) {
		this.jacketUrl = jacketUrl;
	}
	
	@Override
	public String toString() {
		return title + " -> "+jacketUrl;
	}


	public String getTitleForFile() {
		if(noJacket) {
			return "konami";
		}
		return title.replaceAll("[<>:\"\\/\\|?* #\\[\\]]", "_");
	}


	public void addGame(String game) {
		games.add(game);
	}

	public List<String> getGames() {
		return Collections.unmodifiableList(games);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(title);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SongInfo other = (SongInfo) obj;
		return Objects.equals(title, other.title);
	}

	public boolean isNoJacket() {
		return noJacket;
	}

	public void setNoJacket(boolean noJacket) {
		this.noJacket = noJacket;
	}

	public boolean isMatchedOnPattern2() {
		return matchedOnPattern2;
	}

	public void setMatchedOnPattern2(boolean matchedOnPattern2) {
		this.matchedOnPattern2 = matchedOnPattern2;
	}
	
}
