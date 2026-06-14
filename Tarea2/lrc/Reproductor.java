package lrc;

import javazoom.jlgui.basicplayer.BasicPlayer;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import lrc.parser.Parser;
import lrc.lexer.Lexer;
import lrc.node.Start;
import lrc.analysis.LyricVisitor;
import lrc.analysis.LyricVisitor.LyricEntry;
import javax.swing.*;
import java.awt.*;

public class Reproductor {

    private BasicPlayer player;
    private java.util.Timer timer;
    private List<LyricEntry> lyrics;
    private JLabel lyricLabel;
    private JFrame frame;

    Reproductor() {
        player = new BasicPlayer();
        timer = new java.util.Timer();
    }

    public void Play() throws Exception {
        player.play();
    }

    public void AbrirFichero(String ruta) throws Exception {
        player.open(new File(ruta));
    }

    public void Pausa() throws Exception {
        player.pause();
    }

    public void Continuar() throws Exception {
        player.resume();
    }

    public void Stop() throws Exception {
        player.stop();
        timer.cancel();
    }

    private List<LyricEntry> parsearLRC(String ruta) throws Exception {
        Lexer lexer = new Lexer(new PushbackReader(new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8), 1024));
        Parser parser = new Parser(lexer);
        Start ast = parser.parse();
        LyricVisitor visitor = new LyricVisitor();
        ast.apply(visitor);

        this.lyrics = visitor.getLyrics();
        System.out.println("Artista: " + visitor.getArtista());
        System.out.println("Titulo: " + visitor.getTitulo());
        System.out.println("Album: " + visitor.getAlbum());
        System.out.println("Lineas de letra: " + lyrics.size());

        return lyrics;
    }

    public void iniciarLetra(List<LyricEntry> lyrics) {
        for (LyricEntry entry : lyrics) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String text = entry.getText();
                    System.out.println(text);
                    if (lyricLabel != null) {
                        lyricLabel.setText("<html><center>" + text.replaceAll("\\n", "<br>") + "</center></html>");
                    }
                }
            }, entry.getMilliseconds());
        }
    }

    private void crearVentana() {
        frame = new JFrame("LRC Player");
        lyricLabel = new JLabel("", SwingConstants.CENTER);
        lyricLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        lyricLabel.setForeground(Color.WHITE);
        lyricLabel.setBackground(new Color(30, 30, 30));
        lyricLabel.setOpaque(true);

        frame.setLayout(new BorderLayout());
        frame.add(lyricLabel, BorderLayout.CENTER);
        frame.setSize(600, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String args[]) {
        try {
            String mp3Path;
            String lrcPath;

            if (args.length >= 2) {
                mp3Path = args[0];
                lrcPath = args[1];
            } else if (args.length == 1) {
                mp3Path = "audio/" + args[0];
                lrcPath = "lyrics/" + args[0].replaceAll("(?i)\\.mp3$", ".lrc");
            } else {
                mp3Path = "audio/cancion.mp3";
                lrcPath = "lyrics/cancion.lrc";
            }

            Reproductor reproductor = new Reproductor();
            reproductor.crearVentana();
            reproductor.parsearLRC(lrcPath);
            reproductor.AbrirFichero(mp3Path);
            reproductor.iniciarLetra(reproductor.lyrics);
            reproductor.Play();

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
