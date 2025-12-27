package Services;

import Models.Wiki.WikipediaSearchResponse;
import Models.Wiki.WikipediaSummaryResponse;

public class WikiSportService {

    private final WikipediaApi wikiApi = new WikipediaApi();

    public WikipediaSummaryResponse getFromText(String input) {
        if (input == null || input.isBlank())
            return null;

        String title = normalize(input);

        // Tentativo diretto
        WikipediaSummaryResponse direct = wikiApi.getSummary(title);
        if (isValid(direct))
            return direct;

        // Fallback
        WikipediaSearchResponse search = wikiApi.search(input);
        if (search == null || search.pages == null || search.pages.isEmpty())
            return null;

        return wikiApi.getSummary(search.pages.getFirst().title);
    }

    public WikipediaSummaryResponse getFromUrl(String url) {
        if (!isWikipediaUrl(url))
            return null;

        String title = extractTitleFromUrl(url);
        if (title == null)
            return null;

        return wikiApi.getSummary(title);
    }

    private boolean isValid(WikipediaSummaryResponse resp) {
        return resp != null && resp.extract != null;
    }

    private boolean isWikipediaUrl(String input) {
        return input != null && input.startsWith("http") && input.contains("wikipedia.org/wiki/");
    }

    private String normalize(String input) {
        return input.trim().replace(" ", "_");
    }

    private String extractTitleFromUrl(String url) {
        if (url == null || url.isBlank())
            return null;

        String marker = "/wiki/";
        int markerIndex = url.indexOf(marker);

        if (markerIndex == -1)
            return null;

        // Prendi tutto dopo "/wiki/"
        String title = url.substring(markerIndex + marker.length());

        // Rimuovi anchor (#...)
        int anchorIndex = title.indexOf("#");
        if (anchorIndex != -1) {
            title = title.substring(0, anchorIndex);
        }

        // Rimuovi query string (?...)
        int queryIndex = title.indexOf("?");
        if (queryIndex != -1) {
            title = title.substring(0, queryIndex);
        }

        if (title.isBlank())
            return null;

        return title;
    }

    public String toWikiCamelCase(String input) {
        if (input == null || input.isBlank())
            return input;

        String[] words = input.trim().toLowerCase().split(" ");

        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty())
                words[i] = w.substring(0, 1).toUpperCase() + w.substring(1);
        }

        return String.join("_", words);
    }

}
