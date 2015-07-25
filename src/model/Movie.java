package model;

import java.io.File;
import java.util.LinkedList;

public class Movie {
	
	/*public enum Genre {
		ACTION,
		ADVENTURE,
		ANIMATED,
		CHILDREN,
		CHRISTMAS,
		COMEDY,
		DISASTER,
		DRAMA,
		FAMILY,
		FANTASY,
		HALLOWEEN,
		KIDS,
		MUSICAL,
		ROMANCE,
		SCIENCE_FICTION,
		THRILLER,
		WAR		
	};*/
	
	private String movieTitle;
	private LinkedList<String> movieGenres;
	
	public Movie(String title) {
		setTitle(title);
		movieGenres = new LinkedList<String>();
	}
		
	public Movie(String title, LinkedList<String> genres) {
		setTitle(title);
		movieGenres = genres;
	}
	
	@Override
	public String toString() {
		return movieTitle;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Movie)
			if (movieTitle.equalsIgnoreCase(((Movie)obj).getTitle()))
				return true;
					
		return false;
	}
	
	public void setTitle(String title) {
		movieTitle = title.trim();
	}
	
	public String getTitle() {
		return movieTitle;
	}
	
	public void addGenre(String genre) {
		movieGenres.add(genre);
	}
	
	public void removeGenre(String genre) {
		movieGenres.remove(genre);
	}
	
	public boolean containsGenre(String genre) {
		return movieGenres.contains(genre);
	}
	
	public int getGenreCount() {
		return movieGenres.size();
	}
	
	public LinkedList<String> getGenres() {
		return movieGenres;
	}
	
	public File getPictureFile() {
		String location = movieTitle.replaceAll("\\(.*\\)", "").trim();
		int subtitleStart = location.indexOf(':');
		if (subtitleStart > 0)
			location = location.substring(0, subtitleStart);
		location = location.replace(' ', '_');
		location = location.replace(".", "");
		location = location.replace("&", "and");
		location = "./" + location;
		location += ".jpg";
		return new File(location);
	}
}
