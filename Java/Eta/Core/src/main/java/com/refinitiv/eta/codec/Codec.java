package com.refinitiv.eta.codec;

import com.refinitiv.eta.codec.RwfDataConstants;
import com.refinitiv.eta.transport.LibraryVersionInfo;
import com.refinitiv.eta.transport.LibraryVersionInfoImpl;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Collection of interfaces to query supported RWF versions for encoder/decoder
 * as well as RWF protocol type being used by the connection.
 */
public class Codec
{
    /** The RWF protocol type. */
    static public final int RWF_PROTOCOL_TYPE = 0;
    static private LibraryVersionInfoImpl _libVersionInfo = new LibraryVersionInfoImpl();

    static
    {
        Package thisPackage = Codec.class.getPackage();
        _libVersionInfo.productInternalVersion(thisPackage.getImplementationVersion());
        _libVersionInfo.productVersion(thisPackage.getSpecificationVersion());
        try {
            URLClassLoader cl = (URLClassLoader) Codec.class.getClassLoader();
            Enumeration<URL> urls = cl.findResources("META-INF/MANIFEST.MF");
            while(urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if(url.getPath().contains("upa-")){
                    Manifest manifest = new Manifest(url.openStream());
                    String val = manifest.getMainAttributes().getValue("Build-Date");
                    _libVersionInfo.productDate(val);
                    break;
                }
            }
        } catch (Exception e) {
            _libVersionInfo.productDate(null);
        }

        if (_libVersionInfo.productInternalVersion() == null) {
            _libVersionInfo.productInternalVersion("UPA Java Edition");
        }

        if (_libVersionInfo.productVersion() == null) {
            _libVersionInfo.productVersion("UPA Java Edition");
        }

        if (_libVersionInfo.productDate() == null) {
            _libVersionInfo.productDate("N/A");
        }
    }

    /**
     * Protocol type definition. This can be used in conjunction transport layer
     * to indicate protocol being used on the connection. Codec only supports RWF protocol type.
     * 
     * @return protocol type.
     */
    public static int protocolType()
    {
        return RWF_PROTOCOL_TYPE;
    }

    /**
     * Version Major number for the version of RWF supported.
     * 
     * @return RWF major version
     */
    public static int majorVersion()
    {
        return RwfDataConstants.MAJOR_VERSION_1;
    }

    /**
     * Version Minor number for the version of RWF supported.
     * 
     * @return RWF minor version
     */
    public static int minorVersion()
    {
        return RwfDataConstants.MINOR_VERSION_1;
    }

    /**
     * Programmatically extracts library and product version information that is
     * compiled into this library.<BR>
     * 
     * User can call this method to programmatically extract version information.<BR>
     *
     * @return the library version info
     * @see com.refinitiv.eta.transport.LibraryVersionInfo
     */
    public static LibraryVersionInfo queryVersion()
    {
        return _libVersionInfo;
    }
}
