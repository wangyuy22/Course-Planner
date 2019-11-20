import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReqSearch {
	private String baseUrl;
	private Document currentDoc;

	/**
	 * Constructor that initializes the base URL and loads the document produced from that URL
	 */
	public ReqSearch() {
		this.baseUrl = "https://catalog.upenn.edu/undergraduate/engineering-applied-science/majors/";
		try {
			this.currentDoc = getDOMFromURL(baseUrl);
			//            System.out.println(this.currentDoc);

		} catch (IOException e) {
			System.out.println("Could not get the Penn Majors home page!");
		}
	}

	// getter method to get the main document
	public Document getCurrentDoc() {
		return currentDoc;
	}

	public Document getDOMFromURL(String u) throws IOException {
		URL url = new URL(u);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder sb = new StringBuilder();
		String curr = in.readLine();
		while(curr != null) {
			sb.append(curr);
			curr = in.readLine();
		}
		return Jsoup.parse(sb.toString());
	}

	public TreeMap<String, ArrayList<String>> getCourses(String major, String degree) {
		major = major.replace("and ", "");
		major = major.replace(" Program", "");
		major = major.replace(" ", "-");
		String majorDegree = major.toLowerCase() + "-" + degree.toLowerCase();

		Elements banner = currentDoc.getElementsByClass("sitemap");
		List<String> links = banner.select("a").eachAttr("href");

		//Loops through to find index of major
		int counter = 0;
		boolean found = false;
		for (String m : links) {

			if (m.contains(majorDegree)) {
				found = true;
				break;
			}
			counter++;
		}

		//Checks if exist
		if (!found) {
			throw new IllegalArgumentException("Not a valid major");
		}

		String link = links.get(counter);
		String url = "https://catalog.upenn.edu" + link;
		Document newDoc = null;
		try {
			newDoc = getDOMFromURL(url);
		} catch (IOException e) {
			System.out.println("Could not get to page!");
		}

		Element newBanner = newDoc.getElementsByClass("sc_courselist").first();
		List<Element> groups = newBanner.select("tr");
		//System.out.println(groups);
		//System.out.println(groups.size());

		TreeMap<String, ArrayList<String>> courses = new TreeMap<String, ArrayList<String>>();

		ArrayList<String> prereqs = new ArrayList<String>();
		ArrayList<String> coreqs = new ArrayList<String>();
		ArrayList<String> reqs = new ArrayList<String>();

		//Looping through all elements
		for (Element e : groups) {
			String text = e.text();
		//System.out.println("Text: " + text + " length : " + text.length() + " classname: " + e.className());
			boolean b = false;
			if (text.length() != 0  && !e.className().contains("orclass")
					&& !e.className().contains("area")) {
				b = Character.isDigit(text.charAt(text.length() - 1));
			}
			
			if (majorDegree.equals("bioengineering-bse"))
			if (text.equals("PHYS 140 Principles of Physics I (without laboratory") ||
					text.equals("PHYS 140 Principles of Physics I (without laboratory") ||
					text.equals("EAS 091 Chemistry Advanced Placement/International Baccalaureate "
							+ "Credit (Engineering Students Only)") ||
					text.equals("CHEM 102 General Chemistry II length") ||
					text.equals("CHEM 054 General Chemistry Laboratory II") ||
					text.equals("BIOL 121 Introduction to Biology - The Molecular Biology of Life") ||
					text.equals("BIOL 123 Introductory Molecular Biology Laboratory") ||
					text.equals("BIOL 204 Biochemistry")) {
						b = true;
					}
			//Finding elements that are required and are defined
			if (b) {
				Elements temp = e.getElementsByAttribute("href");
				if (temp.select("a").eachAttr("href").size() != 0) {
					String urlLink = temp.select("a").eachAttr("href").get(0);
				//	System.out.println(urlLink);
					if (!urlLink.equals("")) {
						//Getting course name
						String title = urlLink.substring(urlLink.indexOf("=") + 1);
						title = title.replace("%20", " ");


						//System.out.println(urlLink);
						prereqs = this.getPrereqs(urlLink);
						coreqs = this.getCoreqs(urlLink);
						//System.out.println(coreqs);
						//System.out.println(prereqs);
						reqs = prereqs;
						reqs.add(0, "Prereqs");
						reqs.add("Coreqs");
						reqs.addAll(coreqs);
						//System.out.print(title);
						courses.put(title, reqs);
					}     
				}   			
			}
		}
		//System.out.println(courses);
		return courses;

	}

	public ArrayList<String> getPrereqs(String link) {
		//Getting url
		String url = "https://catalog.upenn.edu" + link;

		//Navigating to new doc
		Document newDoc = null;
		try {
			newDoc = getDOMFromURL(url);
		} catch (IOException e) {
			System.out.println("Could not get to page!");
		}
		Elements banner = newDoc.getElementsByClass("courseblock");
		String msg = banner.text();

		//If no prereqs stop
		ArrayList<String> prereqs = new ArrayList<String>();
		if (!msg.contains("Prerequisite")) {
			return prereqs;
		}

		//Change to all uppercase
		msg = msg.toUpperCase();
		
		//Checks if it has coreqs
		boolean hasCoreq = msg.contains("Corequisite");
		if (!hasCoreq) {
			if (msg.contains("PREREQUISITES:")) {
				int indexA = msg.indexOf("PREREQUISITES: ") + 15;
				int indexB = msg.indexOf(" ACTIVITY");
				String sub = msg.substring(indexA, indexB);

				//Checks for unnecessary text
				if (sub.contains(";")) {
					sub = sub.substring(0, sub.indexOf(";"));
				}

				//Replacing all the comma and spaces
				sub = sub.replace(", ", "");

				//Checking length of course acronym
				int length = sub.indexOf(" ");

				//Getting acronym
				String acronym = sub.substring(0, length);
				
				sub = sub.replace(" OR MATH 115", "");
				sub = sub.replace(" OR MATH 116", "");
				sub = sub.replace(" OR PHYS 170", "");
				sub = sub.replace(" OR PHYS 171", "");
				sub = sub.replace(" AND ", "");

				
				//Adding to list
				for (int x = 0; x < sub.length(); x += length + 4) {
					//Checks if it actually has prereqs
					if (sub.substring(x).indexOf(" ") > 4) {
						return prereqs;
					}
					
					if (sub.substring(x).indexOf(" ") < 3) {
						return prereqs;
					}

					if (Character.isDigit(sub.charAt(x))) {
						sub = sub.substring(0, x) + acronym + " " + sub.substring(x, sub.length());
					}
					prereqs.add(sub.substring(x, x + length + 4));
				}

			} else {
				int indexA = msg.indexOf("PREREQUISITE: ") + 14;
				int indexB = msg.indexOf(" ACTIVITY");

				String sub = msg.substring(indexA, indexB);
				//Checks if it actually has prereqs
				if (sub.indexOf(" ") > 4) {
					return prereqs;
				}

				//Checking length of course acronym
				int length = sub.indexOf(" ");

				//Adding
				prereqs.add(sub.substring(0, length + 4));
			}
		} else {
			if (msg.contains("PREREQUISITES:")) {
				int indexA = msg.indexOf("PREREQUISITES: ") + 15;
				int indexB = msg.indexOf(" COREQUISITE");
				String sub = msg.substring(indexA, indexB);


				//Checks for unnecessary text
				if (sub.contains(";")) {
					sub = sub.substring(0, sub.indexOf(";"));
				}

				//Replacing all the comma and spaces
				sub = sub.replace(", ", "");
				
				//Checking length of course acronym
				int length = sub.indexOf(" ");
				
				//Getting acronym
				String acronym = sub.substring(0, length);
				
				int stop = sub.length();
				if (sub.contains(";")) {
					stop = sub.indexOf(";");
				}
				
				sub = sub.replace(" OR MATH 115", "");
				sub = sub.replace(" OR MATH 116", "");
				sub = sub.replace(" OR PHYS 170", "");
				sub = sub.replace(" OR PHYS 171", "");
				sub = sub.replace(" AND ", "");

				//Adding to list
				for (int x = 0; x < stop; x += length + 4) {
					
					//Checks if it actually has prereqs
					if (sub.substring(x).indexOf(" ") > 4) {
						return prereqs;
					}
					
					if (sub.substring(x).indexOf(" ") < 3) {
						return prereqs;
					}
					
					if (Character.isDigit(sub.charAt(x))) {
						sub = acronym + " " + sub;
					}
					prereqs.add(sub.substring(x, x + length + 4));
				}
			} else {
				int indexA = msg.indexOf("PREREQUISITE: ") + 14;
				int indexB = msg.indexOf(" COREQUISITE");

				String sub = msg.substring(indexA, indexB);

				//Checks if it actually has prereqs
				if (sub.indexOf(" ") > 4) {
					return prereqs;
				}
				
				//Checking length of course acronym
				int length = sub.indexOf(" ");

				//Adding
				prereqs.add(sub.substring(0, length + 4));
			}
		}
		return prereqs;
	}

	public ArrayList<String> getCoreqs(String link) {
		//Getting url
		String url = "https://catalog.upenn.edu" + link;

		//Navigating to new doc
		Document newDoc = null;
		try {
			newDoc = getDOMFromURL(url);
		} catch (IOException e) {
			System.out.println("Could not get to page!");
		}
		Elements banner = newDoc.getElementsByClass("courseblock");
		String msg = banner.text();

		//If no coreqs stop
		ArrayList<String> coreqs = new ArrayList<String>();
		if (!msg.contains("Corequisite")) {
			return coreqs;
		}
		
		//Change to all uppercase
		msg = msg.toUpperCase();
		
		if (msg.contains("COREQUISITES: ")) {
			int indexA = msg.indexOf("COREQUISITES: ") + 14;
			int indexB = msg.indexOf(" ACTIVITY");
			String sub = msg.substring(indexA, indexB);

			//Checks for unnecessary text
			if (sub.contains(";")) {
				sub = sub.substring(0, sub.indexOf(";"));
			}

			//Replacing all the comma and spaces
			sub = sub.replace(", ", "");

			//Checking length of course acronym
			int length = sub.indexOf(" ");

			//Getting acronym
			String acronym = sub.substring(0, length);

			//Adding to list
			for (int x = 0; x < sub.length(); x += length + 4) {
				//Checks if it actually has coreqs
				if (sub.substring(x).indexOf(" ") > 4) {
					return coreqs;
				}
				
				if (sub.substring(x).indexOf(" ") < 3) {
					return coreqs;
				}

				if (Character.isDigit(sub.charAt(x))) {
					sub = sub.substring(0, x) + acronym + " " + sub.substring(x, sub.length());
				}
				coreqs.add(sub.substring(x, x + length + 4));
			}
			
		} else {
			int indexA = msg.indexOf("COREQUISITE: ") + 13;
			int indexB = msg.indexOf(" ACTIVITY");
			String sub = msg.substring(indexA, indexB);
			
			//Labs do not count
			if (sub.contains("LAB")) {
				return coreqs;
			}
			//Checking length of course acronym
			int length = sub.indexOf(" ");

			//Adding
			coreqs.add(sub.substring(0, length + 4));
		}
		return coreqs;
	}


	public static void main(String args[]) {
		ReqSearch Penn = new ReqSearch();
		//Penn.getCourses("Bioengineering", "BSE");
		//Penn.getCourses("Biomedical Science", "BAS");
		//Penn.getCourses("Chemical and Biomolecular Engineering", "BSE");
		//Penn.getCourses("Computational Biology", "BAS");
		//Penn.getCourses("Computer and Cognitive Science", "BAS");
		//Penn.getCourses("Computer Engineering", "BSE");
		//Penn.getCourses("Computer Science", "BSE");
		//Penn.getCourses("Computer Science", "BAS");
		//Penn.getCourses("Digital Media Design", "BSE");
		//Penn.getCourses("Electrical Engineering", "BSE");
		//Penn.getCourses("Individualized Program", "BAS");
		//Penn.getCourses("Materials Science and Engineering", "BSE");
		//Penn.getCourses("Mechanical Engineering and Applied Mechanics", "BSE");
		//Penn.getCourses("Networked and Social Systems Engineering", "BSE");
		//Penn.getCourses("Systems Science and Engineering", "BSE");
	}
}
