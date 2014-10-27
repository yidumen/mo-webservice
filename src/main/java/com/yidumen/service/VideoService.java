package com.yidumen.service;

import com.yidumen.dao.constant.VideoResolution;
import com.yidumen.service.framework.mediainfo.MediaInfo;
import com.yidumen.service.framework.mediainfo.StreamKind;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author 蔡迪旻
 */
@Path("video")
public class VideoService {

    private final static Logger LOG = LoggerFactory.getLogger(VideoService.class);
    @Context
    private ServletContext context;
    private String rootPath;

    /**
     * Creates a new instance of VideoService
     */
    public VideoService() {
    }

    @PostConstruct
    public void init() {
        Properties prop = new Properties();
        try {
            prop.load(context.getResourceAsStream("/WEB-INF/jna.properties"));
        } catch (IOException ex) {
            LOG.info("文件应放在 {}", context.getRealPath("WEB-INF"));
        }
        rootPath = prop.getProperty("resourcePath", "D:/resources");
        System.setProperty("jna.library.path", prop.getProperty("mediaInfo"));
    }

    /**
     * Retrieves representation of an instance of
     * com.yidumen.service.VideoService
     *
     * @param file
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json; charset=utf-8")
    @Path("/info/{file}")
    public String getVideoInfo(@PathParam("file") String file) {
        final MediaInfo mediaInfo = new MediaInfo();
        final StringBuilder sb = new StringBuilder("{");
        final File root = new File(rootPath);
        final File videoDir = new File(root, "video");
        for (VideoResolution vr : VideoResolution.values()) {
            final File resDir = new File(videoDir, vr.getResolution());
            for (File video : resDir.listFiles()) {
                if (!video.getName().contains(file)) {
                    continue;
                }
                mediaInfo.Open(video.getAbsolutePath());
                if (vr.equals(VideoResolution.FLOW)) {
                    sb.append("\"Duration\":").append(mediaInfo.Get(StreamKind.General, 0, "Duration")).append(",");
                    sb.append("\"DurationString\":\"").append(mediaInfo.Get(StreamKind.General, 0, "Duration/String3")).append("\",");
                    sb.append("\"EncodedApplication\":\"").append(mediaInfo.Get(StreamKind.General, 0, "Encoded_Application")).append("\",");
                    sb.append("\"EncodedLibrary\":\"").append(mediaInfo.Get(StreamKind.General, 0, "Encoded_Library")).append("\",");
                    sb.append("\"InternetMediaType\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "InternetMediaType")).append("\",");
                    sb.append("\"PixelAspectRatio\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "PixelAspectRatio")).append("\",");
                    sb.append("\"DisplayAspectRatio\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "DisplayAspectRatio/String")).append("\",");
                    sb.append("\"FrameRate\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "FrameRate/String")).append("\",");
                    sb.append("\"extInfo\":[{");
                    sb.append("\"FileName\":\"").append(mediaInfo.Get(StreamKind.General, 0, "FileName")).append(".").append(mediaInfo.Get(StreamKind.General, 0, "FileExtension")).append("\",");
                    sb.append("\"FileSize\":").append(mediaInfo.Get(StreamKind.General, 0, "FileSize")).append(",");
                    sb.append("\"FileSizeString\":\"").append(mediaInfo.Get(StreamKind.General, 0, "FileSize/String4")).append("\",");
                    sb.append("\"Modified\":\"").append(mediaInfo.Get(StreamKind.General, 0, "File_Modified_Date_Local")).append("\",");
                    sb.append("\"BitRate\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "BitRate_Nominal/String")).append("\",");
                    sb.append("\"Width\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "Width")).append("\",");
                    sb.append("\"Height\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "Height")).append("\"}");
                } else {
                    sb.append(",{");
                    sb.append("\"FileName\":\"").append(mediaInfo.Get(StreamKind.General, 0, "FileName")).append(".").append(mediaInfo.Get(StreamKind.General, 0, "FileExtension")).append("\",");
                    sb.append("\"FileSize\":").append(mediaInfo.Get(StreamKind.General, 0, "FileSize")).append(",");
                    sb.append("\"FileSizeString\":\"").append(mediaInfo.Get(StreamKind.General, 0, "FileSize/String4")).append("\",");
                    sb.append("\"Modified\":\"").append(mediaInfo.Get(StreamKind.General, 0, "File_Modified_Date_Local")).append("\",");
                    sb.append("\"BitRate\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "BitRate_Nominal/String")).append("\",");
                    sb.append("\"Width\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "Width")).append("\",");
                    sb.append("\"Height\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "Height")).append("\"}");
                }
                mediaInfo.Close();
            }
        }
        sb.append("]}");
        return sb.toString();
    }

}
