Four Java programs were developed for SAIC-Frederick, Inc. Subcontract Number: 29SX144.

All four programs take as input in various forms from AB Sciex Corporation's ProteinPilot Software and 
reformat this input into a format suitable for import into the R statistics program.

The four programs are:

   FormatAlignmentForR:

      ProteinPilot comes with and Excel Template called "Alignment Template" that takes several output 
      files from several runs and combines them into one table.  Our Java program re-arranges the data
      into a straight-up format that can be imported into R and analyzed with lm or lme.


   ReformatiTRAQPeptidesForRWeightedAvgGeneral

      This program takes as input several PeptideSummary files contained in a folder, and reformats
      them into a single straight-up format as above.

   ReformatiTRAQProteinsForR
      
       Same as above, but for ProteinSummary files.

   ReformatITRAQForOxidationPeptides

      This program is the same as ReformatiTRAQPeptidesForRWeightedAvgGeneral except it was rewritten to
      find pairs of peptides with and without Oxidation on Methionine.  It then computes the ratio of iTRAQ
      ratios of the two peptides.

All four programs require one other input file located in the same directory as the other input. 
This file is called KeyFile.txt, and contains at least three columns, but can contain additional columns
as desired.  These columns are FileKey	Reporter	<Condition_1> ... <Condition_n>

    FileKey          is a string that uniquely identifies one of the file names output from ProteinPilot
    reporter         is one of 113,114,115,116,117,118,119,121 is the reporter channel from iTRAQ
    <Condition_n>    can be called anything you want, and it will be imported into R as a condition associated
                     with the given file name and reporter.

An example of KeyFile.txt is supplied.

Also included with this release are the programs used in R to analyze the data.  These are included for 
illustration purposes only, and are not intended to work on a dataset of other origin without considerable
modification.
