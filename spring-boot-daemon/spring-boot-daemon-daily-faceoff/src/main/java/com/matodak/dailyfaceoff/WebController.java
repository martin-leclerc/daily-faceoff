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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class WebController {
	private final static String BASE_URL = "http://www.dailyfaceoff.com";
	private final static String BASE_URL_SECURE = "https://www.dailyfaceoff.com";

	private RestTemplate rest;
	private Map<String, String> teamUrlNames = new HashMap<String, String>();

	public WebController() {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
				httpClient);
		rest = new RestTemplate(clientHttpRequestFactory);

		teamUrlNames.put("ANA", "anaheim-ducks");
		teamUrlNames.put("ARI", "arizona-coyotes");
		teamUrlNames.put("BOS", "boston-bruins");
		teamUrlNames.put("BUF", "buffalo-sabres");
		teamUrlNames.put("CGY", "calgary-flames");
		teamUrlNames.put("CAR", "carolina-hurricanes");
		teamUrlNames.put("CHI", "chicago-blackhawks");
		teamUrlNames.put("COL", "colorado-avalanche");
		teamUrlNames.put("CLS", "columbus-blue-jackets");
		teamUrlNames.put("DAL", "dallas-stars");
		teamUrlNames.put("DET", "detroit-red-wings");
		teamUrlNames.put("EDM", "edmonton-oilers");
		teamUrlNames.put("FLO", "florida-panthers");
		teamUrlNames.put("LA", "los-angeles-kings");
		teamUrlNames.put("MIN", "minnesota-wild");
		teamUrlNames.put("MTL", "montreal-canadiens");
		teamUrlNames.put("NAS", "nashville-predators");
		teamUrlNames.put("NJ", "new-jersey-devils");
		teamUrlNames.put("NYI", "new-york-islanders");
		teamUrlNames.put("NYR", "new-york-rangers");
		teamUrlNames.put("OTT", "ottawa-senators");
		teamUrlNames.put("PHI", "philadelphia-flyers");
		teamUrlNames.put("PIT", "pittsburgh-penguins");
		teamUrlNames.put("SJ", "san-jose-sharks");
		teamUrlNames.put("STL", "st-louis-blues");
		teamUrlNames.put("TB", "tampa-bay-lightning");
		teamUrlNames.put("TOR", "toronto-maple-leafs");
		teamUrlNames.put("VAN", "vancouver-canucks");
		teamUrlNames.put("VEG", "vegas-golden-knights");
		teamUrlNames.put("WAS", "washington-capitals");
		teamUrlNames.put("WIN", "winnipeg-jets");
	}

	@RequestMapping(path = "/{teamShortName}", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
	@ResponseBody
	public String team(@PathVariable String teamShortName, Model model) {
		String teamUrlName = teamUrlNames.get(teamShortName);
		if (teamUrlName != null) {
			List<Team> teams = getTeams();
			for (Team team : teams) {
				if (teamUrlName.equals(team.urlName)) {
					String teamUrl = BASE_URL + "/teams/" + team.urlName + "/line-combinations/";
					return getTeam(teamUrl);
				}
			}
		}

		return "";
	}

	@RequestMapping(path = "/teams", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
	@ResponseBody
	public String teams(Model model) {
		StringBuffer strbuf = new StringBuffer();
		List<Team> teams = getTeams();
		for (Team team : teams) {
			String teamName = team.name;
			String teamUrlName = team.urlName;
			String teamUrl = "/teams/lines/" + teamUrlName;
			String link = "<a href=\"" + teamUrl + "\">" + teamName + "</a><br>";
			strbuf.append(link);
		}

		return strbuf.toString();
	}

	@RequestMapping(path = "/teams/lines/{teamUrlName}", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
	@ResponseBody
	public String teams(@PathVariable String teamUrlName, Model model) {
		String teamUrl = BASE_URL + "/teams/" + teamUrlName + "/line-combinations/";
		return getTeam(teamUrl);
	}

	private String getTeam(String teamUrl) {
		ResponseEntity<String> responseEntity = rest.exchange(teamUrl, HttpMethod.GET, null, String.class);
		String response = responseEntity.getBody().toString();

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

		StringBuffer strbuf = new StringBuffer();
		for (Iterator<String> playerLastNameIter = playerLastNames.iterator(); playerLastNameIter.hasNext();) {
			String playerLastName = playerLastNameIter.next();
			String link = playerLastName;
			if (playerLastNameIter.hasNext()) {
				link += ",";
			}
			strbuf.append(link);
		}

		return strbuf.toString();
	}

	private List<Team> getTeams() {
		ResponseEntity<String> responseEntity = rest.exchange(BASE_URL + "/teams/", HttpMethod.GET, null, String.class);
		String response = responseEntity.getBody().toString();
		Pattern p = Pattern.compile(
				"href=[\"]" + BASE_URL_SECURE + "/teams/([\\p{L}-\\.]*)/line-combinations[/]?[\"]>([\\p{L}\\s\\.]*)<");
		Matcher m = p.matcher(response);

		Set<String> teamNames = new TreeSet<String>();
		Map<String, String> teamUrlNames = new HashMap<String, String>();
		while (m.find()) {
			String teamUrlName = m.group(1);
			String teamName = m.group(2);
			if (teamName.isEmpty()) {
				continue;
			}

			teamNames.add(teamName);
			teamUrlNames.put(teamName, teamUrlName);
		}

		List<Team> ret = new ArrayList<Team>();
		for (String teamName : teamNames) {
			String teamUrlName = teamUrlNames.get(teamName);

			Team team = new Team();
			team.name = teamName;
			team.urlName = teamUrlName;
			ret.add(team);
		}

		return ret;
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

	class Team {
		String name;
		String urlName;
	}
}