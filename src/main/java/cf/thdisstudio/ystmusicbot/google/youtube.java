package cf.thdisstudio.ystmusicbot.google;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class youtube {
    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    /** Global instance of the max number of videos we want returned (50 = upper limit per page). */
    private static final long NUMBER_OF_VIDEOS_RETURNED = 5;

    static ResourceId rId;

    static SearchResult singleVideo;

    static YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {
    }).setApplicationName("yellowstrawberry-bot").build();
    //id,snippet

    public static List<String> info(String q){
        try {
            YouTube.Search.List search = youtube.search().list(Arrays.asList("id", "snippet"));
            search.setKey("AIzaSyB3yByyNCA5r310Hrt6u9B1WXAnl_na4pM");
            search.setQ(q);
            search.setType(Collections.singletonList("video"));
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            search.setOrder("relevance");
            SearchListResponse searchResponse = search.execute();

            List<SearchResult> searchResultList = searchResponse.getItems();

            singleVideo = searchResultList.iterator().next();
            rId = singleVideo.getId();
        }catch (Exception e){
            System.out.println(e);
        }
        Thumbnail th = (Thumbnail) singleVideo.getSnippet().getThumbnails().get("default");
        return Arrays.asList(singleVideo.getSnippet().getTitle(), singleVideo.getSnippet().getChannelTitle(), th.getUrl(), rId.getVideoId());
    }
}
