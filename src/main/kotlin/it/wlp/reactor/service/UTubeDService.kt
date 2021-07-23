package it.wlp.reactor.service

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo
import com.github.kiulian.downloader.model.videos.formats.Format
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.common.collect.ImmutableList
import io.jsonwebtoken.lang.Collections
import it.sauronsoftware.jave.AudioAttributes
import it.sauronsoftware.jave.Encoder
import it.sauronsoftware.jave.EncodingAttributes
import it.wlp.reactor.config.ConfigProperties
import it.wlp.reactor.model.SearchResult
import it.wlp.reactor.model.VideoInfoModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors

@Service
class UTubeDService {

    @Autowired
    lateinit var youtubeDownloader: YoutubeDownloader

    @Autowired
    lateinit var configProperties: ConfigProperties

    fun downloadMp4(info: VideoInfoModel) :  Mono<InputStreamResource>{


        return Mono.justOrEmpty(info).map {

            val requestVideoInfo = RequestVideoInfo(info.idv);

            val videoInfo = youtubeDownloader!!.getVideoInfo(requestVideoInfo).data();

            val format = videoInfo.formats().get(0)

            val outputDir = kotlin.io.createTempDir("videos_");

            outputDir.deleteOnExit()

            val requestVideoFileDownload = RequestVideoFileDownload(format)
                .saveTo(outputDir) // by default "videos" directory
                .renameTo("video") // by default file name will be same as video title on youtube
                .overwriteIfExists(true);

            val response = youtubeDownloader!!.downloadVideoFile(requestVideoFileDownload);

            return@map InputStreamResource(response.data().inputStream()) // will block current thread
        }

    }

    fun downloadMp3(info: VideoInfoModel) : Mono<InputStreamResource> {

        return Mono.justOrEmpty(info).map {

            val requestVideoInfo = RequestVideoInfo(info.idv);

            val videoInfo = youtubeDownloader!!.getVideoInfo(requestVideoInfo).data();

            val format = videoInfo.formats().get(0)

            val outputDir = kotlin.io.createTempDir("videos_");

            outputDir.deleteOnExit()

            val requestVideoFileDownload = RequestVideoFileDownload(format)
                .saveTo(outputDir) // by default "videos" directory
                .renameTo("mp3") // by default file name will be same as video title on youtube
                .overwriteIfExists(true);

            val source = youtubeDownloader!!.downloadVideoFile(requestVideoFileDownload).data();

            val target = kotlin.io.createTempFile("videos_", ".mp3", outputDir);

            val audio	= AudioAttributes()

            audio.setCodec("libmp3lame")

            audio.setBitRate(128000)
            audio.setChannels(2)
            audio.setSamplingRate(44100)

            val attrs = EncodingAttributes();

            attrs.setFormat("mp3");
            attrs.setAudioAttributes(audio);

            val encoder = Encoder();

             encoder.encode(source, target, attrs)

            return@map InputStreamResource(target.inputStream()) // will block current thread
        }
    }

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