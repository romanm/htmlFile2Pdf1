package hello;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Component
public class ScheduledTasks {
	private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	DateTime startMillis;
	static PeriodFormatter hmsFormatter = new PeriodFormatterBuilder()
			.appendHours().appendSuffix("h ")
			.appendMinutes().appendSuffix("m ")
			.appendSeconds().appendSuffix("s ")
			.toFormatter();
	private static int yearMin =  1993;
	private static int yearMax =  1993;

	//develop
	private static String basicDir ="/home/roman/jura/";
	//prodaction
//	private static String basicDir ="/home/holweb/jura/";

//	//develop
//	private static String workDir = "/home/roman/jura/workshop-manuals1991/";
//	//prodaction
////	private static String workDir = "/home/holweb/jura/workshop-manuals1991/";

	private static String workDir = basicDir + "workshop-manuals"
			+ yearMin
			+ "-"
			+ yearMax
			+ "/";

	private static String dirLargeHtmlName = workDir+ "OUT1html/";
	private static String dirPdfName = workDir+ "OUT1pdf/";
	final static Path pathStart = Paths.get(dirLargeHtmlName);
//	final static Path pathStart = Paths.get(dirPdfName);

	@Scheduled(fixedRate = 500000000)
	public void reportCurrentTime() {
		startMillis = new DateTime();
		System.out.println("The time is now " + dateFormat.format(startMillis.toDate()));
		logger.debug("The time is now " + dateFormat.format(startMillis.toDate()));
		logger.debug(pathStart.toFile()+"");
		filesCount = countFiles2(pathStart.toFile());
		System.out.println("filesCount " + filesCount);
		logger.debug("filesCount " + filesCount);
		try {
//			makeLargeHTML();
			makePdfFromHTML();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void makePdfFromHTML() throws IOException {
		Path pathHtmlLarge = Paths.get(dirLargeHtmlName);
//		Path pathHtmlLarge = Paths.get(dirPdfName);
		logger.debug("Start folder : "+pathHtmlLarge);
		Files.walkFileTree(pathHtmlLarge, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				final FileVisitResult visitFile = super.visitFile(file, attrs);

				fileIdx++;
				logger.debug(fileIdx + "" + "/" + filesCount + procentWorkTime() + file);

				final String fileName = file.toString();
				logger.debug(fileName);
				final String[] splitFileName = fileName.split("\\.");
				final String fileExtention = splitFileName[splitFileName.length - 1];
				String[] splitPathFileName = fileName.split("/");
				logger.debug(""+splitPathFileName);
				final String fileNameShort = splitPathFileName[splitPathFileName.length - 1];
				logger.debug(""+fileNameShort);
				
				String hTML_TO_PDF = dirPdfName+ fileNameShort+".pdf";
				File f = new File(hTML_TO_PDF);
				if(f.exists())
				{
					logger.debug("f.exists() --  "+hTML_TO_PDF);
					return visitFile;
				}

				
				if("html".equals(fileExtention)){
					logger.debug(fileName);
					try {
						savePdf(fileName, hTML_TO_PDF);
						//Files.delete(file);
					} catch (com.lowagie.text.DocumentException | IOException e) {
						System.out.println(fileName);
						e.printStackTrace();
					}
				}
				return visitFile;
			}
		});}

	void savePdf(String htmlOutFileName, String HTML_TO_PDF) throws com.lowagie.text.DocumentException, IOException {
		String url = new File(htmlOutFileName).toURI().toURL().toString();
		logger.debug(procentWorkTime()+" - start - "+HTML_TO_PDF);
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocument(url);
		renderer.layout();
		OutputStream os = new FileOutputStream(HTML_TO_PDF);
		renderer.createPDF(os);
		os.close();
		logger.debug(procentWorkTime()+" - end - "+HTML_TO_PDF);
	}

	private	int fileIdx = 0;
	int filesCount;
	String procentWorkTime() {
		int procent = fileIdx*100/filesCount;
		String workTime = hmsFormatter.print(new Period(startMillis, new DateTime()));
		String procentSecond = " - html2pdf3 - (" + procent + "%, " + workTime + "s)";
		return procentSecond;
	}
	public static int countFiles2(File directory) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				count += countFiles2(file); 
			}else
				count++;
		}
		return count;
	}
}
