package Models;

import java.util.List;

public class PexelsVideoResponse {
    public List<Video> videos;

    public static class Video {
        public List<VideoFile> video_files;
    }

    public static class VideoFile {
        public String link;
    }
}