import static java.lang.Thread.sleep;

public class Lock {

    boolean locked = false;

    public void lock() {
        while (this.locked) {
            try {
                sleep(500);
                System.out.println("Esperando unlock");
            } catch (InterruptedException e) {}
        }
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }
}
