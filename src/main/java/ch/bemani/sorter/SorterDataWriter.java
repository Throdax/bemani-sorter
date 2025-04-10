package ch.bemani.sorter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.stream.JsonGenerator;

public class SorterDataWriter {

	public static void writeDataJson(List<SongInfo> songs) throws IOException {
		JsonArrayBuilder jsonArray = Json.createArrayBuilder();

		songs.stream()
				.map(s -> Json.createObjectBuilder().add("name", s.getTitle())
						.add("img", s.getGames().get(0) + "/" + s.getTitleForFile() + ".png")
						.add("opts", Json.createObjectBuilder().add("game", songGameArray(s)).build()).build())
				.forEach(jsonArray::add);

		Path savePath = Path.of("D:\\workspace\\bemani-sorter\\src\\main\\webapp\\js\\data\\").resolve("crawler");
		Files.createDirectories(savePath);

		JsonArray sorterArray = jsonArray.build();

		try (OutputStream sorterDataOut = Files.newOutputStream(savePath.resolve("songdata.js"),
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
			Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true)).createWriter(sorterDataOut)
					.writeArray(sorterArray);
		}

	}

	private static JsonArrayBuilder songGameArray(SongInfo song) {

		JsonArrayBuilder builder = Json.createArrayBuilder();
		song.getGames().forEach(builder::add);
		return builder;
	}

}
