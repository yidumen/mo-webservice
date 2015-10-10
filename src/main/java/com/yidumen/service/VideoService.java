package com.yidumen.service;

import com.yidumen.service.constant.VideoResolution;
import com.yidumen.service.constant.mediainfo.MediaInfo;
import com.yidumen.service.constant.mediainfo.StreamKind;
import com.yidumen.service.framework.RangeHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info/{file}")
    public Response getVideoInfo(@PathParam("file") String file) {
        final MediaInfo mediaInfo = new MediaInfo();
        final StringBuilder sb = new StringBuilder("{");
        final File root = new File(rootPath);
        final File videoDir = new File(root, "video");
        final File flow = new File(videoDir, "180/" + file + "_180.mp4");
        if (!flow.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        mediaInfo.Open(flow.getAbsolutePath());
        sb.append("\"Duration\":").append(mediaInfo.Get(StreamKind.General, 0, "Duration")).append(",");
        sb.append("\"DurationString\":\"").append(mediaInfo.Get(StreamKind.General, 0, "Duration/String3")).append("\",");
        sb.append("\"EncodedApplication\":\"").append(mediaInfo.Get(StreamKind.General, 0, "Encoded_Application")).append("\",");
        sb.append("\"EncodedLibrary\":\"").append(mediaInfo.Get(StreamKind.General, 0, "Encoded_Library")).append("\",");
        sb.append("\"InternetMediaType\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "InternetMediaType")).append("\",");
        sb.append("\"PixelAspectRatio\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "PixelAspectRatio")).append("\",");
        sb.append("\"DisplayAspectRatio\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "DisplayAspectRatio/String")).append("\",");
        sb.append("\"FrameRate\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "FrameRate/String")).append("\",");
        sb.append("\"extInfo\":[");
        mediaInfo.Close();
        final VideoResolution[] resolutions = VideoResolution.values();
        for (int f = 0, last = resolutions.length - 1; f < resolutions.length; f++) {
            VideoResolution vr = resolutions[f];
            final File resDir = new File(videoDir, vr.getResolution());
            final File[] resFiles = resDir.listFiles();
            for (int i = 0, max = resFiles.length; i < max; i++) {
                final File video = resFiles[i];
                if (!video.getName().contains(file)) {
                    continue;
                }
                mediaInfo.Open(video.getAbsolutePath());
                sb.append("{");
                sb.append("\"FileName\":\"").append(mediaInfo.Get(StreamKind.General, 0, "FileName")).append(".").append(mediaInfo.Get(StreamKind.General, 0, "FileExtension")).append("\",");
                sb.append("\"FileSize\":").append(mediaInfo.Get(StreamKind.General, 0, "FileSize")).append(",");
                sb.append("\"FileSizeString\":\"").append(mediaInfo.Get(StreamKind.General, 0, "FileSize/String4")).append("\",");
                sb.append("\"Modified\":\"").append(mediaInfo.Get(StreamKind.General, 0, "File_Modified_Date_Local")).append("\",");
                sb.append("\"BitRate\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "BitRate_Nominal/String")).append("\",");
                sb.append("\"Width\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "Width")).append("\",");
                sb.append("\"Height\":\"").append(mediaInfo.Get(StreamKind.Video, 0, "Height")).append("\"}");
                mediaInfo.Close();
                if (f < last) {
                    sb.append(",");
                }
            }
        }
        sb.append("]}");
        return Response.ok(sb.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/dl/{resolution}/{file}")
    public void downloadVideo(@PathParam("resolution") String resolution,
                              @PathParam("file") String file,
                              @HeaderParam(HttpHeaders.IF_MODIFIED_SINCE) Date lastModified,
                              @HeaderParam("Range") RangeHeader range,
                              @Context HttpServletResponse response) throws IOException {
        final File root = new File(rootPath);
        final File videoDir = new File(root, "video");
        final File flow = new File(videoDir, resolution + "/" + file + "_" + resolution + ".mp4");
        //1.检测文件是否存在，否则返回404
        if (!flow.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        //2.对比LastModefied决定是否返回304
        if (FileUtils.isFileNewer(flow, lastModified)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            final PrintWriter writer = response.getWriter();
            writer.flush();
            writer.close();
            return;
        }
        //3.下载文件
        //3.1 设置Content-length
        response.setContentLengthLong(flow.length());
        //3.2 检测Range信息，支持断点续传
        long to = range.getTo();
        if (to < 0) {
            to = flow.length();
        }
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range", "bytes " + range.getFrom() + "-" + to + "/" + flow.length());
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, flow.lastModified());
        response.setHeader("content-encoding", "identity");
        final InputStream jsonStream = Request.Get("http://www.yidumen.com/ajax/video/" + file).execute().returnContent().asStream();
        final JsonReader reader = Json.createReader(jsonStream);
        final JsonObject jsonObject = reader.readObject();
        final String title = jsonObject.getString("title");
        final String filename = URLEncoder.encode(file + "_" + title + "_" + resolution + ".mp4", "utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\";filename*=utf-8''"+filename);
        //3.3 输出
        IOUtils.copyLarge(FileUtils.openInputStream(flow), response.getOutputStream(), range.getFrom(), to - range.getFrom() + 1);
    }
}
