package Search;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class searchRepo {
	private static Gson gson;
	public static ArrayList<String> repolist = new ArrayList<String>();
	public static ArrayList<String> repolistBasedcode = new ArrayList<String>();
	public static ArrayList<String> symptopms = new ArrayList<String>();
	public static String reponame, repolink, path, prepath, newFileName, s1;
	public static String repokeywords, date, stars, forks, size, language, username, token, numOfSymptoms;
	public static int exit = 0;

	public static void main(String[] args) throws ClientProtocolException, IOException, JSONException {
		// Using GSON to parse or print response JSON.
		gson = new GsonBuilder().setPrettyPrinting().create();
		readConfig();
		searchCodeByContent();
		if (exit == 0) {
			System.out.println("\n\nNumber of repositories in the first search : " + repolist.size());
			System.out.println("\nNumber of filtered repositories in the second search : " + repolistBasedcode.size());
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
//Function for collecting the repositories
	private static void searchCodeByContent() throws ClientProtocolException, IOException {
		String incomplete;
		Map repocontentSearchResult;
		double totalcount, totalcount2;
		int i = 0;
		do {
			i++;
			// Query for collecting repositories
			String repoContentQuery = repokeywords + "+in:name,topics,description,readme+pushed:" + date + "+language:"
					+ language + "+size:%3E=" + size + "%20forks:%3E=" + forks + "%20stars:%3E=" + stars + "%20&page="
					+ i + "&per_page=100";
			do {
				repocontentSearchResult = makeRESTCall(
						"https://api.github.com/search/repositories?q=" + repoContentQuery,
						"application/vnd.github.v3.text-match+json");
				totalcount = Double.parseDouble(repocontentSearchResult.get("total_count").toString());
				System.out.println("Total number or results = " + repocontentSearchResult.get("total_count"));
				incomplete = repocontentSearchResult.get("incomplete_results").toString();
				System.out.println("incomplete_results = " + incomplete);
				stopForSecond(15);
			} while (incomplete.equals("true"));

			if (totalcount > 1000) {
				System.out.println(
						"\n\nLimitation! The GitHub API can retrieve only 1000 repositories.\nTotal number of repositories: "
								+ totalcount + "\nYou should choose a shorter time period in the config file.");
				exit = 1;
				return;
			}

			gson.toJsonTree(repocontentSearchResult).getAsJsonObject().get("items").getAsJsonArray().forEach(r -> {
				String fork = r.getAsJsonObject().get("fork").toString();
				if (fork.equals("false"))
					repolist.add(r.getAsJsonObject().get("html_url").toString());
			});

		} while (i * 100 < totalcount);

		System.out.println(repolist.size());

		// Save the repositories in the file
		File file = new File("AllRepositories.csv");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int ii = 0; ii < repolist.size(); ii++) {
			bw.write(repolist.get(ii));
			bw.newLine();
		}
		bw.close();
		fw.close();
		System.out.println("Repositories are saved in the file!");

		String codeContentQuery;
		// Filter the repositories based on the symptoms
		for (int j = 0; j < repolist.size(); j++) {
			System.out.println("****" + j + "*****");
			String repos = repolist.get(j).replace("https://github.com/", "");
			for (int num = 0; num < symptopms.size(); num++) {
				stopForSecond(20);
				codeContentQuery = symptopms.get(num) + "+in:file+repo:" + repos;
				codeContentQuery = codeContentQuery.replace("\"", "");
				String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + token).getBytes());
				URL url = new URL("https://api.github.com/search/code?q=" + codeContentQuery);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				Map jsonMap = gson.fromJson(response.toString(), Map.class);
				totalcount2 = Double.parseDouble(jsonMap.get("total_count").toString());
				if (totalcount2 >= 1) {
					gson.toJsonTree(jsonMap).getAsJsonObject().get("items").getAsJsonArray().forEach(r -> {
						s1 = "Repo: " + r.getAsJsonObject().get("repository").getAsJsonObject().get("html_url");
					});
					s1 = s1.replace("Repo: ", "");
					repolistBasedcode.add(s1);
					break;
				}
			}
		}
		// Save target repositories in the file
		File file2 = new File("FilteredRepositories.csv");
		FileWriter fw2 = new FileWriter(file2);
		BufferedWriter bw2 = new BufferedWriter(fw2);
		for (int k = 0; k < repolistBasedcode.size(); k++) {
			bw2.write(repolistBasedcode.get(k));
			bw2.newLine();
		}
		bw2.close();
		fw2.close();
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Function for making a REST GET call for this URL using Apache http client
	private static Map makeRESTCall(String restUrl, String acceptHeaderValue)
			throws ClientProtocolException, IOException {
		Request request = Request.Get(restUrl);

		if (acceptHeaderValue != null && !acceptHeaderValue.isBlank()) {
			request.addHeader("Accept", acceptHeaderValue);
		}
		Content content = request.execute().returnContent();
		String jsonString = content.asString();

//To print response JSON, using GSON. Any other JSON parser can be used here.
		Map jsonMap = gson.fromJson(jsonString, Map.class);
		return jsonMap;
	}

	private static Map makeRESTCall(String restUrl) throws ClientProtocolException, IOException {
		return makeRESTCall(restUrl, null);
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
//Function for reading the config.json file
	public static void readConfig() {
		JSONParser parser = new JSONParser();
		int numofsym;
		try {
			Object obj = parser.parse(new FileReader("config.json"));
			JSONObject jsonObject = (JSONObject) obj;
			repokeywords = (String) jsonObject.get("keywords");
			date = (String) jsonObject.get("date");
			stars = (String) jsonObject.get("stars");
			forks = (String) jsonObject.get("forks");
			size = (String) jsonObject.get("size");
			language = (String) jsonObject.get("language");
			username = (String) jsonObject.get("username");
			token = (String) jsonObject.get("token");
			numOfSymptoms = (String) jsonObject.get("numOfSymptoms");
			numofsym = Integer.parseInt(numOfSymptoms);
			for (int g = 0; g < numofsym; g++) {
				String symp = "symptom" + g;
				symptopms.add((String) jsonObject.get(symp));
			}

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
//Function for sleeping
	public static void stopForSecond(int second) {
		try {
			Thread.sleep(second * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
