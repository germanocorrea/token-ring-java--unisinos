public class TokenRing {

    static Lock lock = new Lock();

    public static void main(String argv[]) {
        int nodes = 8;
        for (int i = nodes; i >= 0; i--) {
            int nextNodeId = i + 1;
            if (nextNodeId >= 8) {
                nextNodeId = 0;
            }

            int finalNextNodeId = nextNodeId;
            int finalI = i;

            Thread thread = new Thread(() -> {
                lock.lock();
                Node node = new Node(finalI, finalNextNodeId);
                if (finalI == 0) {
                    node.setFirstRun();
                }
                lock.unlock();
                // nao posso fazer lock antes de setSockets,
                // preciso que as threads iniciem em ordem mas continuem independentemente

                try {
                    node.setSockets();
                    node.process();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }

    }

}
