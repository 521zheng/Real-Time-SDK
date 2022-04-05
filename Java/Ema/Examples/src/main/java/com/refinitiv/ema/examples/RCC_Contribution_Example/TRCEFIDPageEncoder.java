package com.refinitiv.ema.examples.RCC_Contribution_Example;
import com.refinitiv.ema.access.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static com.refinitiv.ema.examples.RCC_Contribution_Example.Contributor.qMsgs;

public class TRCEFIDPageEncoder {
    /**
     * Add output mangle flag
     */
    private static  final String OUPUT_MANGLE_FLAG = "true";

    private static final String PAGE_START = ((OUPUT_MANGLE_FLAG.equals("false"))?"[WMR":"[MPLD");
    //private static final String PAGE_START = ((OUPUT_MANGLE_FLAG.equals("false"))?"[WMR":"[BJWMR");
    private static final String PAGE_END = ((OUPUT_MANGLE_FLAG.equals("false"))?"[/WMR":"[/MPLD");
   //private static final String PAGE_END = ((OUPUT_MANGLE_FLAG.equals("false"))?"[/WMR":"[/BJWMR");

    public static final String EMPTY_STRING = "";
    public static final String FULL_STRING = "abcdefghijklmnopqrstuvwx-abcdefghijklmnopqrstuvwx-abcdefghijklmnopqrstuvwx-yyzzz";

    private static final String ROW_PREFIX = "ROW80_";



    // TODO : Put prefixes
    static final String HEADER_PREFIX = "";

    static final String FOOTER_PREFIX = "Updated: ";

    static final String FIX_TIME_PREFIX = "Date: ";

    private static final Integer MAX_ROWS = 25;

    private static final Integer MAX_FID = 339;

    private static final Integer FIRST_ROW_TO_UPDATE = 6;

    private static final Integer FIRST_FID_TO_UPDATE = 320;

    private static final Integer BLANK_ROW = 23;

    private static final Integer BLANK_FID = 337;

    private static final Integer FIX_TIME_ROW = 24;

    private static final Integer FIX_TIME_FID = 338;

    private static final Integer UPDATE_TIME_ROW = 25;

    private static final Integer UPDATE_TIME_FID = 339;

    private static final DateFormat BASE_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

    private static final SimpleDateFormat PAGE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm00");

    private static final SimpleDateFormat FOOTER_TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public TRCEFIDPageEncoder() {

    }
    public void encode(String path) {
        Scanner fileScanner = null;
        String pageFormatDateTime = null;
        try {
            fileScanner = new Scanner(new FileReader(new File(path)));
            // This is assuming that first line of a page output will always be
            // a date
            if (fileScanner.hasNext()) {
                String baseoutPutDate = fileScanner.nextLine();
                Date baseFormatDateTime = BASE_DATE_FORMAT.parse(baseoutPutDate);
                pageFormatDateTime = PAGE_DATE_FORMAT.format(baseFormatDateTime);
            } else {
                System.out.println("RMDS Page Output has no update date, publishing abandoned");
            }
            encodePages(fileScanner, pageFormatDateTime, path);
        } catch (FileNotFoundException e) {
            System.out.println("Output file not found ");
        } catch (ParseException e) {
            System.out.println("Error parsing base output file ");
        } finally {
            if (fileScanner != null) {
                fileScanner.close();
            }
        }
    }
    private void encodePages(Scanner fileScanner, String pageFormatDateTime, String path) {
        List<String> pageLines = null;
        String pageName = null;

        int lineIndex = 0;
        while (fileScanner.hasNext()) {
            String line = fileScanner.nextLine();
            line =line.replaceFirst("\\s++$", "");
            lineIndex++;

            if (line.startsWith(PAGE_START)) {
                pageName = line.substring(1, line.indexOf("]"));
                System.out.println("Reading page " + pageName + " from file " + path);
                pageLines = new ArrayList<String>();
            } else if (line.startsWith(PAGE_END)) {
                encodePage(pageName, pageFormatDateTime, pageLines);
                pageName = null;
                pageLines = null;
            } else {
                if (pageLines != null) {
                    pageLines.add(line);
                } else {
                    System.out.println("Found a line outside a page in the output file, PAGE_START:" + PAGE_START + ", PAGE_END:" + PAGE_END +", lineIdex:" + lineIndex + ", OUPUT_MANGLE_FLAG:" + OUPUT_MANGLE_FLAG + " " );
                }
            }
        }
    }
    protected void encodePage(String ricName, String pageFormatDateTime, List<String> pageLines) {

        FieldList fl = EmaFactory.createFieldList();

        UpdateMsg upd = EmaFactory.createUpdateMsg();

        Integer fid = new Integer(FIRST_FID_TO_UPDATE);
        for (String line : pageLines) {
            if (fid <= MAX_FID){
                fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(line.getBytes())));
            }else{
                System.out.println("Error: beyond the MAX_FID");
            }

            fid += 1;
        }
        fl.add(EmaFactory.createFieldEntry().rmtes(BLANK_FID, ByteBuffer.wrap(EMPTY_STRING.getBytes())));
        fl.add(EmaFactory.createFieldEntry().rmtes(FIX_TIME_FID, ByteBuffer.wrap((FIX_TIME_PREFIX+pageFormatDateTime).getBytes())));
        fl.add(EmaFactory.createFieldEntry().rmtes(UPDATE_TIME_FID, ByteBuffer.wrap((FOOTER_PREFIX+FOOTER_TIME_FORMAT.format(Calendar.getInstance().getTime())).getBytes())));
        upd.name(ricName);
        upd.payload(fl);
        //gMsg.name(ricName).payload(upd);

        //qMsgs.add(gMsg);
        EciborgMessage em = new EciborgMessage();
        em.setUpdateMsg(upd);
        qMsgs.add(em);
    }
    public void encodeBlankPage(String ricName){
        FieldList fl = EmaFactory.createFieldList();

        UpdateMsg upd = EmaFactory.createUpdateMsg();

        Integer fid = new Integer(315);
        while (fid <= MAX_FID){
            fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(EMPTY_STRING.getBytes())));
            fid += 1;
        }
        upd.name(ricName);
        upd.payload(fl);
        EciborgMessage em = new EciborgMessage();
        em.setUpdateMsg(upd);
        qMsgs.add(em);
    }

    public void encodeFullPage(String ricName){
        FieldList fl = EmaFactory.createFieldList();

        UpdateMsg upd = EmaFactory.createUpdateMsg();

        Integer fid = new Integer(315);
        while (fid <= MAX_FID){
            fl.add(EmaFactory.createFieldEntry().rmtes(fid, ByteBuffer.wrap(FULL_STRING.getBytes())));
            fid += 1;
        }
        upd.name(ricName);
        upd.payload(fl);

        EciborgMessage em = new EciborgMessage();
        em.setUpdateMsg(upd);
        qMsgs.add(em);
    }
}