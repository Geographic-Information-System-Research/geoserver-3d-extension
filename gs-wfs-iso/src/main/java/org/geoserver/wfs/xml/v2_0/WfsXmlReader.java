/* (c) 2014 - 2015 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v2_0;

import java.io.Reader;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.config.GeoServer;
import org.geoserver.ows.XmlRequestReader;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.WFSException;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.xml.ISOFeatureTypeSchemaBuilder;
import org.geoserver.wfs.xml.WFSURIHandler;
import org.geoserver.wfs.xml.WFSXmlUtils_ISO;
import org.geoserver.util.EntityResolverProvider;
import org.geotools.util.Version;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.xml.Parser;

/**
 * Xml reader for wfs 2.0 xml requests.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class WfsXmlReader extends XmlRequestReader {

    GeoServer gs;
    EntityResolverProvider entityResolverProvider;
    
    public WfsXmlReader(String element, GeoServer gs) {
        super(new QName(WFS.NAMESPACE, element), new Version("2.0.0"), "wfs");
        this.gs = gs;
        this.entityResolverProvider = new EntityResolverProvider(gs);
    }  
    
    @Override
    public Object read(Object request, Reader reader, Map kvp) throws Exception {
        WFSConfiguration_ISO config = new WFSConfiguration_ISO();
        WFSXmlUtils_ISO.initWfsConfiguration(config, gs, new ISOFeatureTypeSchemaBuilder.GML32(gs));
        
        Parser parser = new Parser(config);
        parser.setEntityResolver(entityResolverProvider.getEntityResolver());
        
        WFSInfo wfs = wfs();
        
        WFSXmlUtils_ISO.initRequestParser(parser, wfs, gs, kvp);
        Object parsed = null;
        try {
            parsed = WFSXmlUtils_ISO.parseRequest(parser, reader, wfs);    
        }
        catch(Exception e) {
            //check the exception, and set code to OperationParsingFailed if code not set
            if (!(e instanceof ServiceException) || ((ServiceException)e).getCode() == null) {
                e = new WFSException("Request parsing failed", e, "OperationParsingFailed");
            }
            throw e;
        }

        WFSXmlUtils_ISO.checkValidationErrors(parser, this);
        
        return parsed;
    }

    WFSInfo wfs() {
        return gs.getService(WFSInfo.class);
    }
}
