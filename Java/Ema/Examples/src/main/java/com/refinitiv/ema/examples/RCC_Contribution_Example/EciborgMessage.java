package com.refinitiv.ema.examples.RCC_Contribution_Example;

import com.refinitiv.ema.access.UpdateMsg;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.omg.IOP.Encoding;

import java.io.*;

import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.refinitiv.ema.examples.RCC_Contribution_Example.EciborgMessage.*;

class RecordType{
    private static volatile RecordType instance = null;
    private RecordType()
    {

    }
    public static RecordType getInstance()
    {
        if (instance == null) {
            synchronized (RecordType.class) {
                if(instance == null)
                    instance = new RecordType();
            }
        }
        return instance;
    }
    public boolean GetRecordType(String recordName)
    {
        return ! recordName.contains("=");
        //if (this.PageList.Contains(recordName))
        //{
        //    return true;
        //}
        //else
        //{
        //    return false;
        //}
    }
}
class Response{

    private int responseCode;
    private byte[] tag = new byte[2];
    private String ricName;
    private int throllteWait;
    private int throllteCredit;
    private int throllteCreditCeiling;
    private int throllteRefresh;

    public Response(Receive recv){
        this.ricName = recv.ricName;
    }
}
class Receive{
    public  String messageId;
    public byte[] tag ;
    public String ricName;
    public List<Fid> data;
    public ReceiveCategory category = ReceiveCategory.Unknown;
    boolean isPage=false;
    public byte[] buffer;

    @Contract(pure = true)

    public Receive( ReceiveCategory category, String messageId, byte[] tag, String ricName, List<Fid> data, boolean isPage) {

        this.messageId = messageId;

        this.tag = tag;
        this.ricName = ricName;
        this.data = data;
        this.category = category;
        this.isPage = isPage;
    }
}
class Fid
{
    public String id;
    public String value;
    public String parsed;
    public Fid() {
    }

    public Fid(String id, String value) {
        this.id = id;
        this.value = value;
    }
    public String parseFidToString(boolean pageFlag) throws UnsupportedEncodingException {
        byte[] buff = this.value.getBytes("US-ASCII");
        return ParseFid(buff, pageFlag);
    }
    private  String ParseFid(byte[] fidInputBuffer, boolean pageFlag) throws UnsupportedEncodingException {
        if (!pageFlag)
        {
            return getString(fidInputBuffer, 0, fidInputBuffer.length);
        }
        String pageFidValue = createRepeatString((char) EciborgMessage.Partial,80);
        int endIndex = -1;
        boolean last = false;
        int startIndex = 0;
        String rrepField = null;
        int rrepPos = 0;

        //<CSI>18<RHPA>v3.1.2.1<CSI>39<RHPA>STANDBY<CSI>65<RHPA>+EED+MLIP
        startIndex = Notation.IndexOfCSI(fidInputBuffer, CSI, 0);
        int lastPos = 0;
        int checkFull = startIndex;
        if (checkFull == -1)
        {
            String fidValue = createRepeatString(' ', 80);
            String field = getString(fidInputBuffer, 0, fidInputBuffer.length);
            fidValue = ReplaceAt(fidValue, 0, field.length(), field);
            return fidValue;
        }

        while (last == false && startIndex > 0)
        {
            int notationPos =Notation.PeekPartialNotation(fidInputBuffer, startIndex);
            if (fidInputBuffer[notationPos] == RHPA)
            {
                //<CSI>20<RHPA>B<CSI>6<RREP>
                //<CSI>18<RHPA>
                //RHPA>Failed <CSI>8<RREP>
                endIndex = notationPos;
                int partialOffset = 0;

                if (endIndex - startIndex == 3)
                {
                    byte[] offset = new byte[2];
                    offset[0] = fidInputBuffer[startIndex + 1];
                    offset[1] = fidInputBuffer[startIndex + 2];
                    partialOffset = Integer.parseInt(getString(offset));
                }
                else if ((endIndex - startIndex) == 2)
                {
                    byte[] offset = new byte[1];
                    offset[0] = fidInputBuffer[startIndex + 1];
                    partialOffset = Integer.parseInt(getString(offset));
                }

                startIndex = endIndex;
                //<CSI>39<RHPA>STANDBY<CSI>65<RHPA>+EED+MLIP
                endIndex = Notation.IndexOfCSI(fidInputBuffer, CSI, startIndex);
                if (endIndex == -1)
                {
                    endIndex = fidInputBuffer.length + 1;
                    last = true;
                }

                String field = null;
                field = getString(fidInputBuffer, startIndex + 1, endIndex - startIndex - 2);
                int nextNotation = Notation.PeekPartialNotation(fidInputBuffer, endIndex);
                if (nextNotation !=-1){
                    if (fidInputBuffer[nextNotation] == RREP){
                        //<CSI>65<RHPA>Failed <CSI>8<RREP>
                        //  01 23   4  567890 12 13 14
                        //rollback
                        //notationPos = nextNotation;
                        startIndex = endIndex;
                        //endIndex =  notationPos;
                        rrepField = field;
                        rrepPos = partialOffset;
                        continue;
                    }

                }

                //<CSI>46<RHPA>3:01<CSI>72<RHPA>3:07
                pageFidValue = ReplaceAt(pageFidValue, partialOffset, field.length(), field);
                startIndex = endIndex;

                lastPos = partialOffset + field.length();

            }
            else if (fidInputBuffer[notationPos] == RREP)
            {
                //B<CSI>6<RREP>
                endIndex = notationPos;
                int repeatCount = Integer.parseInt(getString(fidInputBuffer, endIndex - 1, 1));
                String repeatString= rrepField;
                int repeatPos = rrepPos;


                //char[] repeatChar = getString(fidInputBuffer, startIndex - 2, 1).toCharArray();
                //String repeatString = createRepeatString(repeatChar[0], repeatCount);
                StringBuilder generatedRepeatString=new StringBuilder();
                for (int i =0; i<repeatCount;i++ ){
                    generatedRepeatString.append(repeatString);
                }

                pageFidValue = ReplaceAt(pageFidValue, repeatPos, generatedRepeatString.length(), generatedRepeatString.toString());
                startIndex = endIndex;
                endIndex = Notation.IndexOfCSI(fidInputBuffer, CSI, startIndex);
                if (endIndex == -1)
                {
                    endIndex = fidInputBuffer.length - 1;
                    last = true;
                }

                startIndex = endIndex;
            }
        }

        if (checkFull != 1)
        {
            String fieldBegin = getString(fidInputBuffer, 0, checkFull - 1);
            pageFidValue = ReplaceAt(pageFidValue, 0, fieldBegin.length(), fieldBegin);
        }

        pageFlag = true;
        System.out.println(pageFidValue);
        return pageFidValue;
    }
}
enum ReceiveCategory
{
    Unknown,
    Insert,
    Status,
    Unsolicited,
    ThrottleWarning,
    ThrottleSevereWarning,
    ThrottleExceeded,
    ThrottleRejected,
    Timeout
};

class Notation
{
    // 2 bytes to indicate the message size to send/receive.
    public static final short LeadingBytesLength = 2;

    // The max message size to send in one go.
    // There are limitations for the Marketfeed insert messages. These are:
    // Maximum message size, excluding the Marketfeed headers 2300 bytes
    // Maximum number of RIC names per message 1
    // Maximum number of FID/FDAT pairs per message 500
    public static final short MaxBodyLength = 2300;

    // Total message size including first 2 bytes.
    public static final short MaxMessageLength = LeadingBytesLength + MaxBodyLength;

    public static final byte FileSeparator = 0x1C;      // File Separator DEC 28
    public static final byte GroupSeparator = 0x1D;      // Group Separator DEC 29
    public static final byte RecordSeparator = 0x1E;      // Record Separator DEC 30
    public static final byte UserDataSeparator = 0x1F;      // Unit Separator  DEC 31
    public static final byte Insert = 0x35;  // '5' as a byte.  DEC 53
    public static final byte HorizontalPositionAdjustTerminator = 0x60; //DEC
    public static final byte RepeatTerminator = 0x62;
    public static final byte[] ControlSequenceInitiator = { 0x1B, 0x5B };
    public static int PeekPartialNotation(byte[] buffer, int startIndex)
    {
        for (int i = startIndex; i < buffer.length; i++)
        {
            if (buffer[i] ==HorizontalPositionAdjustTerminator || buffer[i] == RepeatTerminator)
            {
                return i;
            }
        }

        return -1;
    }
    public static byte[] PeekNotation(byte[] data, int index, byte[] lastNotation)
    {
        byte b = data[index];

        if (b == FileSeparator || b == GroupSeparator || b == RecordSeparator || b == UserDataSeparator)
        {
            return new byte[] { b };
        }
        else if (b == HorizontalPositionAdjustTerminator || b == RepeatTerminator)
        {
            if (lastNotation != null && lastNotation.length == 2 && lastNotation[0] == ControlSequenceInitiator[0] && lastNotation[1] == ControlSequenceInitiator[1])
            {
                return new byte[] { b };
            }
            else
            {
                return null;
            }
        }
        else if (index + 1 < data.length && b == ControlSequenceInitiator[0] && data[index + 1] == ControlSequenceInitiator[1])
        {
            return new byte[] { b, data[index + 1] };
        }
        else
        {
            return null;
        }
    }
    public static int IndexOfCSI(byte[] input, byte[] target, int startIndex)
    {
        for (int i = startIndex; i < input.length - 1; i++)
        {
            if (input[i] == target[0] && input[i + 1] == target[1])
            {
                return i + 1;
            }
        }

        return -1;
    }
    public static String GetNotationText(byte[] notation)
    {
        if (notation.length == 1)
        {
            switch (notation[0])
            {
                case FileSeparator:
                {
                    return "<FS>";
                }

                case GroupSeparator:
                {
                    return "<GS>";
                }

                case RecordSeparator:
                {
                    return "<RS>";
                }

                case UserDataSeparator:
                {
                    return "<US>";
                }

                case HorizontalPositionAdjustTerminator:
                {
                    return "<RHPA>";
                }

                case RepeatTerminator:
                {
                    return "<RREP>";
                }
            }
        }
        else if (notation.length == 2)
        {
            if (notation[0] == ControlSequenceInitiator[0] && notation[1] == ControlSequenceInitiator[1])
            {
                return "<CSI>";
            }
        }

        return null;
    }
}
public class EciborgMessage {


    public static final byte FS = 0x1C;             //28
    public static final byte GS = 0x1D;             //29
    public static final byte RS = 0x1E;             //30
    public static final byte US = 0x1F;             //31
    public static final byte Insert = 0x35;         //53
    public static final byte RHPA = 0x60;           //96 '`'
    public static final byte RREP = 0x62;           //98
    public static final byte CS = 0x1B;             //27
    public static final byte SI = 0x5B;             //91
    public static final byte[] CSI = { CS, SI };    //27,91
    public static final byte Partial = 0x2C;        //44
    public static final byte Equal = 0x3d;          //61
    public static final byte Point = 0x2e;          //46
    public static final byte CLR = 0x4e;


    public static void main(String[] args) {
        //String pageFidValue = createRepeatString((char) EciborgMessage.Partial,80);
        //System.out.println(pageFidValue);
        //String field = "39";
        //pageFidValue = ReplaceAt(pageFidValue, 18, field.length(), field);
        byte[] bLine1= {0x1B, 0x5B,'1','8',0x60,'7','0'};
        byte[] bLine2= {0x1B,0x5B,0x31,0x38,0x60,0x37,0x30};

        //String sLine1 = bLine1.toString();
        //String sLine2 = bLine2.toString();
        //String sLine1 = new String(bLine1, StandardCharsets.US_ASCII);
        //String sLine2 = new String(bLine2, StandardCharsets.US_ASCII);
        //System.out.println(sLine1);
        //System.out.println(sLine2);


        byte[] bytes1  = {0x0,0x29,FS,Insert,US,'A',0x40,GS,'J','Z','J','P','0','X','3','F','T','A',0x3d,'0','7',
                RS,'3','9','3',US,'1',0x2e,'8','8','1','4',
                RS,'2','7','5',US,'1',0x2e,'8','8','9',FS,
                0x0,0x29,FS,'5',US,'B',0x40,GS,'J','Z','J','P','0','X','3','F','T','A',0x3d,'0','8',RS,'3','9','3',US,'1',0x2e,'8','8','1','4',RS,'2','7','5',US,'1',0x2e,'8','8','9',FS,
                0x0,0x0a,FS,'4','0','9',US,0x40,0x40,GS,'0',FS};

        byte[] bytes = {0x00, 0x6E,FS,Insert,US,'H','A',GS,'M','P','L','D','-','D','D','N','M','C','I','B','0','1','B',
                RS,'3','1','6',US,CS,SI,'1','8',RHPA,'v','3','.','1','.','2','.','1',CS,SI,'3','9',RHPA,'S','T','A','N','D','B','Y',CS,SI,'6','5',RHPA,'+','E','E','D','+','M','L','I','P',
                RS,'3','1','8',US,CS,SI,'1','8',RHPA,'7','0',CS,SI,'3','9',RHPA,'0','0','.','0','0',':','0','0',':','1','9',CS,SI,'6','5',RHPA,'0','0','.','0','0',':','0','0',':','0','0',FS};
        //  00 57<FS>5<US>G\<GS>MPLD-DDNMCIB01B
        // <RS>318<US><CSI>46<RHPA>3:01<CSI>72<RHPA>3:07
        // <RS>328<US><CSI>65<RHPA>Failed <CSI>8<RREP>
        // <RS>334<US><CSI>25<RHPA>Failed <CSI>8<RREP><FS>
        byte[] bytes2 = { 0x0,0x57,FS,Insert,US,'G','\\',GS,'M','P','L','D','-','D','D','N','M','C','I','B','0','1','B',
                RS,'3','2','8',US,CS,SI,'6','5',RHPA,'F','a','i','l','e','d',' ',CS,SI,'8',RREP,
                RS,'3','1','8',US,CS,SI,'4','6',RHPA,'3',':','0','1',CS,SI,'7','2',RHPA,'3',':','0','7',
                RS,'3','3','4',US,CS,SI,'2','5',RHPA,'F','a','i','l','e','d',' ',CS,SI,'8',RREP,FS};
        //<CSI>20<RHPA>B<CSI>6<RREP>

        int size = bytes2.length;
        String fmt = Print(bytes1, size, true, true);
        System.out.println(fmt);

        EciborgMessage em = new EciborgMessage();
        em.DecodeMessageBuffer(bytes2, bytes.length);

        System.out.println(bytes.toString());
    };
    private long rccPostId = 0;
    private  UpdateMsg updateMsg = null;
    private ConcurrentLinkedQueue<EciborgMessage> sendMsgQueue;
    private static boolean IsPrintable(char c)
    {
        return c >= 0x20 && c <= 0x7e;
    }
    public static String Print(byte[] buffer, int length, boolean wrap, boolean tagInHex)
    {
        StringBuilder text = new StringBuilder();
        StringBuilder tags = new StringBuilder();

        int index = 0;

        byte[] lastNotation = null;
        int notationFSCount = 0;
        boolean isFirstHexChar = false;
        boolean isFirstTagChar = true;
        while (index < buffer.length && index < length)
        {
            byte[] notation = Notation.PeekNotation(buffer, index, lastNotation);

            if (notation != null && notation.length > 0)
            {
                if (notation.length == 1 && notation[0] == Notation.FileSeparator)
                {
                    notationFSCount++;
                }

                text.append(Notation.GetNotationText(notation));

                lastNotation = notation;

                if (notation.length == 1 && notation[0] == Notation.GroupSeparator && index >= 6 && (byte)buffer[index - 3] == Notation.UserDataSeparator)
                {
                    // Look back to see what the currentTagValue is.
                    byte[] tag = new byte[2];

                    tag[0] = (byte)buffer[index - 2];
                    tag[1] = (byte)buffer[index - 1];

                    tags.append((tags.length() > 0 ? "," : "") + DecodeTag(tag));
                }

                index += notation.length;

                isFirstHexChar = true;
            }
            else
            {
                byte b = buffer[index];

                if (lastNotation == null || (lastNotation[0] == Notation.FileSeparator && notationFSCount % 2 == 0))
                {
                    text.append((isFirstHexChar ? "": " ") + String.format( "%02x", b));

                    isFirstHexChar = false;
                }else if(lastNotation[0] == Notation.UserDataSeparator && (
                        buffer[index+1]==Notation.GroupSeparator ||
                                buffer[index+2]==Notation.GroupSeparator)){
                    if (tagInHex) {
                        if (isFirstTagChar)
                            text.append(String.format("%02x", b));
                        else {
                            text.append(String.format(" %02x", b));
                        }
                        isFirstTagChar = !isFirstTagChar;
                    }else {
                        text.append((char) b);
                    }

                }
                else
                {
                    text.append((char) b);
                }

                index++;
            }
        }

        if (wrap)
        {
            return String.format(Locale.ROOT, "Tags=%s, Size=%d%s    %s%s", tags.toString(), length, System.lineSeparator(), text.toString(), System.lineSeparator());
        }
        else
        {
            return String.format(Locale.ROOT, "Tags=%s, Size=%d %s", tags.toString(), length, text.toString());
        }
    }

    public static Queue<Integer> GetIndexOfFS(byte[] input, int length)
    {
        Queue<Integer> indexs = new LinkedList<>();
        byte target = FS;
        int strartIndex = 0;
        for (int i = strartIndex; i < length; i++)
        {
            if (input[i] == target)
            {
                indexs.add(i);
            }
        }

        return indexs;
    }
    public static void copyBytes(byte[] source,int sourceFrom, byte[] dest, int destFrom, int copyLength) {

        for (int i = destFrom; i < destFrom + copyLength; i++) {
            dest[i] = source[sourceFrom++];
        }
    }
    public  List<EciborgMessage> DecodeMessageBuffer(byte[] messageBuffer, int length){
        ByteArrayInputStream bInput = new ByteArrayInputStream(messageBuffer);

        processReceives(bInput);

        return null;

    }
    private short ReadSort(ByteBuffer  buff ){
        byte[] networkByteOrder = new byte[2];
        buff.get(networkByteOrder, 0,2);

        return ByteBuffer.wrap(networkByteOrder).getShort();

    }
    @NotNull
    private List<byte[]>  ExtractReceives(InputStream is){
        ByteArrayInputStream bis = (ByteArrayInputStream) is;

        /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int nRead;
        byte[] buf = new byte[4];

        while ((nRead = bis.read(buf, 0, buf.length)) != -1) {
            bos.write(buf, 0, nRead);
        }
        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] targetArray = bos.toByteArray();
        ByteBuffer stream = ByteBuffer.wrap(targetArray);
        */
        ByteBuffer stream = ByteBuffer.allocate(bis.available());
        while (bis.available() > 0) {
            stream.put((byte) bis.read());
        }
        ArrayList<byte[]>  dataList = new ArrayList<> ();
        stream.rewind();
        while (stream.position() + 2 < stream.limit()) {
            try {
                short length = ReadSort(stream);
                System.out.println("length of header: " + length);
                System.out.println("position: " + stream.position());
                byte[] data = new byte[length];
                int remaining = stream.remaining(); //41
                int limit = stream.limit();
                if (length <= remaining) {
                    stream.get(data, 0, length);
                    System.out.println("data length: " + data.length);
                    System.out.println(Arrays.toString(data));
                    dataList.add(data);
                }
            } catch (IllegalArgumentException e) {

                System.out.println("\nException Thrown : " + e);
            } catch (ReadOnlyBufferException e) {

                System.out.println("\nException Thrown : " + e);
            } catch (BufferUnderflowException e) {

                System.out.println("\nException Thrown : " + e);

            } catch (Exception e) {

            }
        }
        return dataList;
    }
    private static boolean IsHeartbeatMessage(byte[] data, int length)
    {
        //                          0   1 2 3   4   5    6    7   8  9  10 11
        //Sending (12-0): 0x0,0x0a,<FS>,4,0,9,<US>,0x40,0x40,<GS>,0,<FS>

        return length >= 9 &&
                data[0] == FS &&
                data[1] == '4' &&
                data[2] == '0' &&
                data[3] == '9' &&
                data[4] == US &&
                data[7] == GS &&
                data[length - 1] == FS;
    }

    @Contract(pure = true)
    private static boolean isInsertMessage(byte[] data, int length)
    {
        // 0   1  2   3  4
        //<FS>,5,<US>,A,0x40,<GS>,J,Z,J,P,0,X,3
        return length >= 6 &&
                data[0] == FS &&
                data[1] == '5' &&
                data[2] == US ;
    }
    /*
    HA
     0      1
    48     41
    0x40 0x40
    8 + 1 <<6 = 72

    B,0x40
    0x42,0x40
    (tag[1] - 0x40) << 6) + (tag[0] - 0x40
    2 + 0 <<6 = 2
    * */
    @NotNull
    public static String DecodeTag(byte[] tag)
    {
        if (tag.length != 2)
        {
            throw new InvalidParameterException("Tag buffer should be 2 octet in size");
        }

        return Short.toString((short)(((tag[1] - 0x40) << 6) + (tag[0] - 0x40)));
    }
    public static ReceiveCategory ParseStatusReceive(byte[] data, Integer length, String tag, int scode, String symbol){
        tag = null;
        scode = -1;
        symbol = null;
        String scodeText = null;
        ReceiveCategory category = ReceiveCategory.Unknown;

        byte[] tagb = new byte[2];

        tagb[0] = data[5];
        tagb[1] = data[6];
        tag = DecodeTag(tagb);

        return category;
    }
    private static int oneBitAfterIndexOfSymbal(byte[] input, byte target, int startIndex)
    {
        for (int i = startIndex; i < input.length; i++)
        {
            if (input[i] == target)
            {
                return i + 1;
            }
        }

        return -1;
    }
    public static String getString(byte[] data) throws UnsupportedEncodingException {
        return getString(data, 0 ,data.length);
    }
    public static String getString(byte[] data, int startIndex, int length) throws UnsupportedEncodingException {
        byte[] dest = new byte[length];
        copyBytes(data, startIndex, dest, 0, length);
        return new String(dest, "US-ASCII");
    }
    public static String createRepeatString(char ch, int length){

        //create char array of specified length
        char[] charArray = new char[length];

        //fill all elements with the specified char
        Arrays.fill(charArray, ch);

        //create string from char array and return
        return new String(charArray);
    }
    static String removeSubstring(String str, int startIndex, int endIndex) {
        if (endIndex < startIndex) {
            startIndex = endIndex;
        }

        String a = str.substring(0, startIndex);
        String b = str.substring(endIndex);

        return a + b;
    }
    public static String insertString(String orig, int startIndex, String value)
    {
        // Create a new string
        String newString = new String();

        for (int i = 0; i <=orig.length(); i++) {

            // Insert the original string character
            // into the new string
            if (i == startIndex) {

                // Insert the string to be inserted
                // into the new string
                newString += value;
            }
            if (i<orig.length() ) {
                newString += orig.charAt(i);
            }

        }

        // return the modified String
        return newString;
    }
    //pageFidValue = ReplaceAt(pageFidValue, partialOffset, field.length(), field);
    public static String ReplaceAt(String str, int index, int length, String replace)
    {
        String s =  removeSubstring(str, index, index + Math.min(length, str.length() - index));
        //System.out.println(s);
        s =  insertString(s, index, replace);
       // System.out.println(s);
        return  s;
    }
    public static String ParseFid(byte[] fidInputBuffer, boolean pageFlag) throws UnsupportedEncodingException {
        if (!pageFlag)
        {
            return getString(fidInputBuffer, 0, fidInputBuffer.length);
        }
        String pageFidValue = createRepeatString((char) EciborgMessage.Partial,80);
        int endIndex = -1;
        boolean last = false;
        int startIndex = 0;
        //<CSI>18<RHPA>v3.1.2.1<CSI>39<RHPA>STANDBY<CSI>65<RHPA>+EED+MLIP

        startIndex = Notation.IndexOfCSI(fidInputBuffer, CSI, 0);
        Hashtable<Integer, String> htNotation= new Hashtable<>();

        int lastPos = 0;
        int checkFull = startIndex;
        if (checkFull == -1)
        {
            String fidValue = createRepeatString(' ', 80);
            String field = getString(fidInputBuffer, 0, fidInputBuffer.length);
            fidValue = ReplaceAt(fidValue, 0, field.length(), field);
            return fidValue;
        }

        while (last == false && startIndex > 0)
        {

            int notationPos =Notation.PeekPartialNotation(fidInputBuffer, startIndex);
            if (fidInputBuffer[notationPos] == RHPA)
            {
                //<CSI>18<RHPA>
                endIndex = notationPos;
                int partialOffset = 0;

                if (endIndex - startIndex == 3)
                {
                    byte[] offset = new byte[2];
                    offset[0] = fidInputBuffer[startIndex + 1];
                    offset[1] = fidInputBuffer[startIndex + 2];
                    partialOffset = Integer.parseInt(getString(offset));
                }
                else if ((endIndex - startIndex) == 2)
                {
                    byte[] offset = new byte[1];
                    offset[0] = fidInputBuffer[startIndex + 1];
                    partialOffset = Integer.parseInt(getString(offset));
                }

                startIndex = endIndex;
                //<CSI>39<RHPA>STANDBY<CSI>65<RHPA>+EED+MLIP
                endIndex = Notation.IndexOfCSI(fidInputBuffer, CSI, startIndex);
                if (endIndex == -1)
                {
                    endIndex = fidInputBuffer.length + 1;
                    last = true;
                }

                String field = null;
                field = getString(fidInputBuffer, startIndex + 1, endIndex - startIndex - 2);
                pageFidValue = ReplaceAt(pageFidValue, partialOffset, field.length(), field);
                startIndex = endIndex;
                lastPos = partialOffset + field.length();
            }
            else if (fidInputBuffer[notationPos] == RREP)
            {
                /*B<CSI>6<RREP>		: inserts ‘BBBBBB’ starting at the current position.
					                  (The cursor is advanced 6 places by this operation)

                 */
                endIndex = notationPos;
                int repeatCount = Integer.parseInt(getString(fidInputBuffer, endIndex - 1, 1));
                char[] repeatChar = getString(fidInputBuffer, startIndex - 2, 1).toCharArray();
                String repeatString = createRepeatString(repeatChar[0], repeatCount);
                pageFidValue = ReplaceAt(pageFidValue, lastPos, repeatCount, repeatString);
                startIndex = endIndex;
                endIndex = Notation.IndexOfCSI(fidInputBuffer, CSI, startIndex);
                if (endIndex == -1)
                {
                    endIndex = fidInputBuffer.length - 1;
                    last = true;
                }

                startIndex = endIndex;
            }
        }

        if (checkFull != 1)
        {
            String fieldBegin = getString(fidInputBuffer, 0, checkFull - 1);
            pageFidValue = ReplaceAt(pageFidValue, 0, fieldBegin.length(), fieldBegin);
        }

        pageFlag = true;
        System.out.println(pageFidValue);
        return pageFidValue;
    }
    public static Receive ParseReceive(byte[] input, int length) throws UnsupportedEncodingException {

        String messageId  = null;

        String symbol = "";
        String tag ="";
        String reason = "";
        String action = "";
        String ricName="";
        boolean isPage = false;
        List<Fid> datas = new ArrayList<>();
        byte[] bTag = new byte[2];
        ReceiveCategory category = ReceiveCategory.Unknown;
        System.out.println("parse data:  "+ input.length + ", " + Arrays.toString(input));
        if (length > 0)
        {
            try
            {
                // STATUS RESPONSE      -> <FS>407<US>TAG<GS>S_CODE<RS>PARAM 0{<US>SUBPARAM}n<FS>
                // UNSOLICITED RESPONSE -> <FS>407<GS>S_CODE<RS><FS>
                if (IsHeartbeatMessage(input, length))
                {
                    //0x0,0x0a,
                    //  0   1 2 3  4    5     6
                    // <FS>,4,0,9,<US>,0x40,0x40,<GS>,0,<FS>
                    System.out.println("Heartbeat Message ------" );
                    int startIndex = oneBitAfterIndexOfSymbal(input, FS, 0);
                    int endIndex = oneBitAfterIndexOfSymbal(input, US, startIndex);
                    messageId = getString(input, startIndex, endIndex - startIndex - 1);
                    System.out.println("Message Id: " + messageId);

                    bTag[0] = input[5];
                    bTag[1] = input[6];

                    tag = DecodeTag(bTag);
                    System.out.println("tag: " + tag);
                    if (tag.equals("0")) category = ReceiveCategory.Status;
                }
                else if (isInsertMessage(input, length))
                {
                    //0x0,0x29,
                    //  0  1  2   3  4
                    //<FS>,5,<US>,A,0x40,<GS>,J,Z,J,P,0,X,3,F,T,A,0x3d,0,7,<RS>,3,9,3,<US>,1,0x2e,8,8,1,4,<RS>,2,7,5,<US>,1,0x2e,8,8,9,<FS>
                    // Got:  INSERT RECEIVED
                    //get <FS>5<US>
                    System.out.println("Insert Message ------" );
                    int startIndex = oneBitAfterIndexOfSymbal(input, FS, 0);
                    int endIndex = oneBitAfterIndexOfSymbal(input, US, startIndex);
                    if (endIndex == -1)
                    {
                        endIndex = oneBitAfterIndexOfSymbal(input, GS, startIndex);
                    }

                    messageId = getString(input, startIndex,endIndex - startIndex - 1);
                    System.out.println("Message Id: " + messageId);

                    bTag[0] = input[3];
                    bTag[1] = input[4];

                    tag = DecodeTag(bTag);
                    System.out.println("tag: " + tag);
                    if (messageId.equals("5"))
                        category = ReceiveCategory.Insert;

                    startIndex = oneBitAfterIndexOfSymbal(input, GS, 0);
                    endIndex = oneBitAfterIndexOfSymbal(input, RS, startIndex);
                    if (endIndex == -1)
                    {
                        endIndex = oneBitAfterIndexOfSymbal(input, FS, startIndex);
                    }
                    ricName = getString(input, startIndex,endIndex - startIndex - 1);
                    System.out.println("Ric name: " + ricName);
                    isPage = RecordType.getInstance().GetRecordType(ricName);

                    //<RS>,3,9,3,<US>,1,0x2e,8,8,1,4,
                    //<RS>,2,7,5,<US>,1,0x2e,8,8,9,
                    //<FS>
                    startIndex = endIndex;
                    endIndex = oneBitAfterIndexOfSymbal(input, US, startIndex);
                    boolean last = false;
                    if (endIndex == -1)
                    {
                        endIndex = oneBitAfterIndexOfSymbal(input, FS, startIndex);
                        last = true;
                    }
                    while (last == false)
                    {
                        Fid fid = new Fid(); //<RS>,3,9,3,<US>
                        fid.id = getString(input, startIndex, endIndex - startIndex - 1);
                        startIndex = endIndex;
                        endIndex = oneBitAfterIndexOfSymbal(input, RS, startIndex);//<US>,1,0x2e,8,8,1,4,<RS>
                        if (endIndex == -1)
                        {
                            endIndex = oneBitAfterIndexOfSymbal(input, FS, startIndex);
                            if (endIndex == -1)
                            {
                                endIndex = input.length - 1;
                            }
                            last = true;
                        }
                        /*
                        byte[] fidBuffer = new byte[endIndex - startIndex - 1];

                        copyBytes(input , startIndex, fidBuffer, 0, endIndex - startIndex - 1);

                        String str = getString(fidBuffer, 0, endIndex - startIndex - 1);
                        fid.value = ParseFid(fidBuffer, isPage);
                        */
                        //fid.value = ParseFid(fidBuffer, isPage);
                        fid.value = getString(input, startIndex, endIndex - startIndex - 1);

                        fid.parsed = fid.parseFidToString(isPage);

                        datas.add(fid);
                        if (!last) {
                            startIndex = endIndex; // <RS>,2,7,5,<US>
                            endIndex = oneBitAfterIndexOfSymbal(input, US, startIndex);
                            if (endIndex == -1) {
                                endIndex = oneBitAfterIndexOfSymbal(input, FS, startIndex);
                                last = true;
                            }
                        }
                    }
                }


            }
            catch (Exception ex)
            {
                //log.Error(Extension.Debug.Format(ex));

                throw ex;
            }
        }
        if (messageId != null)
        {
            return new Receive(category, messageId, bTag, ricName, datas, isPage);
        }
       else{
           return null;
        }

    }
    private void processReceives(ByteArrayInputStream os){
        List<byte[]> receiveList = ExtractReceives(os);

        for (byte[] data : receiveList) {
            try {
                Receive recv =  ParseReceive(data, data.length);
            } catch (Exception e) {

            }
        }

    }
    public ConcurrentLinkedQueue<EciborgMessage> getSendMsgQueue() {
        return sendMsgQueue;
    }

    public void setSendMsgQueue(ConcurrentLinkedQueue<EciborgMessage> sendMsgQueue) {

        this.sendMsgQueue = sendMsgQueue;
    }

    public EciborgMessage() {
    }

    public long getRccPostId() {
        return rccPostId;
    }

    public void setRccPostId(long rccPostId) {
        this.rccPostId = rccPostId;
    }

    public UpdateMsg getUpdateMsg() {
        return updateMsg;
    }

    public void setUpdateMsg(UpdateMsg updateMsg) {
        this.updateMsg = updateMsg;
    }
}
