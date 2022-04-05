package com.refinitiv.ema.examples.RCC_Contribution_Example;

import com.refinitiv.ema.access.DataType;
import com.refinitiv.eta.codec.*;
import com.refinitiv.eta.transport.Error;
import com.refinitiv.eta.transport.TransportFactory;

import java.util.Formatter;

public class RWF2FieldConversion {
    private final String FIELD_DICTIONARY_FILE_NAME = "./RDMFieldDictionary";
    private final String ENUM_TABLE_FILE_NAME = "./enumtype.def";
    private Error error;
    DataDictionary dictionary;
    boolean fieldDictionaryLoadedFromFile;
    boolean enumTypeDictionaryLoadedFromFile;
    boolean fieldDictionaryDownloadedFromNetwork;
    boolean enumTypeDictionaryDownloadedFromNetwork;

    public RWF2FieldConversion() {
        init();
    }

    public void init()
    {
        dictionary = CodecFactory.createDataDictionary();

        fieldDictionaryLoadedFromFile = false;
        enumTypeDictionaryLoadedFromFile = false;
        fieldDictionaryDownloadedFromNetwork = false;
        enumTypeDictionaryDownloadedFromNetwork = false;
        error = TransportFactory.createError();
        loadDictionary();
    }
    void loadDictionary()
    {
        dictionary.clear();
        if (dictionary.loadFieldDictionary(FIELD_DICTIONARY_FILE_NAME, error) < 0)
        {
            System.out.println("Unable to load field dictionary.  Will attempt to download from provider.\n\tText: " + error.text());
        }
        else
        {
            fieldDictionaryLoadedFromFile = true;
        }

        if (dictionary.loadEnumTypeDictionary(ENUM_TABLE_FILE_NAME, error) < 0)
        {
            System.out.println("Unable to load enum dictionary.  Will attempt to download from provider.\n\tText: " + error.text());
        }
        else
        {
            enumTypeDictionaryLoadedFromFile = true;
        }
    }
    public DataDictionary getDictionary()
    {
        return dictionary;
    }
    public DictionaryEntry getDictionaryEntry(int fid){
        DictionaryEntry dictionaryEntry = getDictionary().entry(fid);
        // return if no entry found
        if (dictionaryEntry == null)
        {
            System.out.println("\tFid " + "21"+ " not found in dictionary");

            return null; //CodecReturnCodes.FAILURE;
        }


        return dictionaryEntry;
    }

    public static void main(String[] args) {
        RWF2FieldConversion con = new RWF2FieldConversion();
        con.loadDictionary();
        DictionaryEntry e = con.getDictionaryEntry(22);
        int rwfType = e.rwfType();
        if  (DataType.DataTypes.REAL==rwfType){
            System.out.println("DataTypes.REAL");
        }
        int rwfLength = e.rwfLength();
        String acronym = e.acronym().toString();
        String dDEAcronym = e.ddeAcronym().toString();
        int fid = e.fid();
        int ripplesTo = e.rippleToField();
        int fieldType = e.fieldType();
        int length = e.length();
        /*
        !ACRONYM    DDE ACRONYM          FID  RIPPLES TO  FIELD TYPE     LENGTH  RWF TYPE   RWF LEN
        !-------    -----------          ---  ----------  ----------     ------  --------   -------
        BID        "BID"                   22  BID_1       PRICE              17  REAL64           7
        !
        ! Latest Bid Price (price willing to buy)

         */
        java.util.Formatter formatter = new Formatter(System.out);
        formatter.format("%-11s%-20s%-5s%-12s%-15s%-8s%-11s%-8s\n", "ACRONYM","DDE ACRONYM", "FID", "RIPPLES TO" , "FIELD TYPE","LENGTH","RWF TYPE","RWF LEN");
        formatter.format("%-11s%-20s%-5s%-12s%-15s%-8s%-11s%-8s\n", "-------","-----------","---", "----------",  "----------" ,   "------" , "--------",   "-------");
        formatter.format("%-11s%-20s%-5s%-12s%-15s%-8s%-11s%-8s\n", acronym, "\""+dDEAcronym +"\"",fid,Integer.toString(ripplesTo),Integer.toString(fieldType),Integer.toString(length),Integer.toString(rwfType),Integer.toString(rwfLength));
        //System.out.println("ACRONYM: " + acronym );
        //System.out.println("DDE ACRONYM: " + dDEAcronym );
        //System.out.println("FID: " + fid );
//        System.out.println("RIPPLES TO: " + ripplesTo );
//        System.out.println("FIELD TYPE: " + fieldType);
//        System.out.println("LENGTH: " + length );
//        System.out.println("RWF TYPE: " + rwfType );
//        System.out.println("RWF LEN: " + rwfLength );
        e = con.getDictionaryEntry(4);
        rwfType = e.rwfType();
        if  (DataType.DataTypes.ENUM==rwfType){
            System.out.println("DataTypes.ENUM");
        }
        com.refinitiv.eta.codec.Enum fidEnumValue = CodecFactory.createEnum();
        fidEnumValue.value(1);

        EnumType enumType = con.getDictionary().entryEnumType(e, fidEnumValue);
        formatter.format("%-11s%-7s\n","ACRONYM", "FID");
        formatter.format("%-11s%-7s\n","-------", "-------");
        formatter.format("%-11s%-7s\n",e.acronym(), String.valueOf(e.fid()));
       // System.out.println("ACRONYM: " + e.acronym() );
       // System.out.println("FID: " + e.fid() );

//        System.out.println("VALUE: " + enumType.value() );
//        System.out.println("DISPLAY: " + enumType.display());
//        System.out.println("MEANING: " + enumType.meaning() );

        EnumTypeTable t = e.enumTypeTable();
        formatter.format("%-7s%-13s%-10s\n","VALUE", "DISPLAY", "MEANING");
        for  (EnumType  et : t.enumTypes()){

            formatter.format("%-7s%-13s%-10s\n",String.valueOf(et.value()), "\""+et.display()+"\"",et.meaning());

        }


    }
}
