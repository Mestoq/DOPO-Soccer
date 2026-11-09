package presentation;

import domain.Match;

/**
 * Corre el dominio constantemente para conocer que presentar por cada frame
 * */
public class GameLoop implements Runnable {
    private static final double STEP = 1.0 / 60;
    private static final int MAX_STEPS_PER_FRAME = 12;

    private final Match match;
    private final Runnable onFrame;
    private final Runnable onFinished;
    private volatile boolean running;
    private Thread thread;

    public GameLoop(Match match, Runnable onFrame, Runnable onFinished) {
        this.match = match;
        this.onFrame = onFrame;
        this.onFinished = onFinished;
    }

    public void start() {
        running = true;
        thread = new Thread(this, "game-loop");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        long last = System.nanoTime();
        double acc = 0;
        while (running) {
            long now = System.nanoTime();
            acc += Math.min((now - last) / 1e9, 0.1);
            last = now;
            boolean finished;
            synchronized (match) {
                int steps = 0;
                while (acc >= STEP && steps < MAX_STEPS_PER_FRAME) {
                    match.update(STEP);
                    acc -= STEP;
                    steps++;
                }
                if (steps == MAX_STEPS_PER_FRAME) acc = 0;
                finished = match.isFinished();
            }
            onFrame.run();
            if (finished) {
                running = false;
                onFinished.run();
                return;
            }
            try {
                Thread.sleep(8);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
