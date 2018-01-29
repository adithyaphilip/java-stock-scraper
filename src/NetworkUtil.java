import com.sun.istack.internal.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NetworkUtil {
    private static String URL_FMT = "https://www.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=%s"
            + "&segmentLink=3&symbolCount=1&series=ALL&dateRange=+&fromDate=%s&toDate=%s&dataType=PRICEVOLUMEDELIVERABLE";

    private static String FIRST_DATE = "01-01-%d";
    private static String LAST_DATE = "31-12-%d";

    public static String getHtmlFor(String symbol, int year) throws IOException {
        symbol = URLEncoder.encode(symbol, "UTF-8");
        String urlString = String.format(URL_FMT, symbol, String.format(FIRST_DATE, year),
                String.format(LAST_DATE, year));

        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Referer","https://www.nseindia.com/products/content/equities/equities/eq_security.htm");
        Scanner sc = new Scanner(urlConnection.getInputStream());
        String result = "";
        while (sc.hasNextLine()) {
            result += sc.nextLine() + "\n";
        }
        return result;
    }

    public static List<String> getAllSymbols() {
        ArrayList<String> symbols = new ArrayList<>();
        Scanner sc = null;
        try {
            sc = new Scanner(new File("unobtained"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (sc.hasNextLine()) {
            symbols.add(sc.nextLine());
        }

        return symbols;
    }

    @NotNull
    public static ArrayList<ArrayList<String>> getRows(String symbol, int year) throws IOException {
        symbol = URLEncoder.encode(symbol, "UTF-8");
        String urlString = String.format(URL_FMT, symbol, String.format(FIRST_DATE, year),
                String.format(LAST_DATE, year));
        Document document = Jsoup.parse(getHtmlFor(symbol, year));
        Element div = document.getElementById("csvContentDiv");
        if (div == null) {
            return new ArrayList<>();
        }
        String content = div.text();
        ArrayList<ArrayList<String>> lines = new ArrayList<>();
        for (String line : content.split(":")) {
            ArrayList<String> fields = new ArrayList<>();
            for (String field : line.split(",")) {
                field = field.trim().substring(1, field.length() - 1).trim();
                fields.add(field);
            }
            lines.add(fields);
        }
        return lines;
    }
}
