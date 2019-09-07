package com.matodak.dailyfaceoff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Test {
	private final static String BASE_URL = "http://www.dailyfaceoff.com";
	private final static String BASE_URL_SECURE = "https://www.dailyfaceoff.com";

	private RestTemplate rest;

	public Test() throws Exception {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
				httpClient);
		rest = new RestTemplate(clientHttpRequestFactory);
	}

	public void run() {
		// Change debug level
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);

		ResponseEntity<String> responseEntity = rest.exchange(BASE_URL + "/teams/", HttpMethod.GET, null, String.class);
		String response = responseEntity.getBody().toString();
		Pattern p = Pattern.compile(
				"href=[\"]" + BASE_URL_SECURE + "/teams/([\\p{L}-\\.]*)/line-combinations[/]?[\"]>([\\p{L}\\s\\.]*)<");
		Matcher m = p.matcher(response);

		Set<String> teamNames = new TreeSet<String>();
		Map<String, String> teamUrls = new HashMap<String, String>();
		while (m.find()) {
			String teamName = m.group(2);
			String teamUrl = BASE_URL + "/teams/" + m.group(1) + "/line-combinations/";
			if (teamName.isEmpty()) {
				continue;
			}

			// Remove the team name that has the same URL as this one
			for (Iterator<String> teamUrlsIter = teamUrls.keySet().iterator(); teamUrlsIter.hasNext();) {
				String currTeamName = teamUrlsIter.next();
				String currTeamUrl = teamUrls.get(currTeamName);
				if (currTeamUrl.equals(teamUrl)) {
					teamNames.remove(currTeamName);
				}
			}

			teamNames.add(teamName);
			teamUrls.put(teamName, teamUrl);
		}

		List<Team> teams = new ArrayList<Team>();
		for (String teamName : teamNames) {
			String teamUrl = teamUrls.get(teamName);

			Team team = new Team();
			team.name = teamName;
			team.url = teamUrl;
			teams.add(team);
		}

		for (Team team : teams) {
			System.out.println("Team name: " + team.name + ", url: " + team.url);
		}
	}

	public void run2(String teamUrl) {
		ResponseEntity<String> responseEntity = rest.exchange(teamUrl, HttpMethod.GET, null, String.class);
		String response = responseEntity.getBody().toString();
		// System.out.println("Response: " + response);

		List<String> playerLastNames = new LinkedList<String>();
		String[] playerPositions = { "LW1", "C1", "RW1", "LW2", "C2", "RW2", "LW3", "C3", "RW3", "LW4", "C4", "RW4",
				"LD1", "RD1", "LD2", "RD2", "LD3", "RD3" };
		for (String playerPosition : playerPositions) {
			String playerLastName = getPlayerLastName(response, playerPosition);
			if (playerLastName != null) {
				playerLastNames.add(playerLastName);
			} else {
				playerLastNames.add("");
			}
		}

		System.out.println(playerLastNames.size());
		for (String playerLastName : playerLastNames) {
			System.out.println("Player: " + playerLastName);
		}
	}

	private String getPlayerLastName(String response, String playerPosition) {
		Pattern p = Pattern.compile("<td id=\"" + playerPosition + "\">\\s.*alt=\"([\\p{L}\\s\\.'-]*)\"");
		Matcher m = p.matcher(response);

		if (m.find()) {
			// System.out.println("Found: " + playerPosition + " " +
			// m.group(1));
			String playerName = m.group(1);
			String[] playerNameParts = playerName.split("[\\s]+");

			// Build the player's last name, it could contains more than one
			// word
			String playerLastName = "";
			for (int i = 1; i < playerNameParts.length; i++) {
				playerLastName += playerNameParts[i];
				if (i < playerNameParts.length - 1) {
					playerLastName += " ";
				}
			}

			return playerLastName;
		}

		return "NotAvailable";
	}

	public static void main(String[] args) throws Exception {
		new Test().run();
		new Test().run2(BASE_URL + "/teams/anaheim-ducks/line-combinations/");
		new Test().run2(BASE_URL + "/teams/arizona-coyotes/line-combinations/");
		new Test().run2(BASE_URL + "/teams/boston-bruins/line-combinations/");
		new Test().run2(BASE_URL + "/teams/buffalo-sabres/line-combinations/");
		new Test().run2(BASE_URL + "/teams/calgary-flames/line-combinations/");
		new Test().run2(BASE_URL + "/teams/carolina-hurricanes/line-combinations/");
		new Test().run2(BASE_URL + "/teams/chicago-blackhawks/line-combinations/");
		new Test().run2(BASE_URL + "/teams/colorado-avalanche/line-combinations/");
		new Test().run2(BASE_URL + "/teams/columbus-blue-jackets/line-combinations/");
		new Test().run2(BASE_URL + "/teams/dallas-stars/line-combinations/");
		new Test().run2(BASE_URL + "/teams/detroit-red-wings/line-combinations/");
		new Test().run2(BASE_URL + "/teams/edmonton-oilers/line-combinations/");
		new Test().run2(BASE_URL + "/teams/florida-panthers/line-combinations/");
		new Test().run2(BASE_URL + "/teams/los-angeles-kings/line-combinations/");
		new Test().run2(BASE_URL + "/teams/minnesota-wild/line-combinations/");
		new Test().run2(BASE_URL + "/teams/montreal-canadiens/line-combinations/");
		new Test().run2(BASE_URL + "/teams/nashville-predators/line-combinations/");
		new Test().run2(BASE_URL + "/teams/new-jersey-devils/line-combinations/");
		new Test().run2(BASE_URL + "/teams/new-york-islanders/line-combinations/");
		new Test().run2(BASE_URL + "/teams/new-york-rangers/line-combinations/");
		new Test().run2(BASE_URL + "/teams/ottawa-senators/line-combinations/");
		new Test().run2(BASE_URL + "/teams/philadelphia-flyers/line-combinations/");
		new Test().run2(BASE_URL + "/teams/pittsburgh-penguins/line-combinations/");
		new Test().run2(BASE_URL + "/teams/san-jose-sharks/line-combinations/");
		new Test().run2(BASE_URL + "/teams/st-louis-blues/line-combinations/");
		new Test().run2(BASE_URL + "/teams/tampa-bay-lightning/line-combinations/");
		new Test().run2(BASE_URL + "/teams/toronto-maple-leafs/line-combinations/");
		new Test().run2(BASE_URL + "/teams/vancouver-canucks/line-combinations/");
		new Test().run2(BASE_URL + "/teams/vegas-golden-knights/line-combinations/");
		new Test().run2(BASE_URL + "/teams/washington-capitals/line-combinations/");
		new Test().run2(BASE_URL + "/teams/winnipeg-jets/line-combinations/");
	}

	class Team {
		String name;
		String url;
	}
}