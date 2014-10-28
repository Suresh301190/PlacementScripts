package placement.drive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class GetFilesFromShareLink {

	private static final String requestURL =  "https://docs.google.com/a/iiitd.ac.in/uc?export=download&id=";
	private final String dir;
	private static final boolean DEBUG = true;
	private static int rc = 0;
	private static final Pattern sID1 = Pattern.compile(" *(MT)?[0-9]* *");
	private static final Pattern sID2 = Pattern.compile("[a-zA-Z]* *- *(MT)?[0-9]*");

	public GetFilesFromShareLink(String dir){
		if(dir == null || dir.length() == 0){
			System.out.println("Please provide a Valid directory");
			this.dir = null;
			System.exit(-1);
		}
		else{
			this.dir = dir + ((dir.charAt(dir.length() - 1) == '/' )?"":"/");
		}
	}

	public void SaveFile(String ID, String fileName, boolean isBtech) {
		try{
			HttpURLConnection con =(HttpURLConnection) new URL(requestURL + ID).openConnection();
			con.setRequestMethod("GET");
			if(isBtech)
				Files.copy(con.getInputStream(), new File(dir + "B.Tech/" + fileName + ".pdf").toPath());
			else
				Files.copy(con.getInputStream(), new File(dir + "M.Tech/" + fileName + ".pdf").toPath());

		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		if(args.length == 2){
			System.out.println("args[0] = path/to/output/dir, args[1] = path/to/student/csv ");
			return;
		}

		ArrayList<Student> toSave = parseInput(new File(args[1]));
	}
	
	public void test1(String line){
		String[] parts = "naresh11067@iiitd.ac.in,https://docs.google.com/document/d/1plpiOzRVH-ybsA-0_oFCMfv2QpzI7HYuhnqX9AZrfTE/edit?usp=sharing,B.Tech,Btech- 2011067".split(",");
		System.out.println(new Student(
				parseID(parts[1])
				, parseSID(parts[3])
				, parts[2].equals("B.Tech")
				, parseName(parts[0])).toString());
	}

	private static ArrayList<Student> parseInput(File file) {
		// TODO Auto-generated method stub

		ArrayList<Student> toSave = new ArrayList<Student>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while(null != (line = br.readLine())){
				String[] parts = line.split(",");
				toSave.add(new Student(
						parseID(parts[1])
						, parseSID(parts[3])
						, parts[2].equals("B.Tech")
						, parseName(parts[0])));
			}
			br.close();
		} catch (IOException e) {
			if(DEBUG)
				e.printStackTrace();
		}

		return toSave;
	}

	private static String parseName(String s) {
		// TODO Auto-generated method stub
		int i = 0;
		while(i < s.length()){
			if(s.charAt(i) <= '9')
				break;
			i++;
		}
		return s.substring(0, i);
	}

	private static String parseSID(String s) {
		// TODO Auto-generated method stub

		String sID = "shark" + rc++;
		try{
			if(sID1.matcher(s).matches()){
				sID = s.trim();
			}
			else if(sID2.matcher(s).matches()){
				sID = s.substring(s.lastIndexOf('-') + 1).trim();
			}
		} catch(Exception e){
			if(DEBUG)
				e.printStackTrace();
		}

		return sID;
	}

	private static String parseID(String s) {
		// TODO Auto-generated method stub

		String ID = "shark" + rc++;

		try{
			if(s.contains("/view?usp=sharing") 
					|| s.contains("/edit?usp=sharing") 
					|| s.contains("/edit?usp=docslist_api")){
				ID = s.substring(s.lastIndexOf('/', s.lastIndexOf('/') - 1) + 1, s.lastIndexOf('/'));
			}
		} catch(Exception e){

		}

		return ID;
	}

	static class Student{
		final String fileName, ID, sID, name;
		boolean isBtech;

		public Student(String ID, String sID, boolean isBtech, String name){
			this.sID = sID;
			this.isBtech = isBtech;
			this.ID = ID;
			this.name = name;
			this.fileName = sID + (isBtech?"_Btech_":"_Mtech_") + name;
		}

		@Override
		public String toString(){
			return "{" + name + (isBtech?" B.Tech":" M.Tech") + ", ID-> " + ID + ", sID-> " + sID + "}";
		}
	}
}