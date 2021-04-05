import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebSpider {

	private final Set<URL> links;
	private final long startTime;
	private static final String LINK = "#See_also";
	private static String BASE_URL = "";
	private int deep = 1;

	private WebSpider(final URL url, int deep) {
		this.links = new HashSet<>();
		this.deep = deep;
		this.startTime = System.currentTimeMillis();
		// Build the base URL that we will use in the hrefs
		StringBuilder sb = new StringBuilder(url.getProtocol());
		sb.append("://").append(url.getHost());
		BASE_URL = sb.toString();
		crawl(initURLS(url), deep);
	}

	private void crawl(final Set<URL> urls, int level) {
		urls.removeAll(this.links);
		if (level == 0) {
			return;
		}
		if (!urls.isEmpty()) {
			final Set<URL> newURLS = new HashSet<>();
			try {
				this.links.addAll(urls);
				for (final URL url : urls) {
					System.out.println(
							"time = " + (System.currentTimeMillis() - this.startTime) + " connected to : " + url);
					final Document document = Jsoup.connect(url.toString()).get();
					Elements section = document.select("h2 > span#See_also");
					// Check if the section See also exits
					if (section.first() != null) {
						Element h2 = section.first().parent();
						h2 = h2.nextElementSibling();
						while (h2 != null) {
							for (Element child : h2.children()) {
								// Skip the links that are not references to other articles
								if (!child.select("a").isEmpty()) {
									for (Element e : child.select("a")) {
										if (checkURL(child.select("a").first().attr("href"))) {
											System.out.println("New url : " + BASE_URL + e.attr("href"));
											newURLS.add(new URL(BASE_URL + child.select("a").first().attr("href")));
										}
									}

								}

							}
							h2 = h2.nextElementSibling();
							if (stopPoint(h2)) {
								h2 = null;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			crawl(newURLS, level - 1);
		} else {
			System.out.println("No see section for this link!");
		}
	}

	private boolean stopPoint(Element e) {
		if(e == null||e.text() == null) return false;
		return e.text().equals("References[edit]") || e.text().equals("References") || e.text().equals("Notes")
				|| e.text().equals("Notes[edit]")
				|| e.text().equals("Notes and references")
				|| e.text().equals("Notes and references[edit]");
	}

	private Set<URL> initURLS(final URL startURL) {
		return Collections.singleton(startURL);
	}
	
	private boolean checkURL(String url) {
		return url.startsWith("/wiki") &&
				!url.startsWith("/wiki/File");
				
	}

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Introduce the URL : ");
		String url = sc.next();
		System.out.println("Introduce the deepness of the WebSpider: ");
		int deep = sc.nextInt();

		final WebSpider spider = new WebSpider(new URL(url), deep);

	}

}
