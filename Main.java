package winLossPredictor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Main {
	
	//Dates are needed for the URLs, so this is where they are stored
	private static HashMap<Integer, Integer> dates = new HashMap<>();
	
	//Puts in the date in September when the NFL season started in each associated year
	//First value is year num, second value is date
	static {
		dates.put(0, 15);
		dates.put(1, 14);
		dates.put(2, 12);
		dates.put(3, 11);
		dates.put(4, 10);
		dates.put(5, 16);
		dates.put(6, 14);
		dates.put(7, 13);
		dates.put(8, 12);
		dates.put(9, 11);
	}
	
	//
	public static void main(String[] args) throws IOException, InterruptedException {
		
		//KEY:
		int teamInd = 0;
		int yearInd = 1;
		int weekInd = 2;
		int winLossInd = 3;
		int homeAwayInd = 4;
		int ptsScoredInd = 5;
		int ptsAllowInd = 6;
		int thirdInd = 7;
		int thirdAllowInd = 8;
		int passYdsInd = 9;
		int passYdsAllowInd = 10;
		int rushYdsInd = 11;
		int rushYdsAllowInd = 12;
		
		/* This is the array that all the data is stored. Each row represents one team and their performance
		   for one game */
		double[][] data = new double[4800][13];
		
		//Stores a list of each NFL team to be used when creating URLs to access data
		ArrayList<TeamNode> teams = new ArrayList<>();
		
		//Adds all the NFL teams
		teams.add(new TeamNode(0, "BUF", "Buffalo"));
		teams.add(new TeamNode(1, "MIA", "Miami"));
		teams.add(new TeamNode(2, "NE", "New England"));
		teams.add(new TeamNode(3, "NYJ", "NY Jets"));
		teams.add(new TeamNode(4, "CIN", "Cincinnati"));
		teams.add(new TeamNode(5, "BAL", "Baltimore"));
		teams.add(new TeamNode(6, "PIT", "Pittsburgh"));
		teams.add(new TeamNode(7, "CLE", "Cleveland"));
		teams.add(new TeamNode(8, "JAX", "Jacksonville"));
		teams.add(new TeamNode(9, "TEN", "Tennessee"));
		teams.add(new TeamNode(10, "IND", "Indianapolis"));
		teams.add(new TeamNode(11, "HOU", "Houston"));
		teams.add(new TeamNode(12, "KC", "Kansas City"));
		teams.add(new TeamNode(13, "SD", "LA Chargers"));
		teams.add(new TeamNode(14, "OAK", "LV Raiders"));
		teams.add(new TeamNode(15, "DEN", "Denver"));
		teams.add(new TeamNode(16, "PHI", "Philadelphia"));
		teams.add(new TeamNode(17, "DAL", "Dallas"));
		teams.add(new TeamNode(18, "NYG", "NY Giants"));
		teams.add(new TeamNode(19, "WSH", "Washington"));
		teams.add(new TeamNode(20, "MIN", "Minnesota"));
		teams.add(new TeamNode(21, "DET", "Detroit"));
		teams.add(new TeamNode(22, "GB", "Green Bay"));
		teams.add(new TeamNode(23, "CHI", "Chicago"));
		teams.add(new TeamNode(24, "TB", "Tampa Bay"));
		teams.add(new TeamNode(25, "CAR", "Carolina"));
		teams.add(new TeamNode(26, "NO", "New Orleans"));
		teams.add(new TeamNode(27, "ATL", "Atlanta"));
		teams.add(new TeamNode(28, "SF", "San Francisco"));
		teams.add(new TeamNode(29, "STL", "LA Rams"));
		teams.add(new TeamNode(30, "SEA", "Seattle"));
		teams.add(new TeamNode(31, "ARI", "Arizona"));
		
		/*Skip is needed because if a team has a bye week, there is no point in trying to access data
		   for that team since they didn't play a game, so skip accounts for all the bye weeks */
		int skip = 0;
		// i is the year number, and j is the week number
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 15; j++) {
				// Resets the opposing team for each team
				for (TeamNode k : teams) {
					k.oppTeam = null;
					
					//Changes SD to LAC for Chargers relocation after year 7
					if (k.team.equals("SD") && i >= 7) {
						k.team = "LAC";
					}
					
					//Changes STL to LAR for Rams relocation after year 6
					if (k.team.equals("STL") && i >= 6) {
						k.team = "LAR";
					}
				}
				
				// This URL is needed to access win/loss, home/away, points scored, and points allowed
				URL url = new URL("https://www.espn.com/nfl/schedule/_/week/" + (j + 1) + "/year/201" + i + "/seasontype/2");
				StringBuffer info = getData(url, "<img width=\"99999\" height=\"99999\" alt=\"\"", "<html>");
				
				//Date is needed for the next few URLs, so the data is calculated using the getDateURL function
				String date = getDateURL(i, (j+1));
				
				// Gets third down conversion data
				URL thirdDownURL = new URL("https://www.teamrankings.com/nfl/stat/third-down-conversion-pct" + date);
				StringBuffer thirdDownInfo = getData(thirdDownURL, "<th class=\"text-right\">Away</th>", "</main>");
				
				// Gets passing yards data
				URL passYdsURL = new URL("https://www.teamrankings.com/nfl/stat/passing-yards-per-game" + date);
				StringBuffer passYdsInfo = getData(passYdsURL, "<th class=\"text-right\">Away</th>", "</main>");
				
				// Gets rushing yards data
				URL rushYdsURL = new URL("https://www.teamrankings.com/nfl/stat/rushing-yards-per-game" + date);
				StringBuffer rushYdsInfo = getData(rushYdsURL, "<th class=\"text-right\">Away</th>", "</main>");
				
				// Goes through all 32 teams and adds data in the data array for each of the teams
				for (int t = 0; t < 32; t++) {
					TeamNode curr = teams.get(t); //Current team being looked at
					int points = ptsScored(teams.get(t), info); //gets the points scored by that team
					if (points == -1) { //If points == -1, then that means the team didn't play that week, so skip
						skip++;
					} else { //Else, the team did play that week, so continue adding data
						int currInd = t + (32 * ((i*15) + j)) - skip; //Calculation for current index in array
						
						data[currInd][ptsScoredInd] = points; //Assigns the points scored to the current team
						
						data[currInd][teamInd] = curr.ID; //Adds team ID to array
						data[currInd][yearInd] = i; //Adds year num
						data[currInd][weekInd] = (j+1); //Adds week num
						
						//Assigns third down conversion rate
						data[currInd][thirdInd] = getTeamRankingsData(curr.teamName, thirdDownInfo);
						
						//Assigns pass yards scored
						data[currInd][passYdsInd] = getTeamRankingsData(curr.teamName, passYdsInfo);
						
						//Assigns rush yards scored
						data[currInd][rushYdsInd] = getTeamRankingsData(curr.teamName, rushYdsInfo);
					}
				}
				
				/* Goes through all the teams, and if an opposing team wasn't already assigned by the 
				 * ptsScored function, then it adds in the opposing team
				 */
				for (TeamNode team : teams) {
					if (team.oppTeam == null) {
						for (TeamNode team2 : teams) {
							if (team2.oppTeam != null && team2.oppTeam.equals(team.team)) {
								team.oppTeam = team2.team;
							}
						}
					}
				}
				
				//Assigns all the 'allowed stats' and some miscellaneous stats to the array
				for (TeamNode a : teams) {
					for (TeamNode b : teams) {
						if (a.oppTeam != null && a.oppTeam.equals(b.team)) {
							int spot = (a.ID + (32 * ((i*15) + j))) - skip;
							int oppSpot = (b.ID + (32 * ((i*15) + j))) - skip;
							
							//Assigns points allowed
							data[spot][ptsAllowInd] = data[oppSpot][ptsScoredInd];
							
							//Assigns 3rd down rate allowed
							data[spot][thirdAllowInd] = data[oppSpot][thirdInd];
							
							//Assigns pass yards allowed
							data[spot][passYdsAllowInd] = data[oppSpot][passYdsInd];
							
							//Assigns rush yards allowed
							data[spot][rushYdsAllowInd] = data[oppSpot][rushYdsInd];
							
							//Assigns win/loss
							if (data[spot][ptsScoredInd] > data[spot][ptsAllowInd]) {
								data[spot][winLossInd] = 1;
							} else {
								data[spot][winLossInd] = 0;
							}
							
							//Assigns home/away
							if (a.isHome) {
								data[spot][homeAwayInd] = 1;
							} else {
								data[spot][homeAwayInd] = 0;
							}
						}
					}
				}
				System.out.println("Completed 201" + i + " week " + (j+1));
			}
		}
		
		/* Creates a new csv file to add the data into; will need to change the file location 
		 * if using a different computer */
		String location = "C:\\Users\\neela\\Downloads\\nfl_data.csv";
        File file = new File(location);
		
        //Adds the data into the csv file
		String filePath = location;
		writeArrayToCSV(data, filePath);
	}
	
	/* This function creates a string from the URL that the code can go through and find data. The function
	 * has a start and end parameter, which are the strings that the code identifies to begin the copying
	 * of the html code and end the copying.
	 */
	public static StringBuffer getData(URL url, String start, String end) throws IOException, InterruptedException {
		//Delay put in so the websites aren't overloaded with requests
		Thread.sleep(700);
		
		/* Another safety net for when website is overloaded with requests. Will cause an infinite for loop
		   sometimes though, so should probably delete later */
		boolean gotData = false;
		InputStream in = new StringBufferInputStream(end);
		while (gotData == false) {
			try {
				in = url.openStream();
				gotData = true;
			} catch(IOException e) {
			}
		}
		Scanner scanner = new Scanner(in);

		/* Read HTML from web site one line at a time */
		StringBuffer pageContent = new StringBuffer();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (line.contains(start)) { //If start string is found, start copying html code
				pageContent.append(line);
				while (scanner.hasNext()) {
					line = scanner.nextLine();
					pageContent.append(line);
					if (line.contains(end)) { //If end string is found, stop copying html code
						break;
					}
				}
				break;
			}
		}
		scanner.close();
		return pageContent;
	}
	
	/* This function returns the points scored by the team passed in. If the team is not found in the 
	 * data, then the value -1 is returned to indicate that the team was not found. This is probably the most
	 * useful function to look at to understand data scraping. Open up the ESPN URL, right click, and select
	 * 'view page source' to follow along better
	 */
	public static int ptsScored(TeamNode curr, StringBuffer data) {
		String team = ">" + curr.team + " ";
		int start = data.indexOf(team); //Finds index of team in the string
		if (start == -1) {
			team = " " + curr.team + " ";
			start = data.indexOf(team);
			curr.isHome = false;
		} else {
			curr.isHome = true;
		}
		
		if (start == -1) {
			return -1;
		}
		start++;
		
		int end = data.indexOf(",", start);
		if (end - start > 8 || end == -1) {
			end = data.indexOf("<", start);
		} else {
			int oppsStart = end + 2;
			int oppsEnd = data.indexOf(" ", oppsStart);
			curr.oppTeam = data.substring(oppsStart, oppsEnd);
		}
		
		
		int strEnd = data.indexOf(" ", start) + 1;
		
		if (data.substring(strEnd, end).contains("(OT)")) {
			end = end - 5;
		}
		
		return Integer.parseInt(data.substring(strEnd, end));
	}
	
	// Calculates the date to be used when accessing URLs
	public static String getDateURL(int year, int week) throws MalformedURLException {
		int yr = year;
		int month = 9;
		int day = dates.get(yr);
		
		for (int i = 1; i < week; i++) {
			day += 7;
			if (day > 30 && month % 2 == 1) {
				month++;
				day = day - 30;
			} else if (day > 31 && month % 2 == 0) {
				month++;
				day = day - 31;
			}
		}
		
		String monthStr = month + "";
		String dayStr = day + "";
		
		if (month < 10) {
			monthStr = 0 + monthStr;
		}
		if (day < 10) {
			dayStr = 0 + dayStr;
		}
		
		return "?date=201" + year + "-" + monthStr + "-" + dayStr;
	}
	
	// Gets data for many different spots in array, such as 3rd down conversion rate, pass yds, and rush yds
	public static double getTeamRankingsData(String team, StringBuffer data) {
		int localStart = data.indexOf(team);
		int start = 0;
		for (int i = 0; i < 3; i++) {
			start = data.indexOf("data-sort", localStart);
			localStart = start + 1;
		}
		start += 11;
		
		int end = data.indexOf(">", start) - 1;

		return Double.parseDouble(data.substring(start, end));
	}
	
	//I don't understand this code, I just copied and pasted it to use to make the CSV file
	public static String convertToCSV(double[][] data) {
        StringBuilder csvData = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.######");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

        for (double[] row : data) {
            for (double element : row) {
                String formattedValue = decimalFormat.format(element);
                csvData.append(formattedValue).append(",");
            }
            // Remove the trailing comma for each row
            csvData.deleteCharAt(csvData.length() - 1);
            csvData.append("\n");
        }
        return csvData.toString();
    }
	

    public static void writeArrayToCSV(double[][] data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            String csvData = convertToCSV(data);
            writer.write(csvData);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
