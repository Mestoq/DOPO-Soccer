package presentation;

import domain.DopoSoccerException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class MainMenuPanel extends JPanel {
    private static final String BACKGROUND_FILE = "menu_background.jpg";
    private static final String TITLE_FILE = "title.png";

    private static final int TITLE_MARGIN = 40;
    private static final int BUTTON_MARGIN = 100;
    private static final int TITLE_WIDTH = 460;                             // title width in pixels
    private static final Dimension BUTTON_SIZE = new Dimension(260, 48);    // same size for every button
    private static final int BUTTON_GAP = 14;                               // space between buttons

    private final BufferedImage background = SpriteLoader.load(BACKGROUND_FILE);

    public MainMenuPanel(DopoSoccerGUI gui) {
        setBackground(Theme.BG);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;                          // the column uses the full width
        c.anchor = GridBagConstraints.NORTHWEST; // everything is anchored to the top-left corner

        // Row 0: the title
        c.gridy = 0;
        c.insets = new Insets(TITLE_MARGIN, TITLE_MARGIN, 0, 0);
        add(new TitleComponent(SpriteLoader.load(TITLE_FILE)), c);

        // Rows 1..3: the buttons, one below the other and all the same size
        JButton play = Theme.button("Play");
        play.addActionListener(e -> gui.showConfig());

        JButton open = Theme.button("Open game");
        open.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            try {
                gui.getGame().open(chooser.getSelectedFile().getPath());
            } catch (DopoSoccerException ex) {
                ErrorDialog.show(this, ex);
            }
        });

        JButton exit = Theme.button("Exit");
        exit.addActionListener(e -> System.exit(0));

        int row = 1;
        for (JButton b : new JButton[] {play, open, exit}) {
            b.setPreferredSize(BUTTON_SIZE);
            c.gridy = row++;
            c.insets = new Insets(BUTTON_GAP, BUTTON_MARGIN, 0, 0);
            add(b, c);
        }

        // Empty filler that absorbs the free height, so title and buttons stay at the top.
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        c.gridy = row;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(filler, c);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (background == null) return;
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // "Cover" scaling: fills the whole panel, centered, without distorting the image.
        double s = Math.max(getWidth() / (double) background.getWidth(), getHeight() / (double) background.getHeight());
        int w = (int) Math.ceil(background.getWidth() * s);
        int h = (int) Math.ceil(background.getHeight() * s);
        g.drawImage(background, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        g.dispose();
    }

    /** The title image scaled to TITLE_WIDTH keeping its proportions; plain text when the image is missing. */
    private static class TitleComponent extends JComponent {
        private final BufferedImage image;

        TitleComponent(BufferedImage image) {
            this.image = image;
            int height = image == null ? 70 : TITLE_WIDTH * image.getHeight() / image.getWidth();
            setPreferredSize(new Dimension(TITLE_WIDTH, height));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (image != null) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            } else {
                g.setColor(Theme.TEXT);
                g.setFont(new Font("SansSerif", Font.BOLD, 44));
                g.drawString("DOPOSoccer 2026-2", 0, 50);
            }
            g.dispose();
        }
    }
}