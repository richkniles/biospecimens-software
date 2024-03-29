package edu.ucsf.fisher.ms.r;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.TreeSet;

/*
**
**  This program reads as input, an alignment spreadsheet generated by AB Sciex Excel Alignment Template
**
**  To use this program export the alignment spreadsheet as tab-delimited.  Edit the main to open
**  the resulting text file.  The output file will be ready to import into R.
**
**
*/

public class FormatAlignmentForR 
{
	static class Label
	{
		String fileKey;
		String label[];
		String numerator;
		int replicate;

		public Label(String fileKey, String label[], String numerator, int replicate) 
		{
			super();
			this.fileKey = fileKey;
			this.label = label;
			this.numerator = numerator;
			this.replicate = replicate;
		}
		public String toString()
		{
			String ret = "";
			for (String l : label)
			{
				if (!ret.isEmpty())
					ret += " ";
				ret += l;
			}
			return ret + " " + replicate;
		}
	}
	
	static Label getNumLabel(HashMap<String,Label> labels, String file, String ratio) throws Exception
	{
		String fileKey = null;
		for (String key : labels.keySet())
		{
			fileKey = key.split("\\:")[0];
			if (file.contains(fileKey))
				break;
		}
		if (fileKey == null)
			throw new Exception("Can't recognise " + file);
		
		String numDenom[] = ratio.split("\\:");
		String num = numDenom[0];
		String denom = numDenom[1];
		
		Label lNum = labels.get(fileKey + ":" + num);
		if (lNum == null)
			throw new Exception("don't recognize " + fileKey + ":" + num);
		
		Label lDenom = labels.get(fileKey + ":" + denom);
		if (lDenom == null)
			throw new Exception("don't recognize " + fileKey + ":" + denom);
			
		return lNum;
	}
	static Label getDenomLabel(HashMap<String,Label> labels, String file, String ratio) throws Exception
	{
		String fileKey = null;
		for (String key : labels.keySet())
		{
			fileKey = key.split("\\:")[0];
			if (file.contains(fileKey))
				break;
		}
		if (fileKey == null)
			throw new Exception("Can't recognise " + file);
		
		String numDenom[] = ratio.split("\\:");
		String num = numDenom[0];
		String denom = numDenom[1];
		
		Label lNum = labels.get(fileKey + ":" + num);
		if (lNum == null)
			throw new Exception("don't recognize " + fileKey + ":" + num);
		
		Label lDenom = labels.get(fileKey + ":" + denom);
		if (lDenom == null)
			throw new Exception("don't recognize " + fileKey + ":" + denom);
			
		return lDenom;
	}

	// this program reformats a bunch of protein alignment templates (exported from Excel as text format
	// it formats them into a tab-delimited file suitable for import into R or other Stats packages.
	// the format is one measurement per line, with all the descriptors for that measurement
	
	public static void main(String args[]) throws Exception
	{
		String iTRAQReps[] = {"113", "114","115","116","117","118","119","121"};
		TreeSet<String> iTRAQReporters = new TreeSet<String>();
		for(String r : iTRAQReps)
			iTRAQReporters.add(r);
		
		String folder = //"/Volumes/work/lab/Katy/McCray/ASL iTRAQ/ASL RICH ITRAQ/";
						"/Volumes/work/lab/Biospecimens-Contract/Rich/aligns/MWRI/";
						//"/Volumes/work/lab/Biospecimens-Contract/Rich/aligns/Study 2/";
						//"/Volumes/work/lab/Biospecimens-Contract/Rich/aligns/UCSF/";
						//"/Volumes/work/lab/Biospecimens-Contract/Expts/iTRAQ/iTRAQ data Magee/";
						//"/Volumes/lab/Matt/NASA CSF/NASA Fe iTRAQ/";
		String inFileName = //"ASL UNBIASED Raw Ratios";
							//"20120327 Bias Corrected alignment";
							//"20120327 Un Corrected alignment";
							"MWRI Plasma Alignment";
							//"MWRI Serum Protein Alignment";
							//"Copy of Fe Exported Alignment Ratios LOG";
							//"Study 2 Alignment";
							//"UCSF Alignment";
		String inFile = folder + inFileName + ".txt";
		String outFile = folder + inFileName + " for R all prots1.txt";
		
		// read in itraq labels
		
		HashMap<String, Label> labels = new HashMap<String,Label>();
		HashMap<String, Label> denoms = new HashMap<String,Label>();
		
		// read in the key file which must have the first two columns as fileKey and reporter, 
		// and the remaining columns have conditions describing each sample run with that reporter
		// fileKey is a string uniquely identifying a given input file
		
		BufferedReader inKeyFile = new BufferedReader(new FileReader(folder + "KeyFile.txt"));
		String line = inKeyFile.readLine();
		String keyColLabels[] = line.split("\t");
		int nConditions = keyColLabels.length - 2; // first = fileKey, second = reporter, rest are conditions
		
		while((line = inKeyFile.readLine()) != null)
		{
			String spl[] = line.split("\t");
			String fileKey = spl[0];
			String reporter = spl[1];
			
			String label[] = new String[nConditions];
			for (int i = 0; i < nConditions; i++)
				label[i] = spl[2+i];
			// see if this is a replicate
			int replicate = 1;
			for (String key : labels.keySet())
			{
				Label old = labels.get(key);
				
				boolean isEqual = true;
				for (int i = 0; i < nConditions; i++)
					if (!label[i].equals(old.label[i]))
						isEqual = false;

				if (isEqual)
					replicate++;
			}
			labels.put(fileKey+":" + reporter, new Label(fileKey, label, reporter, replicate));
		}
		inKeyFile.close();

		BufferedReader in = new BufferedReader(new FileReader(inFile));
		
		line = in.readLine();
		line = in.readLine();
		String fileList[] = line.split("\t");
		line = in.readLine();
		String ratioList[] = line.split("\t");
		Label columnNumLabels[] = new Label[ratioList.length];
		String prevFileName = "";
		
		for (int i = 0; i < ratioList.length; i++)
		{
			String fileName = "";
			if (i < fileList.length)
				fileName = fileList[i];
			if (fileName.isEmpty())
				fileName = prevFileName;
			prevFileName = fileName;
			if (ratioList[i].contains(":") && ratioList[i].startsWith("1") && ratioList[i].split("\\:")[1].startsWith("1"))
			{
				String spl[] = ratioList[i].split("\\:");
				columnNumLabels[i] = getNumLabel(labels, fileName, ratioList[i]);
				denoms.put(fileName, getDenomLabel(labels, fileName, ratioList[i]));
			}
		}
		PrintStream out = new PrintStream(new BufferedOutputStream(new PrintStream(new FileOutputStream(outFile))));
		//PrintStream out = System.out;
		
		out.print("N\tAccession\tName\tSet\tNumerator");
		for (int i = 0; i < nConditions; i++)
			out.print("\t" + keyColLabels[2+i]);
		out.println("\tReplicate\tRatio");
		
		// now read the table and output for R
		int linesWritten = 0;
		while((line = in.readLine()) != null)
		{
			line = line.trim();
			if (line.isEmpty())
				continue;
			String spl[] = line.split("\t");
			String N = "";
			String acc = "";
			String name = "";
			prevFileName = "";
			for (int i = 0; i < columnNumLabels.length; i++)
			{
				String fileName = "";
				if (i < fileList.length)
					fileName = fileList[i];
				if (fileName.isEmpty())
					fileName = prevFileName;
				
				
				N = spl[0];
				acc = spl[1];
				name = spl[2];
				name = name.replaceAll("\\\"", "");;
				if (acc.contains("|"))
				{
					String accSpl[] = acc.split("\\|");
					if (accSpl.length == 3)
						acc = accSpl[2];
				}
				if (name.contains(" OS="))
					name = name.substring(0, name.indexOf(" OS="));
				name = name.replaceAll("\\'", "-prime");
				
				if (spl.length < 5 && i == 5)
				{
					// write something for this protein
					out.print(N + "\t" + acc + "\t" + name + "\t" + columnNumLabels[i].fileKey + "\t" + columnNumLabels[i].numerator);
					for (int j = 0; j < nConditions; j++)
						out.print("\t" + columnNumLabels[i].label[j]);
					out.println("\t" + columnNumLabels[i].replicate + "\t" + "NA");
					
					continue;
				}

				if (columnNumLabels[i] == null)
					continue;
				if (spl.length <= i)
					continue;
				if (spl[i].isEmpty())
					continue;
				out.print(N + "\t" + acc + "\t" + name + "\t" + columnNumLabels[i].fileKey + "\t" + columnNumLabels[i].numerator);
				for (int j = 0; j < nConditions; j++)
					out.print("\t" + columnNumLabels[i].label[j]);
				out.println("\t" + columnNumLabels[i].replicate + "\t" + spl[i]);
				linesWritten++;
				if (linesWritten % 1000 == 0)
					System.out.print(".");
/*				
				if (!fileName.equals(prevFileName))
				{
					// print out 1 for the ratio with the denominator
					Label denomLabel = denoms.get(fileName);
					out.print(N + "\t" + acc + "\t" + name + "\t" + denomLabel.fileKey);
					for (int j = 0; j < nConditions; j++)
						out.print("\t" + denomLabel.label[j]);
					out.println("\t" + 1);
					linesWritten++;
					if (linesWritten % 1000 == 0)
						System.out.print(".");
				}
*/
				prevFileName = fileName;
			}
			
		}
		out.close();
	}
}
