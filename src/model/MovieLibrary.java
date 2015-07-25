package model;

import java.util.LinkedList;

import xml.XMLException;
import xml.XMLReader;
import xml.XMLTag;
import xml.XMLWriter;

public class MovieLibrary {

	private static LinkedList<String> libraryGenres;
	private static LinkedList<Movie> libraryMovies;
	
	public static boolean addGenre(String genre) {
		genre = genre.trim();
		int genreIdx;
		for (genreIdx = 0; genreIdx < libraryGenres.size(); genreIdx++) {
			int compareVal = genre.compareToIgnoreCase(libraryGenres.get(genreIdx));
			if (compareVal == 0)
				return false;
			else if (compareVal < 0)
				break;
		}
		
		libraryGenres.add(genreIdx, genre);
		return true;
	}
	
	public static LinkedList<String> getGenres() {
		return libraryGenres;
	}
	
	public static boolean addMovie(Movie movie) {
		if (movie == null)
			return false;
		
		String title = movie.getTitle().replaceFirst("^([Tt][Hh][Ee]\\ )|^([Aa][Nn]?\\ )", "");
		int movieIdx;
		for (movieIdx = 0; movieIdx < libraryMovies.size(); movieIdx++) {
			String compareTitle = libraryMovies.get(movieIdx).getTitle().replaceFirst("^([Tt][Hh][Ee]\\ )|^([Aa][Nn]?\\ )", "");
			int compareVal = title.compareToIgnoreCase(compareTitle);
			if (compareVal == 0)
				return false;
			else if (compareVal < 0)
				break;
		}
		
		libraryMovies.add(movieIdx, movie);
		return true;
	}
	
	public static void removeMovie(Movie movie) {
		libraryMovies.remove(movie);
	}
		
	public static LinkedList<Movie> getMovies() {
		return libraryMovies;
	}
		
	public static LinkedList<Movie> getAnyMoviesOfGenre(LinkedList<String> genres) {
		LinkedList<Movie> genredMovies = new LinkedList<Movie>();
		for (Movie movie : libraryMovies) {
			for (String genre : genres) {
				if (movie.containsGenre(genre)) {
					genredMovies.add(movie);
					break;
				}
			}
		}
		return genredMovies;
	}
	
	public static LinkedList<Movie> getOnlyMoviesOfGenre(LinkedList<String> genres) {
		LinkedList<Movie> genredMovies = new LinkedList<Movie>();
		if (genres.isEmpty())
			return genredMovies;
		
		genredMovies.addAll(libraryMovies);
		for (Movie movie : libraryMovies)
			for (String genre : genres)
				if (!movie.containsGenre(genre))
					genredMovies.remove(movie);
		
		return genredMovies;
	}
	
	public static void LoadLibrary() throws XMLException {
		libraryGenres = new LinkedList<String>();
		libraryMovies = new LinkedList<Movie>();
		
		XMLTag libraryTag = XMLReader.parseXMLFile("MovieLibrary.xml").getSubTags().getFirst();
		if (!libraryTag.getName().equalsIgnoreCase("MovieLibrary"))
			throw new XMLException("Expected main tag \"Movie Library\"; found \"" + libraryTag.getName() + "\".");
		
		for (XMLTag movieTag : libraryTag.getSubTags()) {
			if (movieTag.getName().equalsIgnoreCase("Movie")) {
				Movie movie = new Movie(movieTag.getContent());
				addMovie(movie);
				String errorMessage = " under \"" + movieTag.getContent() + "\" Movie.";
				for (XMLTag subTag : movieTag.getSubTags()) {
					if (subTag.getName().equalsIgnoreCase("Genre")) {
						String genre = subTag.getContent();
						addGenre(genre);
						movie.addGenre(genre);
					}
					else
						throw new XMLException("Expected \"Genre\" tag; found \"" + subTag.getName() + "\"" + errorMessage);
				}
			}
			else
				throw new XMLException("Expected \"Movie\" tag; found \"" + movieTag.getName() + "\".");
		}
	}
	
	public static void SaveLibrary() throws XMLException {
		XMLTag rootTag = new XMLTag();
		
		XMLTag libraryTag = new XMLTag("MovieLibrary");
		for (Movie movie : libraryMovies) {
			XMLTag movieTag = new XMLTag("Movie");
			movieTag.setContent(movie.getTitle());
			
			for (String genre : movie.getGenres()) {
				XMLTag genreTag = new XMLTag("Genre");
				genreTag.setContent(genre);
				movieTag.addSubTag(genreTag);
			}
			
			libraryTag.addSubTag(movieTag);
		}
		rootTag.addSubTag(libraryTag);
		
		XMLWriter.writeXMLToFile(rootTag, "MovieLibrary.xml");
	}
}
