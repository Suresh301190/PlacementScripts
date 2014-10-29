package placement.drive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class GetFilesFromShareLink {

	private static final String requestURL =  "https://docs.google.com/a/iiitd.ac.in/uc?export=download&id=";
	private final String dir;
	private static final boolean DEBUG = true;
	private static int rc = 0;
	private static final Pattern sID1 = Pattern.compile(" *(MT)?[0-9]* *");
	private static final Pattern sID2 = Pattern.compile(" *[a-zA-Z.]* *- *(MT)?[0-9]* *");
	private TreeMap<String, Student> toSave;

	public static void main(String[] args){
		if(args.length != 2){
			System.out.println(Arrays.toString(args));
			System.out.println("args[0] = path/to/output/dir, args[1] = path/to/student/csv ");
			return;
		}

		GetFilesFromShareLink gf = new GetFilesFromShareLink(args[0]);
		gf.parseInput(new File(args[1]));
		System.out.println(gf);
		gf.processMap();

	}

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

	public boolean processMap(){
		try{
			File file = new File(dir);
			if(file.exists())
				file.mkdir();
			new File(dir + "/B.Tech").mkdir();
			new File(dir + "/M.Tech").mkdir();
			ExecutorService executor = Executors.newScheduledThreadPool(24);
			for(Map.Entry<String, Student> en : toSave.entrySet()){
				executor.execute(new Download(
						en.getValue()
						, dir));
			}
			
			executor.shutdown();
			if(executor.awaitTermination(7, TimeUnit.MINUTES)){
				System.out.println("Failed Downloads Please Mail Them\n" + Download.toStrings());
			}
			
		} catch(Exception e){
			e.printStackTrace();
		}

		return true;
	}

	static class Download implements Runnable{

		static private final HashSet<String> failed = new HashSet<String>();
		static private final String token = "Bearer ya29.rQDsW5mbwKulSLVYMwWwuo6CG2FszljTb_mvygVI3fvxxLTtbZB6ffVEDJmH5LJCEnfKMzW89aw4cw";
		final Student student;
		final String dir;

		public Download(final Student student, final String dir){
			this.student = student;
			this.dir = dir;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet get = new HttpGet(requestURL + student.ID);
				get.addHeader("Authorization", token);
				HttpResponse response = httpClient.execute(get);
				
				if(response.getStatusLine().getStatusCode() != 200){
					failed.add(student.email);
					return;
				}
				
				//*
				HttpEntity con = response.getEntity();
				
				//HttpURLConnection con =(HttpURLConnection) new URL(requestURL + student.ID).openConnection();
				//con.addRequestProperty("Authorization", "Bearer " + new String(Base64.encodeBase64("ya29.rADsc0KaaTtrln5bMOpX8H7frslpqUvkbz_Y6xIbAU8IdjD7iUmvX_4MNUNR-Da9mZHE93wzJR4bNw".getBytes())));
				//con.setRequestMethod("GET");
				if(student.isBtech){
					File file = new File(dir + "B.Tech/" + student.fileName + ".pdf");
					if(file.exists())
						file.delete();
					Files.copy(con.getContent(), file.toPath());
				}
				else{
					File file = new File(dir + "M.Tech/" + student.fileName + ".pdf");
					if(file.exists())
						file.delete();
					Files.copy(con.getContent(), file.toPath());
				}
				
				// */

			} catch(Exception e){
				System.out.println("Failed to download ID-> " + student.sID);
				if(DEBUG)
					e.printStackTrace();
			}
		}	
		
		public void reset(){
			failed.clear();
		}
		
		public static String toStrings(){
			StringBuffer sb = new StringBuffer();
			for(String s : failed){
				sb.append(s + "\n");
			}
			return sb.toString();
		}
	}

	public void test1(String line){
		String[] parts = "naresh11067@iiitd.ac.in,https://docs.google.com/document/d/1plpiOzRVH-ybsA-0_oFCMfv2QpzI7HYuhnqX9AZrfTE/edit?usp=sharing,B.Tech,Btech- 2011067".split(",");
		System.out.println(new Student(
				parseID(parts[1])
				, parseSID(parts[3])
				, parts[2].equals("B.Tech")
				, parseName(parts[0])
				, parts[0]).toString());
	}

	private void parseInput(File file) {
		// TODO Auto-generated method stub

		toSave = new TreeMap<String, Student>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			String[] parts;
			while(null != (line = br.readLine())){
				parts = line.split(",");
				try{
					toSave.put(
							parseSID(parts[3]), 
							new Student(
									parseID(parts[1])
									, parseSID(parts[3])
									, parts[2].equals("B.Tech")
									, parseName(parts[0])
									, parts[0]));
					
				} catch(Exception e){
					System.out.println(line);
					e.printStackTrace();
				}
			}
			br.close();
		} catch (IOException e) {
			if(DEBUG)
				e.printStackTrace();
		}
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
					|| s.contains("/edit?usp=docslist_api")
					||s.endsWith("view")){
				ID = s.substring(s.lastIndexOf('/', s.lastIndexOf('/') - 1) + 1, s.lastIndexOf('/'));
			}
			else if (s.contains("&authuser=")){
				ID = s.substring(s.lastIndexOf('=', s.lastIndexOf('=') - 1) + 1, s.lastIndexOf('&'));
			}
		} catch(Exception e){

		}

		return ID;
	}

	@Override
	public String toString(){
		return (null == toSave)?"":toSave.toString();
	}

	static class Student{
		final String fileName, ID, sID, name, email;
		boolean isBtech;

		public Student(String ID, String sID, boolean isBtech, String name, String email){
			this.sID = sID;
			this.isBtech = isBtech;
			this.ID = ID;
			this.name = name;
			this.email = email;
			this.fileName = sID + (isBtech?"_Btech_":"_Mtech_") + name;
		}

		@Override
		public String toString(){
			return "{" + name + (isBtech?" B.Tech":" M.Tech") + ", ID-> " + ID + ", sID-> " + sID + "}";
		}
	}
}