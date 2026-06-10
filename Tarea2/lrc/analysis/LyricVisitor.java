package lrc.analysis;

import lrc.node.*;
import java.util.ArrayList;
import java.util.List;

public class LyricVisitor extends DepthFirstAdapter {

    public static class LyricEntry {
        private int minutes, seconds, centiseconds;
        private String text;

        public LyricEntry(int minutes, int seconds, int centiseconds, String text) {
            this.minutes = minutes;
            this.seconds = seconds;
            this.centiseconds = centiseconds;
            this.text = text;
        }

        public long getMilliseconds() {
            return (minutes * 60 * 1000L) + (seconds * 1000L) + (centiseconds * 10L);
        }

        public String getText() { return text; }
        public int getMinutes() { return minutes; }
        public int getSeconds() { return seconds; }
        public int getCentiseconds() { return centiseconds; }
    }

    private List<LyricEntry> lyrics = new ArrayList<>();
    private String artista = "", titulo = "", album = "";

    public List<LyricEntry> getLyrics() { return lyrics; }
    public String getArtista() { return artista; }
    public String getTitulo() { return titulo; }
    public String getAlbum() { return album; }

    private boolean isTimestamp(String s) {
        return s.matches("\\d+:\\d+\\.\\d+");
    }

    private LyricEntry parseTimestamp(String content) {
        String[] parts = content.split("[:.]");
        int min = Integer.parseInt(parts[0]);
        int sec = Integer.parseInt(parts[1]);
        int cent = Integer.parseInt(parts[2]);
        return new LyricEntry(min, sec, cent, "");
    }

    @Override
    public void caseATagLineElement(ATagLineElement node) {
        String content = node.getBcontent().getText().trim();

        if (content.contains(":")) {
            int colonIdx = content.indexOf(':');
            String key = content.substring(0, colonIdx).trim().toLowerCase();
            String value = content.substring(colonIdx + 1).trim();

            switch (key) {
                case "ar": artista = value; break;
                case "ti": titulo = value; break;
                case "al": album = value; break;
            }
        } else if (isTimestamp(content)) {
            lyrics.add(parseTimestamp(content));
        }
    }

    @Override
    public void caseATaggedLineElement(ATaggedLineElement node) {
        String tag = node.getTag().getText().trim();
        String text = node.getLyric().getText().trim();

        if (isTimestamp(tag)) {
            LyricEntry entry = parseTimestamp(tag);
            lyrics.add(new LyricEntry(entry.minutes, entry.seconds, entry.centiseconds, text));
        }
    }
}
