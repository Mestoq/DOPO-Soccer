package presentation;

import domain.Human;
import domain.Match;
import domain.Player;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/** Translates the keyboard into Human.setInput(...). Esc asks the panel to pause. */
public class KeyboardController implements KeyListener {
    private final Match match;
    private final GamePanel panel;
    private final Set<Integer> pressed = new HashSet<>();

    public KeyboardController(Match match, GamePanel panel) {
        this.match = match;
        this.panel = panel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            panel.togglePause();
            return;
        }
        pressed.add(e.getKeyCode());
        push();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressed.remove(e.getKeyCode());
        push();
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    public void releaseAll() {
        pressed.clear();
        push();
    }

    private boolean k(int code) { return pressed.contains(code); }

    private void push() {
        synchronized (match) {
            match.getPlayer(0).setInput(k(KeyEvent.VK_A), k(KeyEvent.VK_D), k(KeyEvent.VK_W), k(KeyEvent.VK_S), k(KeyEvent.VK_E));
            match.getPlayer(1).setInput(k(KeyEvent.VK_LEFT), k(KeyEvent.VK_RIGHT), k(KeyEvent.VK_UP), k(KeyEvent.VK_L), k(KeyEvent.VK_K));
        }
    }
}
