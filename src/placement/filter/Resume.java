package placement.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class Resume {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Resume().work(args);
	}

	private void work(String[] args) {
		// TODO Auto-generated method stub
		if(args.length != 3) {
			System.out.println("Please specify args[0]=input directory, args[1]=filter.txt, args[2]=output dir");
			return;
		}
		try{
			File dir = new File(args[0]);
			File outDir = new File(args[2]);
			if(!dir.isDirectory()){
				System.out.println("Not a Valid Directory");
				return;
			}
			if(outDir.mkdir()){
				System.out.println("Output Directory created");
			} else {
				String[]entries = outDir.list();
				for(String s: entries){
				    File currentFile = new File(outDir.getPath(),s);
				    currentFile.delete();
				}
			}
			BufferedReader br = new BufferedReader(new FileReader(args[1]));
			LinkedList<String> filter = new LinkedList<String>();
			HashMap<String, File> map = new HashMap<String, File>();

		    for(File file: dir.listFiles()){
		    	if(file.isFile()) {
		    		//System.out.println(file.getName());
		    		try{
		    			map.put(file.getName().substring(0, file.getName().indexOf('_')), file);
		    		} catch(Exception e){}
		    	}
		    }
		    
			String line;
			while(null != (line = br.readLine())){
				filter.add(line);
			}
			
			for(Iterator<String> it = filter.iterator(); it.hasNext();) {
				String key = it.next();
				if(map.containsKey(key)){
					File file = map.get(key);
					Files.copy(file.toPath(), new File(outDir + "/" + file.getName()).toPath());
				} 
				else {
					System.out.println("Does not exists Roll No: " + key);
				}
			}
			br.close();
			
			System.out.println("\n\n");
			System.out.println("**************************");
			System.out.println("**                      **");
			System.out.println("**All Files copies (^_^)**");
			System.out.println("**                      **");
			System.out.println("**************************");		
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
