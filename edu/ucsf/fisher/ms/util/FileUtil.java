/*
 * Created on May 7, 2005
 *
 */
package edu.ucsf.fisher.ms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Comparator;
import java.net.*;

/**
 * @author eric
 *
 */
public class FileUtil {

	public static void diagnoseProblemWithFileName(String fileName) throws Exception
	{
		String pathParts[] = fileName.split("/");
		String path = "/";
		for (int i = 1; i < pathParts.length; i++)
		{
			path += (pathParts[i] + "/");
			if (!(new File(path).exists()))
				throw new Exception(path + " doesn't exist");
		}
	}
	public static String unixToWinPath(String unixPath)
	{
		String winPath = unixPath;
		/*
		if (!( (unixPath.startsWith("L:")) || 
				(unixPath.startsWith("M:")) || 
				(unixPath.startsWith("N:")) ))
		{
			if(unixPath.startsWith("/work/lab/")) 
			{
				winPath="L:\\"+unixPath.substring(10,unixPath.length());
				//winPath = winPath.replaceAll("/", "\\\\");
			}
			else if (unixPath.startsWith("/work/mascot/sequence")) // assume on Massie, mapped to N:
			{
				winPath="N:\\"+unixPath.substring(6,unixPath.length());
				//winPath = winPath.replaceAll("/", "\\\\");
			}
			else if (unixPath.startsWith("/work/"))
			{
				winPath="M:\\"+unixPath.substring(6,unixPath.length());
				//winPath = winPath.replaceAll("/", "\\\\");
			}
			else if (!unixPath.startsWith("N:"))// assume on Massie, mapped to N: // if (unixPath.startsWith("/massie???/"))
			{
				winPath="N:\\"+unixPath; // .substring(???,unixPath.length());
				//winPath = winPath.replaceAll("/", "\\\\");
			}
			winPath = winPath.replaceAll("/", "\\\\");
		}
		*/
		if (unixPath.startsWith("/work/")) // "/work/mascot/"
			winPath="N:\\"+unixPath.substring(6,unixPath.length()); 
		//else if(unixPath.startsWith("/work/lab/")) 
		//	winPath="L:\\"+unixPath.substring(10,unixPath.length());
		//else if (unixPath.startsWith("/work/"))
		//	winPath="M:\\"+unixPath.substring(6,unixPath.length());
		winPath = winPath.replaceAll("/", "\\\\");
		return winPath;
	}
	public static String winToUnixPath(String winPath)
	{
		String unixPath = winPath;
		if (winPath.startsWith("L:"))
		{
			unixPath = winPath.substring(2, winPath.length()).replaceAll("\\\\", "/");
			unixPath = "/work/lab" + unixPath;
		}
		else if (winPath.startsWith("M:"))
		{
			unixPath = winPath.substring(2, winPath.length()).replaceAll("\\\\", "/");
			unixPath = "/work" + unixPath;
		}
		else if (winPath.startsWith("N:\\mascot\\data")) // Massie  /work/mascot/data
		{
			unixPath = winPath.substring(14, winPath.length()).replaceAll("\\\\", "/");
			unixPath = "/massie.data" + unixPath;
		}
		else if (winPath.startsWith("N:\\mascot\\sequence")) // Massie  /work/mascot/sequence
		{
			unixPath = winPath.substring(18, winPath.length()).replaceAll("\\\\", "/");
			unixPath = "/massie.sequence" + unixPath;
		}
		else if (winPath.startsWith("N:\\lab\\automation")) 
		{	// Assuming that L:\automation\ and N:\lab\automation\ are in sync between Michigan and Massie
			// This is needed for any search_params set to N:\lab\automation\MASCOT DB Search Parameters Methods\*
			unixPath = winPath.substring(6, winPath.length()).replaceAll("\\\\", "/");
			unixPath = "/work/lab" + unixPath;
		}
		/*
		else if (winPath.startsWith("N:\\")) 
		{
			System.out.println("DEBUG: FileUtil.winToUnixPath mapped N:\\ to /work/ for "+winPath); 
			unixPath = winPath.substring(3, winPath.length()).replaceAll("\\\\", "/");
			unixPath = "/massie.data/" + unixPath;
		}
		*/
		else if (winPath.startsWith("N:"))
		{
			//System.out.println("DEBUG: FileUtil.winToUnixPath mapped N: to /work/ for "+winPath); 
			unixPath = winPath.substring(2, winPath.length()).replaceAll("\\\\", "/");
			unixPath = "/work" + unixPath;
		}
		
		if (!runningLinux())
		{
			// if we're not running Linux assume Solaris on Michigan (not on Massie) 
			if (winPath.startsWith("/work/mascot/sequence/"))
			{
				unixPath = winPath.substring(21, winPath.length());
				unixPath = "/massie.sequence" + unixPath;
			}
			else if (winPath.startsWith("/work/mascot/data/"))
			{
				unixPath = winPath.substring(17, winPath.length());
				unixPath = "/massie.data" + unixPath;
			}
		}
		return unixPath;
	}
	
	/**
	 * 
	 * @return True if OS is Windows, otherwise False
	 */
	public static boolean runningWindows(){
	    return (System.getProperty("os.name").toLowerCase().indexOf( "win" ) >= 0); 
	}
	
	/**
	 * 
	 * @return True if OS is Mac OSX, otherwise False
	 */
	public static boolean runningMac(){
	    return (System.getProperty("os.name").toLowerCase().indexOf( "mac" ) >= 0); 
	}
	
	/**
	 * True if OS is Linux, otherwise False
	 * @return
	 */
	public static boolean runningLinux(){
	    return (System.getProperty("os.name").toLowerCase().indexOf( "linux" ) >= 0); 
	}
	
	/**
	 * True if OS is Unix, otherwise False
	 * @return
	 */
	public static boolean runningUnix(){
	    return (System.getProperty("os.name").toLowerCase().indexOf( "nix" ) >= 0); 
	}
	
	/**
	 * True if OS is SunOS, otherwise False
	 * @return
	 */
	public static boolean runningSun(){ // SunOS
	    return (System.getProperty("os.name").toLowerCase().indexOf( "sunos" ) >= 0); 
	}
	
	/**
	 * True if OS is Unix or Linux, otherwise False
	 * @return
	 */
	public static boolean runningUnixOrLinux(){ //linux or unix
		String os = System.getProperty("os.name").toLowerCase();
	    return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);
	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public static String translateBetweenWinAndUnix(String filePath)
	{
		if (runningWindows())
			return (unixToWinPath_v2(filePath));
		else
			return (winToUnixPath_v2(filePath));
	}
	
	
	/**
	 * 
	 * @param rootPath
	 * @param origExt
	 * @param newExt
	 */
	public static void renameFiles(String rootPath, String origExt, String newExt){
		File[] fList = new File(rootPath).listFiles();
		for(int i=0;i<fList.length;i++) {
			if(fList[i].isFile() 
					&& fList[i].getName().endsWith(origExt) 
					&& !fList[i].getName().endsWith(newExt)) {
				System.out.println("Renaming "+fList[i].getAbsolutePath()+" \n to "+fList[i].getAbsolutePath().replaceAll(origExt, newExt));
				fList[i].renameTo(new File(fList[i].getAbsolutePath().replaceAll(origExt, newExt)));
			}
		}
	}
	
	/**
	 * 
	 * @param rootPath
	 * @param fileExt
	 */
	public static void printFileContent(String rootPath, String fileExt){
		File[] fList = new File(rootPath).listFiles();
		BufferedReader br = null;
		String inString = null;
		for(int i=0;i<fList.length;i++) {
			try {
				if(fList[i].isFile() 
						&& fList[i].getName().endsWith(fileExt)) {
					br = new BufferedReader(new FileReader(fList[i]));
					System.out.print(fList[i].getName()+":");
					while((inString=br.readLine())!=null) {
						System.out.println(inString);
					}
					br.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param fList
	 * @return
	 */
	public static File[] fileSort(File[] fList){
		File fTemp = null;
		if(fList!=null && fList.length>1) {
			for(int i=0;i<fList.length;i++) {
				for(int j=0;j<fList.length-1;j++) {
					if(fList[j].getName().compareTo(fList[j+1].getName()) > 0) {
						fTemp = fList[j];
						fList[j]=fList[j+1];
						fList[j+1]=fTemp;
					}
				}
			}
		}
		return fList;
	}

	
	/**
	 * Recursive version of getFileList() 
	 * 
	 * @param root
	 * @param ext
	 * @param recurse
	 * @return
	 */
	public static File[] getFileList(File root, String ext, boolean recurse) {
		//System.out.println("DEBUG: getFileList( root="+root.toString()+", ext="+ext+", recurse="+recurse+" ) "); // DEBUG
		ArrayList al = new ArrayList();
		File[] fileList = null;
		if (root.listFiles() != null)
		{
			fileList = root.listFiles();
			//System.out.println("DEBUG: fileList.length="+fileList.length+", fileList="+fileList.toString()); // DEBUG
			File[] subFileList;
			for(int i=0; i<fileList.length; i++) {
				if(fileList[i].isDirectory() && recurse) {
					subFileList = getFileList(fileList[i], ext);
					for(int j=0; j<subFileList.length; j++) {
						al.add(subFileList[j]);
					}
				} else {
					// MWD: added .toLowerCase() to match .raw or .RAW 
					if(fileList[i].isFile() && fileList[i].getName().toLowerCase(java.util.Locale.ENGLISH).endsWith(ext.toLowerCase(java.util.Locale.ENGLISH)))
						al.add(fileList[i]);
					//else if (fileList[i].isFile())
					//	System.out.println("DEBUG: skipping file: "+fileList[i].getName() ); // DEBUG 
					//else if (!recurse)
					//	System.out.println("DEBUG: .isFile() fails on "+fileList[i].getName()); // DEBUG
				}
			}
			fileList = new File[al.size()];
			for(int i=0; i<al.size(); i++) {
				//System.out.println(((File) al.get(i)).getAbsolutePath());
				fileList[i]=(File) al.get(i);
			}
			Arrays.sort(fileList, 
					new Comparator<File>() { 
														public int compare(File a, File b) 
														{ return a.getAbsolutePath().compareTo(b.getAbsolutePath());}
										   }
			);
		}
		//else
		//	System.out.println("DEBUG: listFiles() returns null "); // DEBUG
		return fileList;
	}

	public static File[] getFileList(File root) {
		return getFileList(root, "", true);
	}

	public static File[] getFileList(File root, boolean recurse) {
		return getFileList(root, "", recurse);
	}

	public static File[] getFileList(File root, String ext){
		return getFileList(root, ext, true);
	}
	
	/* non-recursive version of getFileList() */
	/*
	public static File[] getFileList(File root, String ext, boolean recurse){
		ArrayList al = new ArrayList();
		File[] fileList = root.listFiles();
		int i=0;
		for(i=0; i<fileList.length; i++) {
			al.add(fileList[i]);
		}
		i = 0;
		while(true) {
			if(((File) al.get(i)).isDirectory()) {
				fileList = ((File) al.get(i)).listFiles();
				al.remove(i);
				for(int j=0; j<fileList.length; j++) {
					al.add(fileList[j]);
				}
			} else if(++i >=al.size()) {
				break;
			}
		}
		fileList = new File[al.size()];
		for(i=0; i<al.size(); i++) {
			fileList[i]=(File) al.get(i);
		}
		return fileList;
	}
	*/
	
	public static String toSha1(File f) {
		int i;
		SHA1 s = new SHA1();
		try {
			//File f = new File("L:/msdata/Voyager Data/Salivary Proteome/Markus/2005/Metal Precipitation/LC MALDI/Copper/022505/B022505_75_0001.dat");
			InputStream is = new FileInputStream(f);
			System.out.println("File test");
			s.init();

			byte[] bytes = null;
			boolean moreBytes = false;
	        long fileLength  = f.length();
	        if(fileLength>0) moreBytes = true;

	        long offset2 = 0;
	        int numRead = 0;
	        
	        while(moreBytes) {
				//bytes = new byte[Integer.MAX_VALUE];
	        	bytes = new byte[10000];
				numRead=is.read(bytes,0,bytes.length);
				if(numRead >= 0) {
					for(i=0;i<bytes.length && i<numRead;i++) {
						s.update(bytes[i]);
						offset2++;
					}
					/* else {
							if(i>0) System.out.print(bytes[i-1]);
							System.out.print(" ");
							System.out.println(bytes[i]);
							i=bytes.length;
						}
						*/
				} else break;
	        }
	    
	        // Ensure all the bytes have been read in
	        if (offset2 < fileLength) {
	            throw new IOException("Could not completely read file "+f.getName()+". offset2: "+offset2+", fileLength: "+fileLength);
	        }
	        // Close the input stream and return bytes
	        is.close();
			
			s.finish();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return s.digout();

	}
	
	/**
	 * 
	 */
	public static void checkOS()
	{
		if ( runningWindows() )
		{
			System.out.println("Looks like you are running Windows.");
		}
		else if ( runningLinux() )
		{
			System.out.println("Looks like you are running Linux.");
		}
		else if ( runningUnix() )
		{
			System.out.println("Looks like you are running Unix.");
		}
		else if ( runningSun() )
		{
			System.out.println("Looks like you are running Sun Solaris.");
		}
		else if ( runningMac() )
		{
			System.out.println("Looks like you are running Mac OS.");
		}
		// Mac OS X, or Mac OS for earlier versions
		
		
		System.out.println("os.name=" + System.getProperty("os.name") );
		System.out.println("os.arch=" + System.getProperty("os.arch") );
		System.out.println("os.version=" + System.getProperty("os.version") );
		
		System.out.println("file.separator=" + System.getProperty("file.separator") );
		System.out.println("path.separator=" + System.getProperty("path.separator") );
		System.out.println("line.separator=" + System.getProperty("line.separator") );
		
		System.out.println("user.name=" + System.getProperty("user.name") );
		System.out.println("user.dir=" + System.getProperty("user.dir") );
		System.out.println("user.home=" + System.getProperty("user.home") );
		
	}
	
	
	/**
	 * 
	 */
	private static HashMap<String, String> win2UnixMap = new HashMap<String, String>();
	private static HashMap<String, String> unix2WinMap = new HashMap<String, String>();
	
	private static HashMap<String, String> mac2UnixMap = new HashMap<String, String>();
	private static HashMap<String, String> unix2MacMap = new HashMap<String, String>();
	
	private static HashMap<String, String> win2MacMap = new HashMap<String, String>();
	private static HashMap<String, String> mac2WinMap = new HashMap<String, String>();
	
	private static HashMap<String, String> win2WinMap = new HashMap<String, String>();
	
	private static HashMap<String, String> michigan2MassieMap = new HashMap<String, String>();
	private static HashMap<String, String> massie2MichiganMap = new HashMap<String, String>();
	static
	{
		win2UnixMap.put("L:", "/work/lab"); 
		win2UnixMap.put("M:", "/work"); 
		win2UnixMap.put("N:\\mascot\\data", "/massie.data"); 
		win2UnixMap.put("N:\\mascot\\sequence", "/massie.sequence"); 
		win2UnixMap.put("N:\\lab", "/work/lab");
		win2UnixMap.put("N:", "/work/lab"); 
		//win2UnixMap.put("\\\\massie.ckm.ucsf.edu\\lab\\Biospecimens\\CP", "/biospecimens/cp"); 
		//win2UnixMap.put("\\\\massie.ckm.ucsf.edu\\lab", "/work/lab"); 
		win2UnixMap.put("M:\\msdata", "/work/lab/msdata"); // maps from user workstations to server
		win2UnixMap.put("N:\\msdata", "/work/lab/msdata"); // maps from user workstations to server
		
		//
		//unix2WinMap.put("/work/lab", "L:"); 
		unix2WinMap.put("/work", "N:"); // changed from ("/work", "M:") to ("/work", "N:")
		unix2WinMap.put("/massie.data", "N:\\mascot\\data"); 
		unix2WinMap.put("/massie.sequence", "N:\\mascot\\sequence"); 
		unix2WinMap.put("/work/mascot/data", "N:\\mascot\\data"); 
		unix2WinMap.put("/work/mascot/sequence", "N:\\mascot\\sequence"); 
		//unix2WinMap.put("/work/lab", "N:"); 
		unix2WinMap.put("M:\\msdata", "M:\\lab\\msdata"); // maps from user workstations to dev workstations
		unix2WinMap.put("N:\\msdata", "N:\\lab\\msdata"); // maps from user workstations to dev workstations
		//
		
		//
		win2WinMap.put("M:\\msdata", "M:\\lab\\msdata"); // maps from user workstations to dev workstations
		win2WinMap.put("N:\\msdata", "N:\\lab\\msdata"); // maps from user workstations to dev workstations
		//
		
		// 
		mac2UnixMap.put("/Volumes/work/lab", "/work/lab"); 
		mac2UnixMap.put("/Volumes/work", "/work"); 
		mac2UnixMap.put("/massie.ckm.ucsf.edu/lab/Biospecimens/CP", "/biospecimens/cp"); 
		//
		unix2MacMap.put("/work/lab", "/Volumes/work/lab"); 
		unix2MacMap.put("/work", "/Volumes/work"); 
		unix2MacMap.put("\\\\massie.ckm.ucsf.edu\\lab\\Biospecimens\\CP", "/Volumes/work/lab/biospecimens/cp"); 
		unix2MacMap.put("\\\\massie.ckm.ucsf.edu\\lab", "/Volumes/work/lab"); 
		// 
		win2MacMap.put("L:", "/Volumes/work/lab"); 
		win2MacMap.put("M:", "/Volumes/work"); 
		//win2MacMap.put("N:", "/Volumes/work"); 
		mac2WinMap.put("/Volumes/work/lab", "L:"); 
		mac2WinMap.put("/Volumes/work", "M:"); 
		//mac2WinMap.put("/Volumes/work", "N:"); // 
		
		//
		michigan2MassieMap.put("/massie.data", "/work/mascot/data"); 
		michigan2MassieMap.put("/massie.sequence", "/work/mascot/sequence"); 
		michigan2MassieMap.put("/work/lab/mascot", "/work/mascot"); 
		//michigan2MassieMap.put("/work/lab/mascot2.1", "/work/mascot2.1"); 
		//
		massie2MichiganMap.put("/work/mascot/data", "/massie.data"); 
		massie2MichiganMap.put("/work/mascot/sequence", "/massie.sequence"); 
	}
	
	/**
	 * TODO:
	 * @param filePath
	 * @return True if filePath is Windows formatted, otherwise False
	 */
	public static boolean isWindowsPath(String filePath){
		
		// get keyset of win2UnixMap 
		for (String key : win2UnixMap.keySet())
		{
			if (filePath.startsWith(key))
			{
				// ...should we check for anything else? (such as separator)
				return true;
			}
		}
	    return false; 
	} 
	
	/**
	 * TODO:
	 * @param filePath
	 * @return True if filePath is Mac OSX formatted, otherwise False
	 */
	public static boolean isMacPath(String filePath){
		
		// get keyset of mac2WinMap 
		for (String key : mac2WinMap.keySet())
		{
			if (filePath.startsWith(key))
			{
				// ... should we check for anything else?
				return true;
			}
		}
	    return false; 
	}
	
	/**
	 * TODO:
	 * @param filePath
	 * @return True if filePath is Unix formatted, otherwise False
	 */
	public static boolean isUnixPath(String filePath){
		
		if ((!isWindowsPath(filePath)) && (!isMacPath(filePath)))
			return true;
	    return false; 
	}
	
	
	/**
	 * Translates filePath into a form appropriate for the currently running operating system. 
	 * This is designed to be a more generic replacement for translateBetweenWinAndUnix, 
	 * and could have been named translateBetweenMacAndWinAndUnix 
	 * 
	 * @param filePath
	 * @return
	 */
	public static String translateToOS(String filePath)
	{
		if (runningWindows())
		{
			if (isWindowsPath(filePath))
				return (winToWinPath(filePath));
			else if (isMacPath(filePath))
				return (macToWinPath(filePath));
			else
				return (unixToWinPath_v2(filePath));
		}
		else if (runningMac())
		{
			if (isMacPath(filePath))
				return filePath;
			else if (isWindowsPath(filePath))
				return (winToMacPath(filePath));
			else
				return (unixToMacPath(filePath));
		}
		else // running Unix / Linux / Sun OS
		{
			if (isWindowsPath(filePath))
				return (winToUnixPath_v2(filePath));
			else if (isMacPath(filePath))
				return (macToUnixPath(filePath));
			else 
				//return filePath;
				return (MichiganMassie(filePath)); // convert between Massie/Linux & Michigan/Sun OS
		}
	}
	
	
	/**
	 * 
	 * @param winPath
	 * @return
	 */
	public static String winToUnixPath_v2(String winPath)
	{
		String unixPath = winPath; //.replaceAll("\\\\", "/");
		boolean changedFileSeparator = false;
		for (String key : win2UnixMap.keySet())
		{
			if (winPath.startsWith(key))
			{
				unixPath = winPath.substring(key.length(), winPath.length()).replaceAll("\\\\", "/");
				String replacement = win2UnixMap.get(key);
				unixPath = replacement + unixPath;
				changedFileSeparator = true;
			}
		}
		if (!changedFileSeparator)
		{
			//System.out.println("ALERT: FileUtil.java:winToUnixPath_v2() : didn't find key to convert Win filepath to unix : "+winPath);
			unixPath = unixPath.replaceAll("\\\\", "/");
		}
		
		unixPath = MichiganMassie( unixPath );
		
		return unixPath;
	}
	
	public static String winToUnixPath_v2_debug(String winPath, String hostName)
	{
		String unixPath = winPath; //.replaceAll("\\\\", "/");
		boolean changedFileSeparator = false;
		for (String key : win2UnixMap.keySet())
		{
			if (winPath.startsWith(key))
			{
				unixPath = winPath.substring(key.length(), winPath.length()).replaceAll("\\\\", "/");
				String replacement = win2UnixMap.get(key);
				unixPath = replacement + unixPath;
				changedFileSeparator = true;
			}
		}
		if (!changedFileSeparator)
		{
			System.out.println("ALERT: FileUtil.java:winToUnixPath_v2_debug() : didn't find key to convert Win filepath to unix : "+winPath);
			unixPath = unixPath.replaceAll("\\\\", "/");
		}
		
		unixPath = MichiganMassie( unixPath, hostName );
		
		return unixPath;
	}
	
	public static String MichiganMassie(String unixPath)
	{
		return MichiganMassie( unixPath, null );
	}
	public static String MichiganMassie(String unixPath, String hostName)
	{
		if ((null == hostName) || (0 == hostName.trim().length()))
		{
			//hostName = NetUtil.getHostName(); // requires later version of Java than servers have 
			InetAddress addr;
			hostName = "";
			try 
			{
				addr = InetAddress.getLocalHost();
				hostName = addr.getHostName();
			} 
			catch (UnknownHostException e) 
			{
				e.printStackTrace();
			}
		}
		if (hostName.startsWith("massie"))
		{
			for (String key : michigan2MassieMap.keySet())
			{
				if (unixPath.startsWith(key))
				{
					unixPath = unixPath.substring(key.length(), unixPath.length());
					String replacement = michigan2MassieMap.get(key);
					unixPath = replacement + unixPath;
				}
			}
		}
		else if (hostName.startsWith("michigan"))
		{
			for (String key : massie2MichiganMap.keySet())
			{
				if (unixPath.startsWith(key))
				{
					unixPath = unixPath.substring(key.length(), unixPath.length());
					String replacement = massie2MichiganMap.get(key);
					unixPath = replacement + unixPath;
				}
			}
		}
		else
			System.out.println("Error: unrecognized hostname: " + hostName);
		
		return unixPath;
	}
	
	/**
	 * 
	 * @param unixPath
	 * @return
	 */
	public static String unixToWinPath_v2(String unixPath)
	{
		String winPath = unixPath; //unixPath.replaceAll("/", "\\\\");
		boolean changedFileSeparator = false;
		for (String key : unix2WinMap.keySet())
		{
			if (winPath.startsWith(key))
			{
				winPath = unixPath.substring(key.length(), unixPath.length()).replaceAll("/", "\\\\");
				String replacement = unix2WinMap.get(key);
				winPath = replacement + winPath;
				changedFileSeparator = true;
			}
		}
		if (!changedFileSeparator)
		{
			//System.out.println("ALERT: FileUtil.java:unixToWinPath_v2() : didn't find key to convert unix filepath to Win : "+unixPath);
			winPath = winPath.replaceAll("/", "\\\\");;
		}
		return winPath;
	}
	
	
	/**
	 * 
	 * @param macPath
	 * @return
	 */
	public static String macToWinPath(String macPath)
	{
		String winPath = macPath; //macPath.replaceAll("/", "\\\\");
		boolean changedFileSeparator = false;
		for (String key : mac2WinMap.keySet())
		{
			if (winPath.startsWith(key))
			{
				winPath = macPath.substring(key.length(), macPath.length()).replaceAll("/", "\\\\");
				String replacement = mac2WinMap.get(key);
				winPath = replacement + winPath;
				changedFileSeparator = true;
			}
		}
		if (!changedFileSeparator)
		{
			//System.out.println("ALERT: FileUtil.macToWinPath_v2() : didn't find key to convert mac filepath to Win : "+macPath);
			winPath = winPath.replaceAll("/", "\\\\");;
		}
		return winPath;
	}
	
	/**
	 * 
	 * @param winPath
	 * @return
	 */
	public static String winToMacPath(String winPath)
	{
		String macPath = winPath; //.replaceAll("\\\\", "/");
		boolean changedFileSeparator = false;
		for (String key : win2MacMap.keySet())
		{
			if (winPath.startsWith(key))
			{
				macPath = winPath.substring(key.length(), winPath.length()).replaceAll("\\\\", "/");
				String replacement = win2MacMap.get(key);
				macPath = replacement + macPath;
				changedFileSeparator = true;
			}
		}
		if (!changedFileSeparator)
		{
			//System.out.println("ALERT: FileUtil.java:winToMacPath() : didn't find key to convert Win filepath to mac : "+winPath);
			macPath = macPath.replaceAll("\\\\", "/");
		}
		return macPath;
	}
	
	
	/**
	 * 
	 * @param inputPath
	 * @return
	 */
	public static String winToWinPath(String inputPath)
	{
		String outputPath = inputPath; //inputPath.replaceAll("/", "\\\\");
		boolean changedFileSeparator = false;
		for (String key : win2WinMap.keySet())
		{
			if (outputPath.startsWith(key))
			{
				outputPath = inputPath.substring(key.length(), inputPath.length()).replaceAll("/", "\\\\");
				String replacement = win2WinMap.get(key);
				outputPath = replacement + outputPath;
				changedFileSeparator = true;
			}
		}
		if (!changedFileSeparator)
		{
			//System.out.println("ALERT: FileUtil.winToWinPath() : didn't find key to convert input filepath to Win : "+inputPath);
			outputPath = outputPath.replaceAll("/", "\\\\");;
		}
		return outputPath;
	}
	
	
	/**
	 * 
	 * @param unixPath
	 * @return
	 */
	public static String unixToMacPath(String unixPath)
	{
		String macPath = unixPath; 
		for (String key : unix2MacMap.keySet())
		{
			if (unixPath.startsWith(key))
			{
				macPath = unixPath.substring(key.length(), unixPath.length()).replaceAll("\\\\", "/");
				String replacement = unix2MacMap.get(key);
				macPath = replacement + macPath;
			}
		}
		return macPath;
	}
	
	/**
	 * 
	 * @param macPath
	 * @return
	 */
	public static String macToUnixPath(String macPath)
	{
		String unixPath = macPath; 
		for (String key : mac2UnixMap.keySet())
		{
			if (macPath.startsWith(key))
			{
				unixPath = macPath.substring(key.length(), macPath.length()).replaceAll("\\\\", "/");
				String replacement = mac2UnixMap.get(key);
				unixPath = replacement + unixPath;
			}
		}
		return unixPath;
	}
	
	/**
	 * 
	 */
	public static void testPathConversion()
	{
		String[] winFilePaths = {
							"M:\\lab\\niles\\",
							"L:\\msdata\\4800 TOFTOF Data\\Salivary Proteome\\Markus Hardt\\Zoom IEF 2DLC\\MP PS Zoom SF ief03 SCX1-6 desalt 120506\\",
							"N:\\msdata\\4800 TOFTOF Data\\Salivary Proteome\\Markus Hardt\\Zoom IEF 2DLC\\MP PS Zoom SF ief03 SCX1-6 desalt 120506\\",
							"N:\\mascot\\data\\20090526\\",
							"N:\\mascot\\sequence\\IPI_human\\current\\"
								};
		String[] michiganFilePaths = {
							"/work/lab/automation/jobMonitor/",
							"/massie.data/20090526/",
							"/massie.sequence/IPI_human/current/"
								};
		String[] massieFilePaths = {
							"/work/lab/automation/jobMonitor/",
							"/work/mascot/data/20090526/",
							"/work/mascot/sequence/IPI_human/current/"
								};
		String[] macFilePaths = {
							"/Volumes/work/lab/PROJECTS/ENDOMETRIOSIS/Results from Rich Niles/"
								};
		
		for (String filePath : winFilePaths)
		{
			System.out.println("Original Win File Path = "+filePath);
			
			String michFilePath = winToUnixPath_v2_debug(filePath, "michigan");
			System.out.println("Unix/Sun OS Michigan File Path = "+michFilePath);
			
			String massFilePath = winToUnixPath_v2_debug(filePath, "massie");
			System.out.println("Unix/Linux Massie File Path = "+massFilePath);
			
			String macFilePath = winToMacPath(filePath);
			System.out.println("Mac OSX File Path = "+macFilePath);
			
			System.out.println();
		}
		
		for (String filePath : michiganFilePaths)
		{
			System.out.println("Original Unix/Sun OS Michigan File Path = "+filePath);
			
			String winFilePath = unixToWinPath_v2(filePath);
			System.out.println("Windows File Path = "+winFilePath);
			
			String macFilePath = unixToMacPath(filePath);
			System.out.println("Mac OSX File Path = "+macFilePath);
			
			String massFilePath = MichiganMassie( filePath, "massie" );
			System.out.println("Unix/Linux Massie File Path = "+massFilePath);
			
			System.out.println();
		}
		
		for (String filePath : massieFilePaths)
		{
			System.out.println("Original Unix/Linux Massie File Path = "+filePath);
			
			String winFilePath = unixToWinPath_v2(filePath);
			System.out.println("Windows File Path = "+winFilePath);
			
			String macFilePath = unixToMacPath(filePath);
			System.out.println("Mac OSX File Path = "+macFilePath);
			
			String michFilePath = MichiganMassie( filePath, "michigan" );
			System.out.println("Unix/Sun OS Michigan File Path = "+michFilePath);
			
			System.out.println();
		}
		for (String filePath : macFilePaths)
		{
			System.out.println("Original Mac OSX File Path = "+filePath);
			
			String winFilePath = macToWinPath(filePath);
			System.out.println("Windows File Path = "+winFilePath);
			
			String unixFilePath = macToUnixPath(filePath);
			System.out.println("Unix File Path = "+unixFilePath);
			
			System.out.println();
		}
	}
	
	public static void main	(String args[]) 
	{
		File[] a = FileUtil.getFileList(new File("/Volumes/work/lab/msdata/PE Sciex Data/Projects/PenelopeDrake_Fisher/Data/Lectin 3 way Data/Site S/SNA/SNA1_repl1/"));
		for (int i = 0; i < a.length; i++)
			System.out.println(a[i].getName());
	}
	
	/**
	 * This can be called using: /work/lab/automation/jobMonitor/FileUtil.sh
	 * or
	 * java -classpath /work/lab/automation/jarfiles/sprotUnix.jar edu.ucsf.library.util.FileUtil renameFiles [rootPath] [original_extenstion] [new_extension]
	 * or
	 * java -classpath /work/lab/automation/jarfiles/sprotUnix.jar edu.ucsf.library.util.FileUtil getFileList [rootPath] 
	 * 
	 * @param args
	 */
	public static void mainX	(String args[]) 
	{
		if (args.length == 0)
			System.out.println("This tests file name conversions. \nusage: FileUtil <fileName>");
		else
			for (int i = 0; i < args.length; i++)
				//System.out.println(translateBetweenWinAndUnix(args[i]));
				System.out.println(translateToOS(args[i]));

	}
}
