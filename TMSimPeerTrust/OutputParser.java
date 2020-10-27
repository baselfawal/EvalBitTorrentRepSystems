
import java.io.*;
import java.util.regex.*;

/**
 * The OutputParser class is a driver program, that when placed in a directory
 * with TraceSimulator output file(s), will parse that data into a comma-
 * separated-value file - one file a row.
 */
public class OutputParser{
	
	// ************************** PUBLIC METHODS *****************************	
	
	/**
	 * The main driver method.
	 * @param args Two arguments are required, in the following order. First, 
	 * a regular expression (string) matching the simulator output files which
	 * should be processed. Second, the destination of the CSV output.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
			
			// Get list of all output files in current directory
		File[] file_list = getFileList(args[0]);
		
		  // Open output CSV file
		File csv = new File(args[1]);
		Writer out = new BufferedWriter(new FileWriter(csv));
	
			// Write line for header, and one line per input file
		out.write(getColumnNames());
		for(int i=0; i < file_list.length; i++)
			out.write(handleSingleInput(file_list[i]));
		
			// Cleanup output file before exit
		out.flush();
		out.close();
	}
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * List all TraceSimulator output files in the current directory.
	 * @param regexp Regular expression matching output to be processed.
	 * @return An List of type File, containing all such files
	 */
	private static File[] getFileList(String regexp){
		File current_dir = new File (".");
		File[] file_list = current_dir.listFiles(new FilenameFilter(){
			public boolean accept(File parent, String filename){
				boolean do_parse  = filename.endsWith(".none");
				do_parse = do_parse || filename.endsWith(".eigen");
				do_parse = do_parse || filename.endsWith(".etinc");
				do_parse = do_parse || filename.endsWith(".tnasl");
				return do_parse;
			} // Only process files with an appropriate extension
		}); // Build file list from acceptable files in current directory
		
		int parse_count = 0;
		for(int i=0; i < file_list.length; i++){
			if(Pattern.matches(regexp, file_list[i].getName()))
				parse_count++;
		} // First count up how many filenames pass the regex
		
		int parse_position = 0;
		File[] parse_list = new File[parse_count];
		for(int i=0; i < file_list.length; i++){
			if(Pattern.matches(regexp, file_list[i].getName())){
				parse_list[parse_position] = file_list[i];
				parse_position++;	
			} // Add the files to be a parsed to a new array 
		} // This is necessary because DBClust doesn't have Java 5.0
		return (parse_list);
	}
	
	/**
	 * Get a CSV string of the column names for the data being parsed.
	 * @return Column names for the data being parsed (in CSV form)
	 */
	private static String getColumnNames(){
		return ("NUM_USERS," + "NUM_FILES," + "NUM_TRANS," + "BAND_MAX," +
				"BAND_PER," + "WARMUP," + "ZIPF," + "PRE_TRUSTED," + 
				"USR_GOOD," + "USR_PURE," + "USR_FEED," + "USR_PROV," + 
				"USR_DISG," + "USR_SYBL," + "SMART_GEN," +  "GEN_SEED," + 
				"SIM_SEED," + "SIM_ALG," + "STRATEGY," + "NUM_TRANS," + 
				"TRANS_COMP," + "TRANS_INCOMP," + "TRANS_INCOMP," +	
				"RECP_DECLINED," + "NO_SENDER," + "TRANS_COMP," + 
				"VALID_TRANS," + "INVALID_TRANS," + "FBACK_COMMIT," + 
				"FBACK_TRUE," + "FBACK_LIES," +	"FBACK_SYBL," +	"GOOD_TRANS," +
				"GOOD_SUCC," + "GOOD_FAIL\n");
	}
	
	/**
	 * Parse a single TraceSimulator output file.
	 * @param cur_file A TraceSimulator output file
	 * @return String containing parsed statistical date (in CSV form) 
	 * @throws Exception
	 */
	private static String handleSingleInput(File cur_file) throws Exception{
		
		FileInputStream fis = new FileInputStream(cur_file);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader in = new BufferedReader(new InputStreamReader(dis));
		
		String out_line = "";
		String cur_line = in.readLine();
		while(cur_line != null){
			if(cur_line.startsWith(">")){
				cur_line = cur_line.substring(25);
				cur_line = cur_line.trim();
				out_line += cur_line + ",";
			} // Lines beginning with '>' denote data entries
			cur_line = in.readLine();
		} // Process the entire input file
		out_line += "\n";
		
			// Cleanup and close input file
		in.close();
		dis.close();
		fis.close();
		return out_line;
	}

}
	