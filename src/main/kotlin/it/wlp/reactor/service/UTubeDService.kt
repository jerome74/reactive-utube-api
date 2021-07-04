package it.wlp.reactor.service

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.common.collect.ImmutableList
import io.jsonwebtoken.lang.Collections
import it.wlp.reactor.config.ConfigProperties
import it.wlp.reactor.model.SearchResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

@Service
class UTubeDService {

    @Autowired
    lateinit var youtubeDownloader: YoutubeDownloader

    @Autowired
    lateinit var configProperties: ConfigProperties


    fun findVideo(research: String): Flux<SearchResult> {


        return Mono.just(
            YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory(), HttpRequestInitializer() {})
                .setApplicationName("youtube-cmdline-search-sample").build()
        )
            .map {

                // Define the API request for retrieving search results.
                val search = it.Search().list("id,snippet");

                // Set your developer key from the Google Cloud Console for
                // non-authenticated requests. See:
                // https://cloud.google.com/console
                search.setKey(configProperties.apikey);
                search.setQ(research);
                search.setFields("items(id/videoId,snippet/thumbnails/default/url)");
                search.setMaxResults(15);

                // Call the API and print results.
                val searchResponse = search.execute()
                val searchResultList = searchResponse.getItems();

                val videosId =
                    ImmutableList.copyOf(searchResultList.iterator()).stream().filter { !it.id.videoId.isNullOrBlank() }
                        .collect(Collectors.toList())

                return@map videosId;

            }.map {

                var sdf = SimpleDateFormat("HH:mm:ss")
                sdf.timeZone = TimeZone.getTimeZone("GMT - 01:00")

                return@map Flux.fromIterable(it).map {

                    var snippet = it.snippet

                    var singleSearchResult = SearchResult()

                    singleSearchResult.id = it.id.videoId

                    val request = RequestVideoInfo(singleSearchResult.id);

                    val video = youtubeDownloader!!.getVideoInfo(request).data();


                    singleSearchResult.etag = ""
                    singleSearchResult.kind = ""
                    singleSearchResult.channelId = ""
                    singleSearchResult.channelTitle = video.details().author()
                    singleSearchResult.description = ""
                    singleSearchResult.title = video.details().title()
                    singleSearchResult.thumbnails = snippet.thumbnails.toString()
                    singleSearchResult.length = sdf.format(Date((video.details().lengthSeconds() * 1000).toLong()))

                    return@map singleSearchResult

                }
            }.flatMapMany { return@flatMapMany it }
    }


}