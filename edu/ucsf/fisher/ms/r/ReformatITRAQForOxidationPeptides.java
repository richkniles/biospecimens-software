package edu.ucsf.fisher.ms.r;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import edu.ucsf.library.util.FileUtil;

/*
**
**  This program reads as input, Peptide Summary files generated by AB Sciex ProteinPilot Software
**
**  To use this program export results from ProteinPilot as PeptideSummaries.  Edit the main to open
**  the resulting folder containing the files.  The output file will be ready to import into R.
**
**
*/


public class ReformatITRAQForOxidationPeptides 
{
	static class PeptideAccumulator
	{
		double sum = 0;
		double weight = 0;
		double confidence = 0;
		
		public void add(double ratio, double pctErr, double conf) throws Exception
		{
			if (pctErr == 0)
				throw new Exception("0 pctErr");
			sum += Math.log(ratio)/pctErr;
			weight += 1/pctErr;
			confidence = Math.max(confidence, conf);
		}
		public double weigthtedAvg() throws Exception
		{
			double ret = Math.exp(sum/weight);
			if (Double.isNaN(ret))
				throw new Exception("NaN in weightedAvg()");
			return (Math.exp(sum/weight));
		}
		public double confidence()
		{
			return confidence;
		}
	}
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
		public String conditions()
		{
			String ret = "";
			for (String lab : label)
			{
				if (!ret.isEmpty())
					ret += ":";
				ret += lab;
			}
			return ret;
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
	
	static void printALine(String spl[], Label ratioLabels[], double pctErrThreshold) throws Exception
	{
		//0 1       2       3       4           5           6           7       8       9           10      11      12          13              14          15      16   17 18          19          20          21          22          23      24              25                  26
		//N	Unused	Total	%Cov	%Cov(50)	%Cov(95)	Accessions	Names	Used	Annotation	Contrib	Conf	Sequence	Modifications	Cleavages	dMass	Prec MW	Prec m/z	Theor MW	Theor m/z	Theor z	Sc	Spectrum	Time	PrecursorSignal	PrecursorElution	113:118	%Err 113:118	114:118	%Err 114:118	115:118	%Err 115:118	116:118	%Err 116:118	117:118	%Err 117:118	119:118	%Err 119:118	121:118	%Err 121:118	Area 113	Err 113	Area 114	Err 114	Area 115	Err 115	Area 116	Err 116	Area 117	Err 117	Area 118	Err 118	Area 119	Err 119	Area 121	Err 121	Background
		String accs = spl[6];
		String acclst[] = accs.split("; ");
		String parts[] = acclst[0].split("\\|");
		String acc = parts[2];
		String peptide = spl[12];
		String mods = spl[13].replaceAll("\\:","");
		for (int col = 0; col < spl.length; col++)
		{
			if (ratioLabels[col] != null)
			{
				String key = peptide + ":" + mods + ":" + ratioLabels[col].conditions();
				PeptideAccumulator accum = peptideAccumulators.get(key);
				if (!spl[col].isEmpty() && !spl[col+1].isEmpty())
				{
					double ratio = Double.parseDouble(spl[col]);
					double err = Double.parseDouble(spl[col+1]);
					
					if (err > pctErrThreshold)
						continue;
					
					if (accum == null)
						peptideAccumulators.put(key, accum = new PeptideAccumulator());
					
					double conf = Double.parseDouble(spl[11]);
					accum.add(ratio, err, conf);
				}
			}
		}
	}
	
	public static String removeOx(String mods)
	{
		String spl[] = mods.split("; ");
		
		String modsNoOx = "";
		for (String mod : spl)
		{
			if (mod.startsWith("Oxidation(M)"))
				continue;
			if (!modsNoOx.isEmpty())
				modsNoOx += "; ";
			modsNoOx += mod;
		}
		return (modsNoOx);
	}
	
	static HashMap<String, Boolean> usePeptideCache = new HashMap<String, Boolean>();
	

	static HashMap<String, TreeSet<String>> pepAccs = new HashMap<String,TreeSet<String>>();
	static HashMap<String, TreeSet<String>> pepSamples = new HashMap<String,TreeSet<String>>(); // for monitored samples to make sure have min number

	static HashMap<String, PeptideAccumulator> peptideAccumulators = new HashMap<String, PeptideAccumulator>();
	static TreeSet<String> pepsToInclude = new TreeSet<String>();
	
	public static void main(String args[]) throws Exception
	{
		double confidenceThreshold = 95;
		double pctErrThreshold = 10;

		String folder = //"/Volumes/work/lab/Biospecimens-Contract/Rich/Analysis/1. Study 2/Peptide and Protein Exports/";
						//"/Volumes/work/lab/Biospecimens-Contract/Rich/Analysis/2. UCSF/Peptide and Protein Exports/";
						//"/Volumes/lab/Biospecimens-Contract/Rich/Analysis/3. MWRI Plasma/Peptide and Proteins exports/";
						"/Volumes/lab/Biospecimens-Contract/Rich/Analysis/4. MWRI Serum/Peptide and Protein exports/";

		
		String iTRAQReps[] = {"113", "114","115","116","117","118","119","121"};
		TreeSet<String> iTRAQReporters = new TreeSet<String>();
		for(String r : iTRAQReps)
			iTRAQReporters.add(r);
		
		// read in itraq labels
		
		HashMap<String, Label> labels = new HashMap<String,Label>();
		HashMap<String, Label> denoms = new HashMap<String,Label>();
		
		BufferedReader inKeyFile = new BufferedReader(new FileReader(folder + "KeyFile.txt"));
		String line = inKeyFile.readLine();
		String keyFileColumns[] = line.split("\t");
		int nConditions = keyFileColumns.length - 2; // first = fileKey, second = reporter, rest are conditions
		// figure out which condition we're monitoring for min number samples required
		String conditions[] = new String[nConditions];

		for (int i = 2; i < keyFileColumns.length; i++)
			conditions[i-2] = keyFileColumns[i];
				
		while((line = inKeyFile.readLine()) != null)
		{
			String spl[] = line.split("\t");
			String fileKey = spl[0];
			String reporter = spl[1];
			
			String label[] = new String[nConditions];
			String condString = "";
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
			Label thisLabel;
			labels.put(fileKey+":" + reporter, thisLabel = new Label(fileKey, label, reporter, replicate));
		}
		inKeyFile.close();

		File fileList[] = FileUtil.getFileList(new File(folder), "PeptideSummary.txt", false);

		TreeSet<String> allAccessions = new TreeSet<String>();
		
		int nFiles = 0;
		// first scan files for Oxidized peptides
		for (File f : fileList)
		{
			String fileName = f.getName();

			if (fileName.startsWith("."))
				continue;
			
			BufferedReader in = new BufferedReader(new FileReader(f));
			line = in.readLine();
			while ((line = in.readLine()) != null)
			{
				String spl[] = line.split("\t");
				
				double conf = Double.parseDouble(spl[11]);
				if (conf < confidenceThreshold)
					continue;
				
				String peptide = spl[12];
				String mods = spl[13].replaceAll("\\:","");

				if (mods.contains("Oxidation(M)"))
					pepsToInclude.add(peptide + ":" + removeOx(mods));
			}
			System.out.println(fileName + ":" + pepsToInclude.size());
		}
		
		for (File f : fileList)
		{
			String fileName = f.getName();

			if (fileName.startsWith("."))
				continue;
			
			BufferedReader in = new BufferedReader(new FileReader(f));
			line = in.readLine();
			String spl[] = line.split("\t");
			Label columnNumLabels[] = new Label[spl.length];
			int ratioColumns[] = new int[spl.length];
			for (int column = 0; column < spl.length; column++)
			{
				String h = spl[column];
				if (h.contains(":"))
				{
					String hspl[] = h.split(":");
					if (hspl.length==2 && iTRAQReporters.contains(hspl[0]) && iTRAQReporters.contains(hspl[1]))
					{
						columnNumLabels[column] = getNumLabel(labels, fileName, spl[column]);
						denoms.put(fileName, getDenomLabel(labels, fileName, spl[column]));
					}
				}
			}
			
			
			int countOutputLines = 0;
			int countInputLines = 0;
			while ((line = in.readLine()) != null)
			{
// for debug, use only first 1000 lines
//if (countInputLines++ > 5000)
//	break;

				
				spl = line.split("\t");
				
				double conf = Double.parseDouble(spl[11]);
				if (conf < confidenceThreshold)
					continue;
				
				String peptide = spl[12];
				String mods = spl[13].replaceAll("\\:","");
				
				
				String acclist = spl[6];
				if (acclist.contains("RRRR"))
					continue;
				
				String modsNoOx = removeOx(mods);
				if (!pepsToInclude.contains(peptide + ":" + modsNoOx))
					continue;
				
				
				String accs[] = acclist.split("; ");
				TreeSet<String> thisPepsAccs = pepAccs.get(peptide);
				if (thisPepsAccs == null)
					pepAccs.put(peptide, thisPepsAccs = new TreeSet<String>());
				
				for (String acc : accs)
				{
					String parts[] = acc.split("\\|");
					thisPepsAccs.add(parts[2]);
					allAccessions.add(parts[2]);
				}

				printALine(spl, columnNumLabels, pctErrThreshold);
				countOutputLines += 7;
			}
			System.out.println(f.getName() + "\t" + countOutputLines);
		}

		// read fasta file for all sequences for these proteins
		
		HashMap<String,String> sequenceLookup = new HashMap<String,String>();
		SprotFastaFileReader inFasta = new SprotFastaFileReader("/Volumes/work/lab/niles/databases/sprot_20110509.fasta");
		FastaProtein p;

		// now print out all the accumulators
		String outFile = folder + "WeigtedAvg Pepides " + confidenceThreshold + " " + pctErrThreshold + " OxidationRatios" + ".txt";
		PrintStream out = new PrintStream(new BufferedOutputStream(new PrintStream(new FileOutputStream(outFile))));
		out.print("Accession" + "\t" + "Peptide" +  "\t" + "Conf");
		for (int i = 0; i < nConditions; i++)
			out.print("\t" + keyFileColumns[i+2]);
		out.println("\tOxQuant\tNoOxQuant\tRatio\tWeight\tStart\tEnd\tProteinLen");
		
		
		TreeSet<String> oxPeptidePairs = new TreeSet<String>();
		int maxDonors = 0;
		for (String pepModsRatio : peptideAccumulators.keySet())
		{
			
			String spl[] = pepModsRatio.split(":");
			String peptide = spl[0];
			String mods = spl[1];
			
			if ((peptide + ":" + mods).equals("EFNLQNMGLPDFHIPENLFLK:iTRAQ8plex@N-term; Oxidation(M)@7; iTRAQ8plex(K)@21"))
			{
				int x = 1;
				x++;
			}
			
			if (!mods.contains("Oxidation(M)"))
				continue;
			
			PeptideAccumulator accum = peptideAccumulators.get(pepModsRatio);
			
			
			// see if we can find the same peptide with and without mods
			
			String modsNoOx = removeOx(mods);
			String keyNoOx = peptide + ":" + modsNoOx;
			for (int i = 2; i < spl.length; i++)
				keyNoOx += ":" + spl[i];
			PeptideAccumulator accumNoOx = peptideAccumulators.get(keyNoOx);
			
			if (accumNoOx == null)
				continue;
			
			oxPeptidePairs.add(peptide);
			
			TreeSet<String> accSet = pepAccs.get(peptide);
			String accList = "";
			for (String acc : accSet)
			{
				if (!accList.isEmpty())
					accList += ",";
				accList += acc;
			}
			String acc = accSet.first();				
			
			int start = 0;
			int end = 0;
			int protLen = 0;

			double conf = Math.min(accum.confidence(), accumNoOx.confidence);
			out.print(accList + "\t" + peptide + ":" + mods + "\t" + conf);
			
			double oxQuant = accum.weigthtedAvg();
			double noOxQuant = accumNoOx.weigthtedAvg();
			double oxNoOxRatio = oxQuant/noOxQuant;
			double oxNoOxWeight = Math.min(accum.weight, accumNoOx.weight);
			for (int i = 0; i < nConditions; i++)
				out.print("\t" + spl[i+2]);
			out.println("\t" + oxQuant + "\t" + noOxQuant + "\t" + oxNoOxRatio + "\t" + oxNoOxWeight + "\t" + start + "\t" + end + "\t" + protLen);
			out.flush();
			
		}
		out.close(); 
		System.out.println(oxPeptidePairs.size());
	}
}
