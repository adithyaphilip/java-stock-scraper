import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static int NUM_THREADS = 1;
    public static int START_YEAR = 1996;
    public static int END_YEAR = 2018;

    private static int tot = 0;


    public static void main(String[] args) {
        byte b = -1;
        b= (byte) ((b & 0xff) >>> (byte) 4);
        System.out.println((byte) (Math.pow(2, 8) - 1));
        try {
            download(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void download(String[] args) throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        ArrayList<Future<ArrayList<ArrayList<String>>>> all_rows = new ArrayList<>();
        final List<String> symbols = NetworkUtil.getAllSymbols();
//        System.out.println("Symbols:"+symbols);
        for (final String symbol : symbols) {
            System.out.println(symbol);
            Future<ArrayList<ArrayList<String>>> rows = executorService.submit(() -> {
                ArrayList<ArrayList<String>> all_results = new ArrayList<>();

                for (int i = END_YEAR; i >= START_YEAR; i--) {
                    try {
                        ArrayList<ArrayList<String>> results = NetworkUtil.getRows(symbol, i);
                        if (results.size() <= 1) {
                            break;
                        }
                        if (all_results.size() == 0) {
                            all_results.addAll(results);
                        } else {
                            all_results.addAll(1, results);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        i++;
                    }
                }
                tot++;
                System.out.println(tot * 100.0 / symbols.size());
                PrintWriter printWriter = new PrintWriter(new File("DB/"+symbol + ".csv"));
                for (ArrayList<String> fields : all_results) {
                    printWriter.println(String.join(",", fields));
                }
                printWriter.close();
                return all_results;
            });
            all_rows.add(rows);
        }
        System.out.println("Starting at " + System.currentTimeMillis() / 1000);

        for (Future<ArrayList<ArrayList<String>>> future : all_rows) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        //shut down the executor service now
        executorService.shutdown();
    }


}
